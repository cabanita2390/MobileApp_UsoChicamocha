package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val formUUID: String = "",                    // Clave foránea a FormEntity (vacío si es de vehículo/ejecución)
    val vehicleInspectionUUID: String? = null,    // Clave foránea a VehiculoInspectionEntity (null si es de formulario/ejecución)
    val ejecucionUUID: String? = null,            // Clave foránea a EjecucionEntity (null si es de formulario/vehículo)
    val localUri: String,                         // La ruta local de la imagen *comprimida*
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false,                // Campo para controlar concurrencia
    /**
     * Discrimina a qué endpoint/repositorio debe subirse esta imagen — antes de este
     * campo, [SyncPendingImagesUseCase] enviaba TODAS las filas de esta tabla compartida
     * a `v1/inspection/{id}/image` sin importar su origen, lo que rompía la evidencia de
     * Subestaciones (se subía usando el id de la ejecución como si fuera un id de
     * inspección de vehículo). Ver TIPO_FORM/TIPO_SUBESTACION.
     */
    val tipo: String = TIPO_FORM
) {
    companion object {
        const val TIPO_FORM = "FORM"
        const val TIPO_SUBESTACION = "SUBESTACION"
    }
}