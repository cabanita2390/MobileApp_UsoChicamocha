# UsoChicamocha — App Móvil

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/minSdk%2024-targetSdk%2035-3DDC84?logo=android&logoColor=white)

App Android de campo del sistema de gestión de flota y maquinaria pesada de **Distrito de Riego Usochicamocha** (Colombia). Offline-first: los inspectores registran inspecciones, cambios de aceite y datos de vehículos/motos/maquinaria sin necesidad de conexión, y la app sincroniza contra el [backend](https://github.com/cabanita2390/UsochimochaBackend) apenas hay señal.

## Tabla de contenido

- [Stack técnico](#stack-técnico)
- [Arquitectura](#arquitectura)
- [Prerequisitos](#prerequisitos)
- [Configuración](#configuración)
- [Compilar y ejecutar](#compilar-y-ejecutar)
- [Testing](#testing)
- [Distribución](#distribución)

## Stack técnico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.0 |
| UI | Jetpack Compose (BOM 2024.09) + Material 3 + Navigation Compose |
| Inyección de dependencias | Hilt |
| Persistencia local | Room (offline-first — toda escritura pasa primero por acá) |
| Red | Retrofit + OkHttp (logging interceptor) + Gson |
| Trabajo en segundo plano | WorkManager (sincronización diferida sin conexión) |
| Preferencias | DataStore |
| Imágenes | Coil |
| Tests | JUnit + MockK + Room Testing; instrumentados con Espresso/Compose UI Test |

## Arquitectura

MVVM + Hilt, offline-first:

```
data/local/       # Room: entidades, DAOs, AppDataBase.kt (versión 40, migraciones manuales)
data/remote/      # Retrofit — interceptor JWT + authenticator de refresh de token
data/repository/  # Sincroniza lo local con el backend (idempotencia vía UUID + lock isSyncing)
di/               # Módulos Hilt: AppModule, DatabaseModule, DataStoreModule, NetworkModule, RepositoryModule, UseCaseModule
domain/           # Modelos y casos de uso
ui/               # Un paquete por dominio: vehiculo/, motocicleta/, mantenimiento/, home/, login/, form/, shared/, theme/, etc.
util/             # NetworkMonitor (detecta conectividad), TokenRefreshMonitor, JwtUtils, ImageUtils, AppLogger
```

`util/NetworkMonitor` decide si algo se sincroniza de inmediato o se difiere con WorkManager para cuando vuelva la conexión. La navegación (`NavHost`) vive directo en `MainActivity.kt` — no hay un paquete de navegación separado.

**Sincronización en segundo plano** (`data/workers/`, orquestada desde `MyApplication` y `domain/usecase/LocalSyncCoordinator.kt`):
- `SyncDataWorker`: cada 15 min (y una vez al abrir la app), si hay conexión — sincroniza formularios/inspecciones/mantenimientos pendientes y catálogos maestros (vehículos, motos, máquinas, aceites, ubicaciones, documentos); al terminar encola `ImagenSyncWorker`.
- `ImagenSyncWorker`: sube las imágenes asociadas a los registros ya sincronizados.
- `CleanupWorker`: cada 24h (no requiere red) — resetea locks de formularios que quedaron colgados en `isSyncing = 1` por una sincronización interrumpida.

**Permisos** (`AndroidManifest.xml`): `INTERNET`, `ACCESS_NETWORK_STATE`, `CAMERA`, `READ_MEDIA_IMAGES` — la app toma fotos directamente para inspecciones/documentos.

## Prerequisitos

- Android Studio (o JDK 17 + Gradle vía línea de comandos)
- Un dispositivo/emulador con API 24+ (Android 7.0+)

## Configuración

La URL base del backend está fijada en el `buildConfigField` de cada build type, en `app/build.gradle.kts` — no hay un `.env`:

| Build type | `BASE_URL` |
|---|---|
| `debug` | `https://back-test.usochicamocha.co/api/` (comentada al lado, la alternativa para emulador local: `http://10.0.2.2:8080/api/`) |
| `release` | Ver `app/build.gradle.kts` |

El firmado (`debugFixed`, usado tanto en `debug` como en `release`) usa el keystore versionado `app/usochicamocha-debug.keystore` — las credenciales están en el propio `build.gradle.kts`. Esto simplifica compartir builds entre el equipo, pero significa que el keystore no es privado; no usar este `.gradle.kts` como referencia para firmar un release que vaya a la Play Store.

## Compilar y ejecutar

```bash
./gradlew build                    # Build completo
./gradlew assembleDebug             # APK debug
./gradlew assembleRelease           # APK release (mismo keystore que debug, ver arriba)
```

O directamente desde Android Studio: **Run ▸ Run 'app'**.

## Testing

```bash
./gradlew test                      # Unit tests (JVM, JUnit + MockK)
./gradlew connectedAndroidTest       # Tests instrumentados — requiere un dispositivo/emulador conectado
```

Los instrumentados (`app/src/androidTest/`) son de flujo completo: `HappyPathE2ETest`, `MotoHappyPathTest`, `FullAuditTest`.

## Distribución

No hay un pipeline de CI/CD ni distribución automatizada — el APK se comparte manualmente con el equipo tras compilarlo (`assembleDebug`/`assembleRelease`). Si en algún momento se necesita automatizar (build + firma + distribución a testers, p. ej. con Firebase App Distribution), ese es el momento de agregar un workflow — hoy no existe uno porque no había nada real que disparar con él.
