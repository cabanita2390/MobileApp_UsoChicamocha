package com.example.testusoandroidstudio_1_usochicamocha.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
     * Reduce la foto a [LADO_MAXIMO] px de lado mayor, la endereza según su orientación
     * EXIF y la guarda como JPEG en el caché.
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
            // El JPEG que se escribe no lleva EXIF: sin rotar aquí, una foto tomada con el
            // teléfono vertical llega acostada a la web.
            val orientacion = context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            val final = enderezar(escalarALadoMaximo(decodificada, LADO_MAXIMO), orientacion)

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

    /** Grados a rotar según la etiqueta EXIF Orientation (solo rotaciones; los espejos no se dan en cámaras). */
    internal fun gradosDeOrientacion(orientacion: Int): Float = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270f
        else -> 0f
    }

    private fun enderezar(bitmap: Bitmap, orientacion: Int): Bitmap {
        val grados = gradosDeOrientacion(orientacion)
        if (grados == 0f) return bitmap
        val rotada = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true
        )
        if (rotada !== bitmap) bitmap.recycle()
        return rotada
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
