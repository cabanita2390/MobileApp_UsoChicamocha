package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.EjecucionDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.ImageDao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Libera los locks `isSyncing = 1` que quedaron persistidos cuando el proceso murió a mitad de
 * un envío (Android lo mató, el técnico cerró la app, se apagó el equipo). Ese registro quedaba
 * en "ENVIANDO" para siempre: el worker solo toma `isSyncing = 0` y nadie lo liberaba, así que
 * tampoco subía su foto.
 *
 * Corre UNA vez por proceso y antes de que cualquier sync tome un lock (lo llaman los puntos de
 * entrada: SyncDataWorker y SyncPendingImagesUseCase). Al arrancar el proceso ningún envío de
 * este proceso está en curso, así que todo `isSyncing = 1` es huérfano. Reenviar es seguro: el
 * backend responde con el registro ya guardado si el `uuidCliente` existe, y deduplica las
 * fotos por hash.
 */
@Singleton
class LiberadorLocksHuerfanos @Inject constructor(
    private val ejecucionDao: EjecucionDao,
    private val imageDao: ImageDao
) {
    private val mutex = Mutex()
    @Volatile private var liberados = false

    suspend fun liberarUnaVezPorProceso() {
        if (liberados) return
        mutex.withLock {
            if (liberados) return
            try {
                ejecucionDao.resetStuckSyncing()
                imageDao.resetStuckSyncing()
                Log.d("LiberadorLocks", "Locks de sincronización huérfanos liberados")
            } catch (e: Exception) {
                Log.e("LiberadorLocks", "No se pudieron liberar los locks huérfanos", e)
            }
            liberados = true
        }
    }
}
