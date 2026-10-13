package com.example.testusoandroidstudio_1_usochicamocha.util

/**
 * El log HTTP (solo en debug) escribía en logcat la contraseña del login y los tokens de la
 * respuesta. Cualquier app con acceso a logs, un reporte de error o un cable USB los exponía.
 */
private val CAMPOS_SECRETOS = Regex(
    "(\"(?:password|jwt|accessToken|refreshToken|token)\"\\s*:\\s*)\"[^\"]*\"",
    RegexOption.IGNORE_CASE
)
private val BEARER = Regex("(Bearer\\s+)[A-Za-z0-9._\\-]+")

fun redactarSecretos(mensaje: String): String =
    mensaje.replace(CAMPOS_SECRETOS, "$1\"***\"").replace(BEARER, "$1***")
