package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.form

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Caso de uso con la única responsabilidad de sincronizar las imágenes pendientes.
 * Se encarga de la orquestación: obtener imágenes, intentar subirlas y marcarlas como completadas.
 * IMPLEMENTA CONTROL DE CONCURRENCIA: marca imágenes como 'syncing' para evitar duplicación.
 *
 * Despacha cada imagen al repositorio/endpoint correcto según [ImageForSync.tipo]: antes
 * TODAS las filas de `pending_images` (formularios, vehículos, ejecuciones de subestación)
 * se enviaban a `FormRepository.syncImage` → `v1/inspection/{id}/image`, lo que para las
 * evidencias de Subestaciones subía la foto al endpoint equivocado usando el id de la
 * ejecución como si fuera un id de inspección de vehículo. `markImage*` sigue yendo por
 * `formRepository` porque son simples pasos a `ImageDao`, sin lógica específica de dominio.
 */
class SyncPendingImagesUseCase @Inject constructor(
    private val formRepository: FormRepository,
    private val subestacionRepository: SubestacionRepository
) {
    // La función invoke permite que la clase sea llamada como si fuera una función.
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. Obtenemos solo las imágenes de formularios cuyo texto ya se ha sincronizado.
            val pendingImages = formRepository.getPendingImagesForSync().first()

            if (pendingImages.isEmpty()) {
                Log.d("SyncPendingImagesUC", "No hay imágenes pendientes para sincronizar.")
                return Result.success(Unit) // No hay nada que hacer, es un éxito.
            }

            Log.d("SyncPendingImagesUC", "Se encontraron ${pendingImages.size} imágenes para sincronizar.")

            // 2. MARCAMOS LAS IMÁGENES COMO 'SYNCING' ANTES DE PROCESARLAS
            // Esto previene que otros workers procesen las mismas imágenes
            val imagesToProcess = mutableListOf<ImageForSync>()
            for (image in pendingImages) {
                try {
                    formRepository.markImageAsSyncing(image.localId)
                    imagesToProcess.add(image)
                    Log.d("SyncPendingImagesUC", "Imagen ${image.localId} marcada como syncing")
                } catch (e: Exception) {
                    Log.w("SyncPendingImagesUC", "No se pudo marcar imagen ${image.localId} como syncing: ${e.message}")
                    // Continuamos con la siguiente imagen
                }
            }

            // 3. Procesamos solo las imágenes que logramos marcar como syncing
            var successCount = 0
            for (image in imagesToProcess) {
                try {
                    val syncResult = if (image.tipo == ImageEntity.TIPO_SUBESTACION) {
                        subestacionRepository.syncEvidencia(image.serverId, image.localUri)
                    } else {
                        formRepository.syncImage(image.serverId, image.localUri)
                    }
                    if (syncResult.isSuccess) {
                        // 4. Si la subida es exitosa, la marcamos como synced
                        formRepository.markImageAsSynced(image.localId)
                        successCount++
                        Log.d("SyncPendingImagesUC", "Imagen ${image.localId} sincronizada exitosamente")
                    } else {
                        // Si falla, la marcamos como no syncing para que pueda reintentarse
                        formRepository.markImageAsNotSyncing(image.localId)
                        Log.e("SyncPendingImagesUC", "Falló la sincronización para la imagen con ID local: ${image.localId}")
                    }
                } catch (e: Exception) {
                    // En caso de error, liberamos el lock
                    try {
                        formRepository.markImageAsNotSyncing(image.localId)
                    } catch (unlockException: Exception) {
                        Log.e("SyncPendingImagesUC", "Error al liberar lock para imagen ${image.localId}: ${unlockException.message}")
                    }
                    Log.e("SyncPendingImagesUC", "Error procesando imagen ${image.localId}: ${e.message}")
                }
            }

            Log.d("SyncPendingImagesUC", "Sincronización de lote completada. Éxitos: $successCount de ${imagesToProcess.size}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SyncPendingImagesUC", "Ocurrió un error general en el caso de uso.", e)
            Result.failure(e)
        }
    }
}
