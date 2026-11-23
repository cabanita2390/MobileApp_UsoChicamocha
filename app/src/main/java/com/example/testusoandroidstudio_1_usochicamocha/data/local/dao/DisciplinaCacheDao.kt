package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DisciplinaCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplinaCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(disciplinas: List<DisciplinaCacheEntity>)

    @Query("SELECT * FROM mant_disciplina_cache ORDER BY orden ASC, codigo ASC")
    fun getAllFlow(): Flow<List<DisciplinaCacheEntity>>

    @Query("DELETE FROM mant_disciplina_cache")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(disciplinas: List<DisciplinaCacheEntity>) {
        deleteAll()
        insertAll(disciplinas)
    }
}
