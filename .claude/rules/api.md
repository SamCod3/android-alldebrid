---
paths:
  - "**/api/**/*.kt"
  - "**/repository/**/*.kt"
  - "**/model/**/*.kt"
---

# API AllDebrid v4.1

## Endpoints
- Todos los endpoints son **POST**
- Base URL: `https://api.alldebrid.com/v4/`
- Auth: `apikey` como parámetro

## Magnets
- `/magnet/status` - Lista magnets **SIN archivos** (solo metadata)
- `/magnet/files` - Archivos de un magnet específico (requiere `id[]`)
- `/magnet/upload` - Subir magnet/torrent
- `/magnet/delete` - Eliminar magnet

## Links
- `/link/unlock` - Desbloquear link premium
- `/link/streaming` - Obtener links de streaming

## Patrón Repository
```kotlin
suspend fun getData(): Result<T> {
    return try {
        val response = api.endpoint()
        if (response.status == "success") {
            Result.success(response.data)
        } else {
            Result.failure(ApiException(response.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Errores Comunes
- `AUTH_BAD_APIKEY` - API key inválida
- `MAGNET_INVALID` - Magnet mal formado
- `MAGNET_MUST_BE_PREMIUM` - Requiere cuenta premium
