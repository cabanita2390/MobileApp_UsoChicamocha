package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.DocumentoVehiculoResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.KilometrajeValidacionResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormSyncResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.InspeccionMotoRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.DocumentoExistenteDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.UserDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MachineDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MotoPlacaDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.NewAccessTokenResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.OilDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.RefreshTokenRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.UbicacionDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.VehicleDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.OilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehicleOilChangeRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.VehiculoInspectionRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.ActividadDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.CumplimientoDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionEditRequestDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionRequestDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EjecucionResponseDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EstacionDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.EvidenciaSubestacionDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.PageDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.ProgramacionDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("v1/oil/brand")
    suspend fun getOils(): Response<List<OilDto>>
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("v1/auth/token/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<NewAccessTokenResponse>
    @GET("v1/machine")
    suspend fun getMachines(): Response<List<MachineDto>>
    @GET("v1/vehicle")
    suspend fun getVehicles(): Response<List<VehicleDto>>

    /** Busca un vehículo por placa para autocompletar datos */
    @GET("v1/vehicle/{placa}")
    suspend fun getVehicleByPlaca(
        @Path("placa") placa: String
    ): Response<VehicleDto>
    @POST("v1/inspection")
    suspend fun syncForm(@Body form: FormDto): Response<FormSyncResponse>
    @POST("oil-changes/motor")
    suspend fun syncMotorOilChange(@Body request: OilChangeRequest): Response<Unit>
    @POST("oil-changes/hydraulic")
    suspend fun syncHydraulicOilChange(@Body request: OilChangeRequest): Response<Unit>
    @Multipart
    @POST("v1/inspection/{id}/image")
    suspend fun syncImage(
        @Path("id") formId: Long,
        @Part imagen: MultipartBody.Part
    ): Response<Unit>

    // --- MOTO endpoints ---
    @GET("v1/moto/placas")
    suspend fun getMotocicletas(): Response<List<MotoPlacaDto>>

    @GET("v1/moto/ubicaciones")
    suspend fun getUbicaciones(): Response<List<UbicacionDto>>

    @GET("v1/moto/{placa}/documentos")
    suspend fun getDocumentosByPlaca(@Path("placa") placa: String): Response<List<DocumentoExistenteDto>>

    @POST("v1/moto/inspeccion")
    suspend fun saveInspeccionMoto(@Body request: InspeccionMotoRequest): Response<Long>

    /** Envía la inspección pre-operativa de vehículos al backend */
    @POST("v1/vehicle-inspection")
    suspend fun submitVehiculoInspection(
        @Body request: VehiculoInspectionRequest
    ): Response<com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.VehiculoInspectionResponse>

    /** Cambio de aceite de vehículo liviano (monitoreo / historial aceite). */
    @POST("v1/vehicle/oil-change")
    suspend fun registerVehicleOilChange(@Body request: VehicleOilChangeRequest): Response<Unit>

    /** Cambio de aceite de motocicleta */
    @POST("v1/moto/{placa}/oil-change")
    suspend fun registerMotoOilChange(
        @Path("placa") placa: String,
        @Body request: VehicleOilChangeRequest
    ): Response<Long>

    /** Obtiene el perfil del usuario autenticado (incluye categoría, vencimiento y URL de licencia) */
    @GET("v1/user/me")
    suspend fun getUsuarioActual(): Response<UserDto>

    /** Consulta el estado actual de los documentos de un vehículo por ID */
    @GET("v1/vehicle-inspection/documentos/{idVehiculo}")
    suspend fun getDocumentosVehiculo(
        @Path("idVehiculo") idVehiculo: Int
    ): Response<DocumentoVehiculoResponse>

    /** Valida si el kilometraje ingresado es menor al registrado en BD */
    @GET("v1/vehicle-inspection/validar-kilometraje")
    suspend fun validarKilometraje(
        @retrofit2.http.Query("placa") placa: String,
        @retrofit2.http.Query("kilometraje") kilometraje: Int
    ): Response<KilometrajeValidacionResponse>

    // --- Asset Management endpoints (SUPERVISOR_OPERATIVO) ---
    @POST("v1/vehicle")
    suspend fun createVehicle(@Body request: VehicleDto): Response<VehicleDto>

    @PUT("v1/vehicle/{id}")
    suspend fun updateVehicle(
        @Path("id") id: Long,
        @Body request: VehicleDto
    ): Response<VehicleDto>

    @POST("v1/machine")
    suspend fun createMachine(@Body request: MachineDto): Response<MachineDto>

    @PUT("v1/machine/{id}")
    suspend fun updateMachine(
        @Path("id") id: Long,
        @Body request: MachineDto
    ): Response<MachineDto>

    @POST("v1/moto")
    suspend fun createMoto(@Body request: MotoPlacaDto): Response<MotoPlacaDto>

    @PUT("v1/moto/{id}")
    suspend fun updateMoto(
        @Path("id") id: Long,
        @Body request: MotoPlacaDto
    ): Response<MotoPlacaDto>

    // --- Subestaciones (mantenimiento civil) ---

    @GET("v1/substation/estaciones")
    suspend fun getEstacionesSubestacion(): Response<List<EstacionDto>>

    @GET("v1/substation/actividades")
    suspend fun getActividadesSubestacion(@Query("disciplina") disciplina: String): Response<List<ActividadDto>>

    @GET("v1/substation/programacion")
    suspend fun getProgramacionSubestacion(
        @Query("estacionId") estacionId: Long,
        @Query("anio") anio: Int,
        @Query("mes") mes: Int,
        @Query("disciplina") disciplina: String
    ): Response<List<ProgramacionDto>>

    @POST("v1/substation/ejecuciones")
    suspend fun registrarEjecucionSubestacion(@Body request: EjecucionRequestDto): Response<EjecucionResponseDto>

    @PUT("v1/substation/ejecuciones/{id}")
    suspend fun editarEjecucionSubestacion(
        @Path("id") id: Long,
        @Body request: EjecucionEditRequestDto
    ): Response<EjecucionResponseDto>

    /** El backend espera el nombre de parte "file" (SubstationController.agregarEvidencia), no "imagen". */
    @Multipart
    @POST("v1/substation/ejecuciones/{id}/evidencia")
    suspend fun subirEvidenciaSubestacion(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<EvidenciaSubestacionDto>

    @GET("v1/substation/ejecuciones/{id}")
    suspend fun getEjecucionSubestacion(@Path("id") id: Long): Response<EjecucionResponseDto>

    @GET("v1/substation/ejecuciones/por-programacion/{programacionId}")
    suspend fun getEjecucionPorProgramacion(@Path("programacionId") programacionId: Long): Response<EjecucionResponseDto>

    @GET("v1/substation/indicadores/cumplimiento")
    suspend fun getCumplimientoSubestacion(
        @Query("estacionId") estacionId: Long? = null,
        @Query("anio") anio: Int,
        @Query("mes") mes: Int? = null,
        @Query("disciplina") disciplina: String
    ): Response<List<CumplimientoDto>>

    /**
     * Listado de ejecuciones (SubstationController.listarEjecuciones). estacionId null =
     * todas las estaciones. Usado con esProgramada=false para traer las actividades
     * "no previstas" (sin cita de cronograma) que alimentan la pestaña Realizadas de
     * Pendientes — v_mant_cumplimiento nunca las trae porque arranca desde
     * mant_programacion.
     */
    @GET("v1/substation/ejecuciones")
    suspend fun getEjecucionesSubestacion(
        @Query("estacionId") estacionId: Long? = null,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null,
        @Query("esProgramada") esProgramada: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Response<PageDto<EjecucionResponseDto>>
}