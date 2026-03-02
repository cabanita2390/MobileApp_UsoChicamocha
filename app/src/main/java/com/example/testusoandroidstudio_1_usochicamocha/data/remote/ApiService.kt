package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.FormSyncResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.InspeccionMotoRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.DocumentoExistenteDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.LoginResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MachineDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.MotoPlacaDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.NewAccessTokenResponse
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.OilDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.RefreshTokenRequest
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.dto.UbicacionDto
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.request.OilChangeRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {

    @GET("v1/oil/brand")
    suspend fun getOils(): Response<List<OilDto>>
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("v1/auth/token/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<NewAccessTokenResponse>
    @GET("v1/machine")
    suspend fun getMachines(): Response<List<MachineDto>>
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

    @POST("v1/moto/placas")
    suspend fun registrarPlaca(@Body placa: String): Response<MotoPlacaDto>

    @GET("v1/moto/ubicaciones")
    suspend fun getUbicaciones(): Response<List<UbicacionDto>>

    @GET("v1/moto/{placa}/documentos")
    suspend fun getDocumentosByPlaca(@Path("placa") placa: String): Response<List<DocumentoExistenteDto>>

    @POST("v1/moto/inspeccion")
    suspend fun saveInspeccionMoto(@Body request: InspeccionMotoRequest): Response<Long>
}

