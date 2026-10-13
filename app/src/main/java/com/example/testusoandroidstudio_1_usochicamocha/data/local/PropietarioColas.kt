package com.example.testusoandroidstudio_1_usochicamocha.data.local

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.PropietarioColasDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.SesionActualEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cada registro pendiente se envía a nombre de quien lo hizo. Antes, si el técnico A dejaba
 * registros sin enviar (sin señal) y cerraba sesión, al entrar B en el mismo teléfono se
 * enviaban con el token de B y quedaban a su nombre.
 *
 * - Mientras hay sesión, lo nuevo queda con propietarioId = null (= de la sesión actual).
 * - Al cerrar sesión (por el usuario o porque el servidor la invalidó) lo pendiente sin dueño
 *   queda a nombre de quien estaba en sesión.
 * - Las colas solo envían lo que no tiene dueño o es del usuario en sesión (`sesion_actual`);
 *   lo de otro usuario espera en el teléfono hasta que él vuelva a entrar.
 */
@Singleton
class PropietarioColas @Inject constructor(
    private val dao: PropietarioColasDao,
    private val tokenManager: TokenManager
) {
    suspend fun alIniciarSesion(usuarioId: Long) {
        dao.fijarSesion(SesionActualEntity(usuarioId = usuarioId))
    }

    suspend fun alCerrarSesion() {
        try {
            val usuarioId = tokenManager.getUserId().first()?.toLong() ?: dao.usuarioEnSesion()
            if (usuarioId != null) dao.asignarPendientesSinDueno(usuarioId)
            dao.limpiarSesion()
        } catch (e: Exception) {
            Log.e("PropietarioColas", "No se pudo asignar el dueño de los pendientes al cerrar sesión", e)
        }
    }

    /** Alinea sesion_actual con la sesión guardada (instalaciones que vienen de antes del cambio). */
    suspend fun sincronizarConSesionGuardada() {
        val usuarioId = tokenManager.getUserId().first()?.toLong()
        if (usuarioId != null) dao.fijarSesion(SesionActualEntity(usuarioId = usuarioId)) else dao.limpiarSesion()
    }
}
