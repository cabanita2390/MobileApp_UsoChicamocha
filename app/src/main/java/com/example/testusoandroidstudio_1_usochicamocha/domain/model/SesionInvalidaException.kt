package com.example.testusoandroidstudio_1_usochicamocha.domain.model

/**
 * La sesión terminó de verdad: el refresh token venció o el servidor lo rechazó (401/403).
 * Es el ÚNICO caso en que se cierra la sesión. Un fallo de red al refrescar (sin señal en
 * la estación, backend caído) NO es esto: la sesión se conserva y el técnico sigue
 * trabajando offline hasta que vuelva la señal.
 */
class SesionInvalidaException(message: String = "Sesión expirada.") : Exception(message)
