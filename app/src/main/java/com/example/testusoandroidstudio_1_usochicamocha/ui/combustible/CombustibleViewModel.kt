package com.example.testusoandroidstudio_1_usochicamocha.ui.combustible

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelLogEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Moto
import com.example.testusoandroidstudio_1_usochicamocha.data.local.dao.FuelStationDao
import com.example.testusoandroidstudio_1_usochicamocha.data.local.entity.FuelStationEntity
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel.GetLocalFuelLogsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel.SaveFuelLogLocalUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.fuel.SyncFuelLogsUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine.GetLocalMachinesUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.moto.GetLocalMotosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.vehiculo.GetLocalVehiculosUseCase
import com.example.testusoandroidstudio_1_usochicamocha.ui.vehiculo.VehiculoItem
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

data class CombustibleUiState(
    val stations: List<FuelStationEntity> = emptyList(),
    val assetType: String = "MACHINE",
    // Asset catalog lists
    val machines: List<Machine> = emptyList(),
    val vehiculos: List<VehiculoItem> = emptyList(),
    val motos: List<Moto> = emptyList(),
    // Selected asset (one active at a time based on assetType)
    val selectedMachine: Machine? = null,
    val selectedVehiculo: VehiculoItem? = null,
    val selectedMoto: Moto? = null,
    val fuelDateTime: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
    val odometerKm: String = "",
    val hourMeter: String = "",
    val quantity: String = "",
    val quantityUnit: String = "GALLONS",
    val pricePerUnit: String = "",
    val totalCostCalculated: String = "",
    val totalCostActual: String = "",
    val totalCostMismatch: Boolean = false,
    val fuelType: String = "DIESEL",
    val serviceStation: String = "",
    val discountAmount: String = "",
    val voucherNumber: String = "",
    val notes: String = "",
    val invoicePhotoPath: String? = null,
    val isLoading: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CombustibleViewModel @Inject constructor(
    private val saveFuelLogLocalUseCase: SaveFuelLogLocalUseCase,
    private val syncFuelLogsUseCase: SyncFuelLogsUseCase,
    private val getLocalFuelLogsUseCase: GetLocalFuelLogsUseCase,
    private val getLocalMachinesUseCase: GetLocalMachinesUseCase,
    private val getLocalVehiculosUseCase: GetLocalVehiculosUseCase,
    private val getLocalMotosUseCase: GetLocalMotosUseCase,
    private val fuelStationDao: FuelStationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CombustibleUiState())
    val uiState: StateFlow<CombustibleUiState> = _uiState.asStateFlow()

    private val _fuelLogs = MutableStateFlow<List<FuelLogEntity>>(emptyList())
    val fuelLogs: StateFlow<List<FuelLogEntity>> = _fuelLogs.asStateFlow()

    init {
        viewModelScope.launch {
            getLocalFuelLogsUseCase().collect { logs -> _fuelLogs.value = logs }
        }
        viewModelScope.launch {
            fuelStationDao.getAll().collect { list -> _uiState.update { it.copy(stations = list) } }
        }
        viewModelScope.launch {
            getLocalMachinesUseCase().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(machines = resource.data ?: emptyList()) }
                }
            }
        }
        viewModelScope.launch {
            getLocalVehiculosUseCase().collect { list ->
                _uiState.update { it.copy(vehiculos = list) }
            }
        }
        viewModelScope.launch {
            getLocalMotosUseCase().collect { list ->
                _uiState.update { it.copy(motos = list) }
            }
        }
    }

    fun setAssetType(value: String) = _uiState.update {
        it.copy(
            assetType = value,
            selectedMachine = null,
            selectedVehiculo = null,
            selectedMoto = null
        )
    }

    fun onMachineSelected(machine: Machine) = _uiState.update { it.copy(selectedMachine = machine) }
    fun onVehiculoSelected(v: VehiculoItem) = _uiState.update { it.copy(selectedVehiculo = v) }
    fun onMotoSelected(m: Moto) = _uiState.update { it.copy(selectedMoto = m) }

    fun setFuelDateTime(value: String) = _uiState.update { it.copy(fuelDateTime = value) }
    fun setOdometerKm(value: String) = _uiState.update { it.copy(odometerKm = value) }
    fun setHourMeter(value: String) = _uiState.update { it.copy(hourMeter = value) }
    fun setQuantityUnit(value: String) = _uiState.update { it.copy(quantityUnit = value).also { _ -> recalculateCost() } }
    fun setFuelType(value: String) = _uiState.update {
        val unit = if (value == "GAS_NATURAL") "CUBIC_METERS" else if (it.quantityUnit == "CUBIC_METERS") "GALLONS" else it.quantityUnit
        it.copy(fuelType = value, quantityUnit = unit)
    }
    fun setServiceStation(value: String) = _uiState.update { it.copy(serviceStation = value) }
    fun setDiscountAmount(value: String) = _uiState.update { it.copy(discountAmount = value) }
    fun setVoucherNumber(value: String) = _uiState.update { it.copy(voucherNumber = value) }
    fun setNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun setInvoicePhotoPath(path: String?) = _uiState.update { it.copy(invoicePhotoPath = path) }
    fun clearSuccess() = _uiState.update { it.copy(submissionSuccess = false, error = null) }

    fun setQuantity(value: String) {
        _uiState.update { it.copy(quantity = value) }
        recalculateCost()
    }

    fun setPricePerUnit(value: String) {
        _uiState.update { it.copy(pricePerUnit = value) }
        recalculateCost()
    }

    fun setTotalCostActual(value: String) {
        _uiState.update { it.copy(totalCostActual = value) }
        checkMismatch()
    }

    private fun recalculateCost() {
        val state = _uiState.value
        val qty = state.quantity.toDoubleOrNull() ?: return
        val price = state.pricePerUnit.toDoubleOrNull() ?: return
        _uiState.update { it.copy(totalCostCalculated = String.format("%.2f", qty * price)) }
        checkMismatch()
    }

    private fun checkMismatch() {
        val state = _uiState.value
        val calculated = state.totalCostCalculated.toDoubleOrNull() ?: return
        val actual = state.totalCostActual.toDoubleOrNull()
        if (actual != null && calculated > 0) {
            val deviation = abs(calculated - actual) / calculated
            _uiState.update { it.copy(totalCostMismatch = deviation > 0.01) }
        } else {
            _uiState.update { it.copy(totalCostMismatch = false) }
        }
    }

    fun submitFuelLog() {
        val state = _uiState.value
        val isMachine = state.assetType == "MACHINE"

        val (assetIdLong, assetPlate) = when (state.assetType) {
            "MACHINE" -> {
                val m = state.selectedMachine
                if (m == null) {
                    _uiState.update { it.copy(error = "Selecciona una maquinaria") }
                    return
                }
                m.id.toLong() to (m.internalIdentificationNumber.ifBlank { m.name })
            }
            "VEHICLE" -> {
                val v = state.selectedVehiculo
                if (v == null) {
                    _uiState.update { it.copy(error = "Selecciona un vehículo") }
                    return
                }
                v.idVehiculo.toLong() to v.placa
            }
            else -> {
                val mo = state.selectedMoto
                if (mo == null) {
                    _uiState.update { it.copy(error = "Selecciona una motocicleta") }
                    return
                }
                mo.id.toLong() to mo.placa
            }
        }

        val qty = state.quantity.toDoubleOrNull()
        val price = state.pricePerUnit.toDoubleOrNull()
        val odometer = if (!isMachine) state.odometerKm.toDoubleOrNull() else null
        val hours = if (isMachine) state.hourMeter.toDoubleOrNull() else null

        if (qty == null || price == null) {
            _uiState.update { it.copy(error = "Cantidad y precio son obligatorios") }
            return
        }
        if (isMachine && hours == null) {
            _uiState.update { it.copy(error = "El horómetro es obligatorio para maquinaria") }
            return
        }
        if (!isMachine && odometer == null) {
            _uiState.update { it.copy(error = "El odómetro (km) es obligatorio para vehículos y motos") }
            return
        }
        if (state.fuelType.isBlank()) {
            _uiState.update { it.copy(error = "El tipo de combustible es obligatorio") }
            return
        }
        if (state.serviceStation.isBlank()) {
            _uiState.update { it.copy(error = "La estación de servicio es obligatoria") }
            return
        }
        if (state.invoicePhotoPath.isNullOrBlank()) {
            _uiState.update { it.copy(error = "La factura es obligatoria") }
            return
        }

        val litersPerGallon = 3.785411784
        val quantityLiters = if (state.quantityUnit == "GALLONS") qty * litersPerGallon else qty
        val totalCostCalc = qty * price
        val totalActual = state.totalCostActual.toDoubleOrNull()
        val mismatch = if (totalActual != null && totalCostCalc > 0) {
            abs(totalCostCalc - totalActual) / totalCostCalc > 0.01
        } else false

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val entity = FuelLogEntity(
                    syncId = UUID.randomUUID().toString(),
                    assetType = state.assetType,
                    assetId = assetIdLong,
                    assetPlate = assetPlate,
                    fuelDateTime = state.fuelDateTime,
                    odometerKm = odometer,
                    hourMeter = hours,
                    quantity = qty,
                    quantityUnit = state.quantityUnit,
                    quantityLiters = quantityLiters,
                    pricePerUnit = price,
                    totalCostCalculated = totalCostCalc,
                    totalCostActual = totalActual,
                    totalCostMismatch = mismatch,
                    fuelType = state.fuelType,
                    serviceStation = state.serviceStation.ifBlank { null },
                    discountAmount = state.discountAmount.toDoubleOrNull(),
                    voucherNumber = state.voucherNumber.ifBlank { null },
                    notes = state.notes.ifBlank { null },
                    invoicePhotoPath = state.invoicePhotoPath,
                    createdAt = System.currentTimeMillis()
                )
                saveFuelLogLocalUseCase(entity)
                _uiState.update { it.copy(isLoading = false, submissionSuccess = true) }
                Log.d("CombustibleViewModel", "✅ Fuel log saved locally for asset $assetIdLong")
                // Intentar sincronización inmediata; si no hay red, quedará pendiente
                try {
                    syncFuelLogsUseCase()
                    Log.d("CombustibleViewModel", "✅ Fuel log synced to backend")
                } catch (e: Exception) {
                    Log.w("CombustibleViewModel", "⚠️ Sync failed (se reintentará al reconectarse): ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("CombustibleViewModel", "❌ Error saving fuel log", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") }
            }
        }
    }
}
