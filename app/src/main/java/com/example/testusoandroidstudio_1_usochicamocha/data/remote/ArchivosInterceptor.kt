package com.example.testusoandroidstudio_1_usochicamocha.data.remote

import com.example.testusoandroidstudio_1_usochicamocha.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * /uploads del backend ya no es público: las imágenes (evidencias, SOAT, licencias…) se piden
 * con la credencial de archivos (?k=) que entregan el login y el refresh. Va en el cargador de
 * imágenes (Coil) para cubrir todas las pantallas sin tocar cada una.
 */
class ArchivosInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!esDeArchivos(request.url)) return chain.proceed(request)
        val k = runBlocking { tokenManager.getArchivosToken().first() } ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().url(conCredencial(request.url, k)).build())
    }

    companion object {
        fun esDeArchivos(url: HttpUrl): Boolean = url.encodedPath.startsWith("/uploads/")

        fun conCredencial(url: HttpUrl, k: String): HttpUrl =
            url.newBuilder().removeAllQueryParameters("k").addQueryParameter("k", k).build()
    }
}
