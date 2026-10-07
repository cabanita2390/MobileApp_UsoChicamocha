package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.CumplimientoCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CumplimientoCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(citas: List<CumplimientoCacheEntity>)

    @Query("DELETE FROM mant_cumplimiento_cache WHERE anio = :anio AND mes = :mes")
    suspend fun deleteMes(anio: Int, mes: Int)

    /** Para Cronograma: un mes puntual, todas las estaciones. */
    @Query("SELECT * FROM mant_cumplimiento_cache WHERE anio = :anio AND mes = :mes")
    fun getPorMesFlow(anio: Int, mes: Int): Flow<List<CumplimientoCacheEntity>>

    @Query("SELECT * FROM mant_cumplimiento_cache WHERE programacionId = :programacionId LIMIT 1")
    suspend fun getPorProgramacion(programacionId: Long): CumplimientoCacheEntity?

    /**
     * Para Pendientes/Home: el año completo. Incluye meses futuros porque una cita puede
     * ejecutarse por adelantado y debe salir en "Realizadas"; las pendientes futuras quedan
     * PROGRAMADA y no entran en "Por hacer" ni "Vencidas".
     */
    @Query("SELECT * FROM mant_cumplimiento_cache WHERE anio = :anio")
    fun getDelAnioFlow(anio: Int): Flow<List<CumplimientoCacheEntity>>

    /**
     * Reemplaza el caché de un mes puntual con la respuesta fresca del backend — así una
     * cita que el backend ya no devuelve (reprogramada, etc.) desaparece del caché en vez
     * de quedar huérfana.
     */
    @Transaction
    suspend fun reemplazarMes(anio: Int, mes: Int, citas: List<CumplimientoCacheEntity>) {
        deleteMes(anio, mes)
        insertAll(citas)
    }
}
