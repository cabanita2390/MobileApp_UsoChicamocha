package com.example.testusoandroidstudio_1_usochicamocha.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Ejecucion
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ElementoMarcado
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ElementoOtro

@Entity(tableName = "pending_mant_ejecucion")
data class EjecucionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val serverId: Long? = null,
    val uuidCliente: String,
    val fecha: String,
    val mesEjecucion: Int,
    val semanaEjecucion: Int,
    val estacionId: Long,
    val estacionNombre: String,
    val tipoMantenimiento: String,
    val tipoActividad: String,
    val actividadId: Long? = null,
    val actividadNombre: String? = null,
    val programacionId: Long? = null,
    val esProgramada: Boolean,
    val motivoNoCatalogado: String? = null,
    val resultado: String,
    val observaciones: String,
    val descripcionLibre: String? = null,
    var isSynced: Boolean = false,
    var isSyncing: Boolean = false,
    var syncFallido: Boolean = false,
    /** Usuario que hizo el registro (ver PropietarioColas); null = de la sesión actual. */
    val propietarioId: Long? = null,
    /** Orden de trabajo (OT-EB) que este registro ejecuta; null en citas y registros libres. */
    val ordenId: Long? = null,
    /** Disciplina del registro; los encolados antes de Eléctrico quedan en CIVIL. */
    @ColumnInfo(defaultValue = "CIVIL") val disciplina: String = "CIVIL",
    /** Eléctrico: ids marcados en «¿Qué se intervino?», separados por coma ("" = ninguno). */
    @ColumnInfo(defaultValue = "") val conjuntoIds: String = "",
    @ColumnInfo(defaultValue = "") val elementoIds: String = "",
    val conjuntoOtro: String? = null,
    val elementoOtro: String? = null,
    /** Elementos marcados con su conjunto, en JSON ("" = ninguno o registro anterior a Room 58). */
    @ColumnInfo(defaultValue = "") val elementosMarcadosJson: String = "",
    /** "Otro" por conjunto, en JSON ("" = ninguno). */
    @ColumnInfo(defaultValue = "") val elementosOtrosJson: String = ""
)

private val gsonMarcas = com.google.gson.Gson()

fun marcasAJson(marcas: List<ElementoMarcado>): String = if (marcas.isEmpty()) "" else gsonMarcas.toJson(marcas)

fun otrosAJson(otros: List<ElementoOtro>): String = if (otros.isEmpty()) "" else gsonMarcas.toJson(otros)

internal fun marcasDeJson(json: String): List<ElementoMarcado> =
    if (json.isBlank()) emptyList()
    else runCatching { gsonMarcas.fromJson(json, Array<ElementoMarcado>::class.java).toList() }.getOrDefault(emptyList())

internal fun otrosDeJson(json: String): List<ElementoOtro> =
    if (json.isBlank()) emptyList()
    else runCatching { gsonMarcas.fromJson(json, Array<ElementoOtro>::class.java).toList() }.getOrDefault(emptyList())

/** "3,7,12" → [3, 7, 12]; "" → []. */
internal fun idsDeCsv(csv: String): List<Long> = csv.split(',').mapNotNull { it.trim().toLongOrNull() }

fun EjecucionEntity.toDomain(): Ejecucion {
    return Ejecucion(
        localId = localId,
        serverId = serverId,
        uuidCliente = uuidCliente,
        fecha = fecha,
        mesEjecucion = mesEjecucion,
        semanaEjecucion = semanaEjecucion,
        estacionId = estacionId,
        estacionNombre = estacionNombre,
        tipoMantenimiento = tipoMantenimiento,
        tipoActividad = tipoActividad,
        actividadId = actividadId,
        actividadNombre = actividadNombre,
        programacionId = programacionId,
        esProgramada = esProgramada,
        motivoNoCatalogado = motivoNoCatalogado,
        resultado = resultado,
        observaciones = observaciones,
        descripcionLibre = descripcionLibre,
        isSynced = isSynced,
        isSyncing = isSyncing,
        syncFallido = syncFallido,
        ordenId = ordenId,
        disciplina = disciplina,
        conjuntoIds = idsDeCsv(conjuntoIds),
        elementoIds = idsDeCsv(elementoIds),
        conjuntoOtro = conjuntoOtro,
        elementoOtro = elementoOtro,
        elementosMarcados = marcasDeJson(elementosMarcadosJson),
        elementosOtros = otrosDeJson(elementosOtrosJson)
    )
}
