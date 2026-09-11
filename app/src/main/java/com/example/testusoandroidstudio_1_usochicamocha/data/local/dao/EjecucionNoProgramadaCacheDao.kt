package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.EjecucionNoProgramadaCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EjecucionNoProgramadaCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ejecuciones: List<EjecucionNoProgramadaCacheEntity>)

    @Query("DELETE FROM mant_ejecucion_no_programada_cache WHERE anio = :anio")
    suspend fun deleteAnio(anio: Int)

    /** Para Pendientes: todo el año hasta el mes actual (mismo rango que CumplimientoCacheDao.getDelAnioFlow). */
    @Query("SELECT * FROM mant_ejecucion_no_programada_cache WHERE anio = :anio AND mes <= :mesActual")
    fun getDelAnioFlow(anio: Int, mesActual: Int): Flow<List<EjecucionNoProgramadaCacheEntity>>

    /** Reemplaza el caché del año con la respuesta fresca del backend (mismo criterio que CumplimientoCacheDao.reemplazarMes). */
    @Transaction
    suspend fun reemplazarAnio(anio: Int, ejecuciones: List<EjecucionNoProgramadaCacheEntity>) {
        deleteAnio(anio)
        insertAll(ejecuciones)
    }
}
