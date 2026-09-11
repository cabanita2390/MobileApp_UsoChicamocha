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

    /** Para Pendientes/Home: todo el año hasta el mes actual (mismo rango que antes armaba `getPendientesDelAnio`). */
    @Query("SELECT * FROM mant_cumplimiento_cache WHERE anio = :anio AND mes <= :mesActual")
    fun getDelAnioFlow(anio: Int, mesActual: Int): Flow<List<CumplimientoCacheEntity>>

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
