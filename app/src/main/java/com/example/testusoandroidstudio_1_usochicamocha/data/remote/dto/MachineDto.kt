package com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto

import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.MachineEntity
import com.google.gson.annotations.SerializedName

data class MachineDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("brand") // Clave JSON del backend
    val brand: String?,
    @SerializedName("model") // Clave JSON del backend
    val model: String?,
    @SerializedName("name")  // Clave JSON del backend
    val name: String?,
    @SerializedName("num_engine") // Clave JSON del backend
    val engineNumber: String?,
    @SerializedName("numInterIdentification") // Clave JSON del backend
    val internalIdentificationNumber: String?,
    @SerializedName("runt") // Clave JSON del backend
    val runtExpirationDate: String?,
    @SerializedName("soat") // Clave JSON del backend
        val soatExpirationDate: String?
)

fun MachineDto.toEntity(): MachineEntity {
    return MachineEntity(
        id = id,
        name = name ?: "Default Name", // O manejar el nulo como prefieras
        brand = brand ?: "Default Brand",
        model = model ?: "Default Model",
        engineNumber = engineNumber ?: "Default Engine No.",
        internalIdentificationNumber = internalIdentificationNumber ?: "Default ID No.",
        runtExpirationDate = runtExpirationDate, // Puede ser nulo
        soatExpirationDate = soatExpirationDate  // Puede ser nulo
    )
}

