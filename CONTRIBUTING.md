# Convenciones de código — MobileApp_UsoChicamocha

- **Tamaño de archivo**: 500-600 líneas es la señal de alerta para dividir una pantalla/ViewModel, no una ley. El criterio real es una responsabilidad por archivo. Una función > 40-50 líneas es candidata a extraer.
- **Anti-duplicación**: si dos pantallas/ViewModels comparten más del 50% del código (p. ej. `Vehiculo*` vs `Moto*`), se extrae un composable/clase genérica ANTES de copiar.
- **Prohibido hardcodear URLs, credenciales o secretos**: la URL del backend sale de `BuildConfig.BASE_URL` (definida por buildType en `build.gradle.kts`), nunca de una constante fija comentada/descomentada a mano en el código Kotlin.
- **Offline-first**: toda escritura pasa primero por Room; la sincronización con el backend usa UUID + `isSyncing` para evitar duplicados.
- **Tests**: se actualizan en el mismo PR que cambia el comportamiento. No se mergea con la suite en rojo (`./gradlew testDebugUnitTest`).
