package com.example.testusoandroidstudio_1_usochicamocha.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FormDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.toDomain
import com.example.testusoandroidstudio_1_usochicamocha.data.local.pojo.ImageForSync
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormDto
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Form
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.PendingFormStatus
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.toEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.FormRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FormRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val formDao: FormDao,
    private val imageDao: ImageDao,
    private val apiService: ApiService
) : FormRepository {

    companion object {
        private const val TAG = "FormRepositoryImpl"
        private val duplicateCounter = AtomicInteger(0)
    }

    fun getDuplicateCount(): Int = duplicateCounter.get()

    override fun getPendingFormsWithStatus(): Flow<List<PendingFormStatus>> {
        return formDao.getPendingFormsWithImageCount().map { list ->
            list.map { item ->
                PendingFormStatus(
                    form = item.formEntity.toDomain(),
                    totalImageCount = item.totalImageCount,
                    syncedImageCount = item.syncedImageCount
                )
            }
        }
    }

    override fun getPendingForms(): Flow<List<Form>> {
        return formDao.getPendingFormsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveFormLocally(form: Form, imageUris: List<String>) {
        val formEntity = form.toEntity()
        formDao.insertForm(formEntity)

        if (imageUris.isNotEmpty()) {
            val imageEntities = imageUris.map { uri ->
                ImageEntity(
                    formUUID = form.UUID,
                    localUri = uri,
                    isSynced = false,
                    tipo = ImageEntity.TIPO_FORM
                )
            }
            imageDao.insertImages(imageEntities)
        }
    }

    /**
     * MÉTODO DE SINCRONIZACIÓN CON DEDUPLICACIÓN BÁSICA
     * Implementa lógica simplificada para prevenir formularios duplicados
     * SIN usar tabla de tracking adicional
     */
    override suspend fun syncForm(form: Form): Result<Unit> {
        val workerId = "worker_${Thread.currentThread().id}"
        
        Log.d(TAG, "🔄 Starting sync for form: ${form.UUID} (Worker: $workerId)")
        
        return try {
            // 1. VERIFICAR SI YA ESTÁ SINCRONIZADO LOCALMENTE
            val isAlreadySynced = formDao.isFormAlreadySynced(form.UUID)
            if (isAlreadySynced == true) {
                Log.d(TAG, "✅ Form ${form.UUID} already synced locally, skipping")
                return Result.success(Unit)
            }
            
            // Si el resultado es null, significa que no está sincronizado, continuar
            if (isAlreadySynced == null) {
                Log.d(TAG, "ℹ️ Form ${form.UUID} not synced yet, proceeding with sync")
            }

            // 2. INTENTAR OBTENER LOCK ATÓMICO BÁSICO
            val lockResult = formDao.acquireFormLock(form.UUID)
            if (lockResult == 0) {
                Log.d(TAG, "🔒 Form ${form.UUID} is being synced by another process")
                duplicateCounter.incrementAndGet()
                return Result.success(Unit)
            }

            Log.d(TAG, "🔓 Lock acquired for form ${form.UUID}, proceeding with sync")

            // 3. REALIZAR SINCRONIZACIÓN
            val formDto = form.toDto()
            val response = apiService.syncForm(formDto)

            if (response.isSuccessful && response.body() != null) {
                val serverId = response.body()!!.id
                formDao.markAsSynced(form.UUID, serverId)
                
                Log.d(TAG, "✅ Form ${form.UUID} synced successfully with serverId: $serverId")
                Result.success(Unit)
            } else {
                // 4. MANEJAR FALLO
                formDao.markAsNotSyncing(form.UUID)
                
                Log.e(TAG, "❌ Form ${form.UUID} sync failed: ${response.code()}")
                Result.failure(Exception("Error del servidor al sincronizar formulario: ${response.code()}"))
            }
        } catch (e: Exception) {
            // 5. MANEJAR EXCEPCIÓN
            formDao.markAsNotSyncing(form.UUID)
            
            Log.e(TAG, "❌ Exception syncing form ${form.UUID}", e)
            Result.failure(e)
        } finally {
            // 6. SIEMPRE LIBERAR LOCK
            formDao.markAsNotSyncing(form.UUID)
            Log.d(TAG, "🔓 Lock released for form ${form.UUID}")
        }
    }

    override fun getPendingImagesForSync(): Flow<List<ImageForSync>> {
        return imageDao.getPendingImagesForSync()
    }

    override suspend fun syncImage(formId: Long, imageUri: String): Result<Unit> {
        return try {
            val uri = Uri.parse(imageUri)

            // 1. Copiamos el contenido del Uri a un archivo temporal
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("No se pudo abrir el URI: $imageUri"))

            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 2. Creamos el RequestBody a partir del archivo
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

            // 3. Ojo al nombre del parámetro: "imagen"
            val imagePart = MultipartBody.Part.createFormData(
                "imagen",                  // nombre del campo que espera el backend
                tempFile.name,             // nombre del archivo enviado
                requestFile                // contenido
            )

            // 4. Ejecutamos la petición
            val response = apiService.syncImage(formId, imagePart)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Verificar si el error es "Duplicate image detected"
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("Duplicate image detected") == true) {
                    Log.d(TAG, "✅ Imagen duplicada detectada, tratando como éxito: $imageUri")
                    Result.success(Unit) // Tratar como éxito si es imagen duplicada
                } else {
                    Log.e(TAG, "❌ Error del servidor al subir imagen: ${response.code()} - $errorBody")
                    Result.failure(Exception("Error del servidor al subir imagen: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markImageAsSynced(localId: Int) {
        imageDao.markAsSynced(localId)
    }

    override suspend fun markImageAsSyncing(localId: Int) {
        imageDao.markAsSyncing(localId)
    }

    override suspend fun markImageAsNotSyncing(localId: Int) {
        imageDao.markAsNotSyncing(localId)
    }

    private fun Form.toDto(): FormDto {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val isoDateString = sdf.format(Date(this.timestamp))

        return FormDto(
            UUID = this.UUID,
            dateStamp = isoDateString,
            hourMeter = this.hourmeter,
            leakStatus = this.leakStatus,
            brakeStatus = this.brakeStatus,
            beltsPulleysStatus = this.beltsPulleysStatus,
            tireLanesStatus = this.tireLanesStatus,
            carIgnitionStatus = this.carIgnitionStatus,
            electricalStatus = this.electricalStatus,
            mechanicalStatus = this.mechanicalStatus,
            temperatureStatus = this.temperatureStatus,
            oilStatus = this.oilStatus,
            hydraulicStatus = this.hydraulicStatus,
            coolantStatus = this.coolantStatus,
            structuralStatus = this.structuralStatus,
            expirationDateFireExtinguisher = this.expirationDateFireExtinguisher,
            observations = this.observations,
            userId = this.userId,
            machineId = this.machineId,
            greasingAction = this.greasingAction,
            greasingObservations = this.greasingObservations,
            isUnexpected = this.isUnexpected
        )
    }
}
