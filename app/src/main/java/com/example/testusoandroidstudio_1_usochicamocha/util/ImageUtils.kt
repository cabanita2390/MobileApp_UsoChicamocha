package com.example.testusoandroidstudio_1_usochicamocha.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Utilidad sin estado (un `object`) para preparar las fotos antes de guardarlas en el
 * equipo y subirlas.
 */
object ImageUtils {

    /** Lado mayor de la foto final: suficiente para ver detalles de una fisura o una placa. */
    internal const val LADO_MAXIMO = 1600
    private const val CALIDAD = 80
    private const val CALIDAD_MINIMA = 50
    private const val BYTES_OBJETIVO = 400 * 1024

    /**
     * Reduce la foto a [LADO_MAXIMO] px de lado mayor y la guarda como JPEG en el caché.
     *
     * Se decodifica ya reducida (`inSampleSize`), nunca a resolución completa: una foto de
     * 50 MP ocupa ~200 MB como bitmap. Todo corre en `Dispatchers.IO` para no congelar la
     * pantalla. Devuelve null si la imagen no se puede leer.
     */
    suspend fun compressAndSaveImage(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return@withContext null

            val opciones = BitmapFactory.Options().apply {
                inSampleSize = calcularInSampleSize(limites.outWidth, limites.outHeight, LADO_MAXIMO)
            }
            val decodificada = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return@withContext null
            val final = escalarALadoMaximo(decodificada, LADO_MAXIMO)

            val salida = ByteArrayOutputStream()
            var calidad = CALIDAD
            do {
                salida.reset()
                final.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
                calidad -= 10
            } while (salida.size() > BYTES_OBJETIVO && calidad >= CALIDAD_MINIMA)
            final.recycle()

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            file.writeBytes(salida.toByteArray())
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("ImageUtils", "No se pudo preparar la foto $uri", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e("ImageUtils", "Sin memoria para preparar la foto $uri", e)
            null
        }
    }

    /** Mayor potencia de 2 que deja el lado mayor decodificado en al menos [ladoMaximo]. */
    internal fun calcularInSampleSize(ancho: Int, alto: Int, ladoMaximo: Int): Int {
        var muestra = 1
        while (maxOf(ancho, alto) / (muestra * 2) >= ladoMaximo) {
            muestra *= 2
        }
        return muestra
    }

    private fun escalarALadoMaximo(bitmap: Bitmap, ladoMaximo: Int): Bitmap {
        val mayor = maxOf(bitmap.width, bitmap.height)
        if (mayor <= ladoMaximo) return bitmap
        val factor = ladoMaximo.toFloat() / mayor
        val escalada = Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * factor).toInt(), (bitmap.height * factor).toInt(), true
        )
        if (escalada !== bitmap) bitmap.recycle()
        return escalada
    }
}
