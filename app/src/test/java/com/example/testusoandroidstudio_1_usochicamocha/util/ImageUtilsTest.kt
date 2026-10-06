package com.example.testusoandroidstudio_1_usochicamocha.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageUtilsTest {

    @Test
    fun `foto de 50 MP se decodifica a un cuarto de su tamaño`() {
        // 8160 / 4 = 2040 ≥ 1600; / 8 = 1020 ya quedaría por debajo.
        assertEquals(4, ImageUtils.calcularInSampleSize(8160, 6144, 1600))
    }

    @Test
    fun `foto de 12 MP vertical usa la mitad`() {
        assertEquals(2, ImageUtils.calcularInSampleSize(3000, 4000, 1600))
    }

    @Test
    fun `foto pequeña no se reduce`() {
        assertEquals(1, ImageUtils.calcularInSampleSize(1200, 800, 1600))
    }
}
