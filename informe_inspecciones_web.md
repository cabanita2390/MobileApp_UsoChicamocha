# Informe Técnico: Análisis de Datos Enviados al Backend en Módulos de Inspección

## Resumen de Endpoints

A continuación, se listan los endpoints de API identificados para el envío de datos de inspección:

- **Inspección Vehicular**: `POST /v1/vehicle-inspection`
  - Utiliza el DTO `VehiculoInspectionRequest` en el cuerpo de la petición.
  
- **Inspección de Motos**: `POST /v1/moto/inspeccion`
  - Utiliza el DTO `InspeccionMotoRequest` en el cuerpo de la petición.

Ambos endpoints utilizan Retrofit con anotaciones `@POST` y `@Body` para enviar los datos en formato JSON.

## Esquema de Datos (JSON Payload)

### Inspección Vehicular
```json
{
  "placaVehiculo": "string",
  "marca": "string",
  "tipoVehiculo": "string",
  "kilometrajeReportado": "integer",
  "responsableInspeccion": "string",
  "aprobadoRuta": "boolean",
  "observacionesFinales": "string",
  "nivelAceite": "string",
  "nivelRefrigerante": "string",
  "nivelFrenos": "string",
  "estadoLlantas": "string",
  "lucesGeneral": "string",
  "estadoVisual": "string",
  "limpiezaGeneral": "string",
  "checkSoat": "string",
  "checkTecno": "string",
  "checkLicencia": "string",
  "checkExtintor": "string",
  "vigenciaExtintor": "string",
  "fechaVencSoat": "string",
  "fechaVencTecno": "string",
  "fechaVencLicencia": "string",
  "tieneBotiquin": "boolean",
  "tieneSeñalizacion": "boolean",
  "tieneLineasEmergencia": "boolean",
  "tieneLlantaRepuesto": "boolean",
  "tieneGatoHidraulico": "boolean",
  "saludFisica": "boolean",
  "saludMental": "boolean",
  "sobrio": "boolean",
  "medicamentos": "boolean",
  "conscienteResponsabilidad": "boolean",
  "condicionParaConducir": "boolean"
}
```

### Inspección de Motos
```json
{
  "idVehiculo": "integer",
  "kilometrajeReportado": "integer",
  "estadoVehiculo": "string",
  "observacionesFinales": "string",
  "checkSoat": "string",
  "checkTecno": "string",
  "checkLicencia": "string",
  "checkExtintor": "string",
  "checkNivelAceite": "string",
  "checkEstadoLlantas": "string",
  "checkEstadoLuces": "string",
  "idUbicacion": "integer"
}
```

## Tabla de Referencia

### Inspección Vehicular (`VehiculoInspectionRequest`)

| Campo | Tipo en Kotlin | Descripción / Origen |
|-------|----------------|----------------------|
| `placaVehiculo` | `String` | Placa del vehículo inspeccionado |
| `marca` | `String` | Marca del vehículo (informativo, ya está en BD) |
| `tipoVehiculo` | `String` | Tipo de vehículo (informativo, ya está en BD) |
| `kilometrajeReportado` | `Int` | Kilometraje reportado en la inspección |
| `responsableInspeccion` | `String` | Nombre del responsable de la inspección |
| `aprobadoRuta` | `Boolean` | Indicador de aprobación para ruta |
| `observacionesFinales` | `String` | Observaciones finales de la inspección |
| `nivelAceite` | `String` | Nivel de aceite (parte de inspección mecánica) |
| `nivelRefrigerante` | `String` | Nivel de refrigerante (parte de inspección mecánica) |
| `nivelFrenos` | `String` | Nivel de frenos (parte de inspección mecánica) |
| `estadoLlantas` | `String` | Estado de las llantas (parte de inspección mecánica) |
| `lucesGeneral` | `String` | Estado general de las luces (parte de inspección mecánica) |
| `estadoVisual` | `String` | Estado visual general (parte de inspección mecánica) |
| `limpiezaGeneral` | `String` | Estado de limpieza general (parte de inspección mecánica) |
| `checkSoat` | `String` | Estado del SOAT (parte de documentos) |
| `checkTecno` | `String` | Estado de la tecnomecánica (parte de documentos) |
| `checkLicencia` | `String` | Estado de la licencia (parte de documentos) |
| `checkExtintor` | `String` | Estado del extintor (parte de documentos) |
| `vigenciaExtintor` | `String` | Vigencia del extintor en formato "YYYY-MM" |
| `fechaVencSoat` | `String` | Fecha de vencimiento del SOAT en formato "YYYY-MM-DD" |
| `fechaVencTecno` | `String` | Fecha de vencimiento de la tecnomecánica en formato "YYYY-MM-DD" |
| `fechaVencLicencia` | `String` | Fecha de vencimiento de la licencia en formato "YYYY-MM-DD" |
| `tieneBotiquin` | `Boolean` | Indicador de presencia de botiquín (parte de elementos) |
| `tieneSeñalizacion` | `Boolean` | Indicador de presencia de señalización (serializado como "tieneSeñalizacion") |
| `tieneLineasEmergencia` | `Boolean` | Indicador de presencia de líneas de emergencia |
| `tieneLlantaRepuesto` | `Boolean` | Indicador de presencia de llanta de repuesto |
| `tieneGatoHidraulico` | `Boolean` | Indicador de presencia de gato hidráulico |
| `saludFisica` | `Boolean` | Estado de salud física del conductor |
| `saludMental` | `Boolean` | Estado de salud mental del conductor |
| `sobrio` | `Boolean` | Indicador de sobriedad del conductor |
| `medicamentos` | `Boolean` | Indicador de consumo de medicamentos |
| `conscienteResponsabilidad` | `Boolean` | Indicador de conciencia de responsabilidad |
| `condicionParaConducir` | `Boolean` | Condición general para conducir |

### Inspección de Motos (`InspeccionMotoRequest`)

| Campo | Tipo en Kotlin | Descripción / Origen |
|-------|----------------|----------------------|
| `idVehiculo` | `Int` | ID del vehículo (moto) en la base de datos |
| `kilometrajeReportado` | `Int` | Kilometraje reportado en la inspección |
| `estadoVehiculo` | `String` | Estado general del vehículo |
| `observacionesFinales` | `String` | Observaciones finales de la inspección |
| `checkSoat` | `String` | Estado del SOAT |
| `checkTecno` | `String` | Estado de la tecnomecánica |
| `checkLicencia` | `String` | Estado de la licencia |
| `checkExtintor` | `String` | Estado del extintor |
| `checkNivelAceite` | `String` | Estado del nivel de aceite (Bueno / Regular / Malo) |
| `checkEstadoLlantas` | `String` | Estado de las llantas (Bueno / Regular / Malo) |
| `checkEstadoLuces` | `String` | Estado de las luces (Bueno / Regular / Malo) |
| `idUbicacion` | `Int` | ID de la ubicación de la inspección |

## Diferencias

La estructura de datos entre la inspección vehicular y la de motos presenta diferencias significativas en términos de complejidad y campos incluidos:

- **Campos comunes**: Ambos incluyen `kilometrajeReportado`, `observacionesFinales`, y checks de documentos (`checkSoat`, `checkTecno`, `checkLicencia`, `checkExtintor`).

- **Inspección Vehicular (más detallada)**: Incluye campos adicionales como `placaVehiculo`, `marca`, `tipoVehiculo`, `responsableInspeccion`, `aprobadoRuta`, múltiples niveles mecánicos (`nivelAceite`, `nivelRefrigerante`, etc.), fechas de vencimiento específicas, elementos de seguridad (`tieneBotiquin`, `tieneSeñalizacion`, etc.), y evaluación completa de salud del conductor. Total: 32 campos.

- **Inspección de Motos (más simplificada)**: Utiliza `idVehiculo` en lugar de placa, incluye `estadoVehiculo` y `idUbicacion`, y tiene checks mecánicos más limitados (solo aceite, llantas y luces con estados Bueno/Regular/Malo). No incluye evaluación de salud ni elementos detallados. Total: 12 campos.

La inspección vehicular es más exhaustiva y orientada a vehículos pesados o comerciales, mientras que la de motos es más básica y enfocada en aspectos esenciales de seguridad y mecánica. Para asegurar paridad en una aplicación web, se debe implementar lógica condicional para manejar estos esquemas diferentes según el tipo de vehículo.

## Configuración de API para Consumo Web

### URL Base
- **Base URL**: `https://server.usochicamocha.co/api/`
- Todos los endpoints son relativos a esta URL.

### Autenticación
- **Tipo**: JWT (Bearer Tokens).
- **Login Inicial**: `POST /v1/auth/login` con `{"email": "string", "password": "string"}` → Devuelve `accessToken` y `refreshToken`.
- **Refresh Token**: `POST /v1/auth/token/refresh` con `{"refreshToken": "string"}` → Devuelve nuevo `accessToken`.
- **Header para Peticiones Protegidas**: `Authorization: Bearer <access_token>`.
- **Nota**: Endpoints de login/refresh no requieren Authorization. Manejar expiración automática (401) refrescando tokens.

### Headers Comunes
- `Content-Type: application/json`
- `Authorization: Bearer <token>` (excepto login/refresh)

### Respuestas Esperadas
- **Inspección Vehicular** (`POST /v1/vehicle-inspection`):
  - **Éxito (200)**:
    ```json
    {
      "idInspeccion": 123,
      "mensaje": "Inspección vehicular guardada exitosamente"
    }
    ```
  - **Errores**: 401 (no autorizado), 400 (datos inválidos), 500 (error servidor).
- **Inspección de Motos** (`POST /v1/moto/inspeccion`):
  - **Éxito (200)**: Devuelve el ID de la inspección como `Long` (ej. `456`).
  - **Errores**: Iguales a vehicular.

### Ejemplo de Consumo en JavaScript (Fetch)
```javascript
// Inspección Vehicular
async function enviarInspeccionVehicular(datos) {
  const token = localStorage.getItem('access_token');
  const response = await fetch('https://server.usochicamocha.co/api/v1/vehicle-inspection', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(datos)
  });
  if (response.ok) {
    return await response.json(); // {idInspeccion, mensaje}
  } else if (response.status === 401) {
    // Refrescar token y reintentar
  }
}
```