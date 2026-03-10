package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoMotoEntity

@Dao
abstract class DocumentoMotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(documento: DocumentoMotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(documentos: List<DocumentoMotoEntity>)

    @Query("SELECT * FROM cache_documentos_moto WHERE placa = :placa")
    abstract suspend fun getByPlaca(placa: String): List<DocumentoMotoEntity>

    @Query("DELETE FROM cache_documentos_moto WHERE placa = :placa")
    abstract suspend fun deleteByPlaca(placa: String)

    /** Reemplaza todos los documentos de una placa (borra y reinserta) */
    @Transaction
    open suspend fun refreshForPlaca(placa: String, documentos: List<DocumentoMotoEntity>) {
        deleteByPlaca(placa)
        insertAll(documentos)
    }
}
