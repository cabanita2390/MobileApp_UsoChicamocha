package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.subestacion

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.ItemIntervenido
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.SubestacionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Conjuntos y elementos de «¿Qué se intervino?» guardados en el teléfono (sirven sin conexión). */
class ObtenerItemsIntervenidosUseCase @Inject constructor(
    private val repository: SubestacionRepository
) {
    operator fun invoke(): Flow<List<ItemIntervenido>> = repository.getItemsIntervenidosCache()
}
