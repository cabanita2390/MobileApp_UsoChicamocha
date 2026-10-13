package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.ImageEntity
import com.example.testusoandroidstudio_1_usochicamocha.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Las fotos que esperan subir viven en [ImageUtils.directorioFotosPendientes] (filesDir), no en
 * el caché: Android vacía el caché cuando le falta espacio y la evidencia se perdía sin aviso.
 *
 * - [prepararUnaVezPorProceso]: mueve al directorio nuevo las fotos pendientes que versiones
 *   anteriores dejaron en el caché, y borra las fotos que ningún registro pendiente usa (fotos
 *   quitadas del formulario o de un registro que nunca se guardó) con más de [EDAD_HUERFANA_MS].
 * - [borrarSubida]: borra la foto en cuanto el servidor la confirma, para no llenar el equipo.
 */
@Singleton
class AlmacenFotosPendientes @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageDao: ImageDao
) {
    companion object {
        private const val TAG = "AlmacenFotosPendientes"
        /** Margen para no borrar la foto de un formulario que el técnico todavía está llenando. */
        internal const val EDAD_HUERFANA_MS = 2 * 24 * 60 * 60 * 1000L
    }

    private val mutex = Mutex()
    @Volatile private var preparado = false

    private val directorio: File get() = ImageUtils.directorioFotosPendientes(context)

    suspend fun prepararUnaVezPorProceso() {
        if (preparado) return
        mutex.withLock {
            if (preparado) return
            try {
                val pendientes = imageDao.getNoSincronizadas()
                moverDesdeCache(pendientes)
                borrarHuerfanas(imageDao.getNoSincronizadas())
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo preparar el almacén de fotos pendientes", e)
            }
            preparado = true
        }
    }

    /** Borra la foto ya confirmada por el servidor, solo si está en el directorio de pendientes. */
    fun borrarSubida(localUri: String) {
        val archivo = archivoDe(localUri) ?: return
        if (archivo.parentFile?.canonicalPath == directorio.canonicalPath) {
            archivo.delete()
        }
    }

    private suspend fun moverDesdeCache(pendientes: List<ImageEntity>) {
        val cache = context.cacheDir.canonicalPath
        for (imagen in pendientes) {
            val origen = archivoDe(imagen.localUri) ?: continue
            if (origen.parentFile?.canonicalPath != cache) continue
            if (!origen.exists()) {
                Log.w(TAG, "La foto ${imagen.localId} ya no existe en el caché: no se puede recuperar")
                continue
            }
            val destino = File(directorio, origen.name)
            origen.copyTo(destino, overwrite = true)
            imageDao.actualizarLocalUri(imagen.localId, Uri.fromFile(destino).toString())
            origen.delete()
        }
    }

    private fun borrarHuerfanas(pendientes: List<ImageEntity>) {
        val enUso = pendientes.mapNotNull { archivoDe(it.localUri)?.canonicalPath }.toSet()
        val limite = System.currentTimeMillis() - EDAD_HUERFANA_MS
        directorio.listFiles()?.forEach { archivo ->
            if (archivo.canonicalPath !in enUso && archivo.lastModified() < limite) archivo.delete()
        }
    }

    private fun archivoDe(localUri: String): File? {
        val uri = Uri.parse(localUri)
        return if (uri.scheme == "file") uri.path?.let { File(it) } else null
    }
}
