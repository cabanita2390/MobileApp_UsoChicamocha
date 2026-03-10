package com.example.testusoandroidstudio_1_usochicamocha.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.DocumentoVehiculoEntity

@Dao
abstract class DocumentoVehiculoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(documento: DocumentoVehiculoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(documentos: List<DocumentoVehiculoEntity>)

    @Query("SELECT * FROM cache_documentos_vehiculo WHERE placa = :placa")
    abstract suspend fun getByPlaca(placa: String): List<DocumentoVehiculoEntity>

    @Query("DELETE FROM cache_documentos_vehiculo WHERE placa = :placa")
    abstract suspend fun deleteByPlaca(placa: String)

    /** Reemplaza todos los documentos de una placa (borra y reinserta) */
    @Transaction
    open suspend fun refreshForPlaca(placa: String, documentos: List<DocumentoVehiculoEntity>) {
        deleteByPlaca(placa)
        insertAll(documentos)
    }
}
