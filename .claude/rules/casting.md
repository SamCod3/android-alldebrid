---
paths:
  - "**/discovery/**/*.kt"
  - "**/kodi/**/*.kt"
  - "**/device/**/*.kt"
---

# Casting - DLNA/UPnP y Kodi

## Device Discovery
- **SSDP** para descubrir dispositivos UPnP/DLNA
- **Kodi** detectado por puerto 8080 (JSON-RPC)
- Scan híbrido: Kodi + SSDP en paralelo

## jupnp (UPnP/DLNA)
```kotlin
// Servicio de control
val service = device.findService(ServiceId("AVTransport"))
val action = service.getAction("SetAVTransportURI")
```

## DIDL-Lite Metadata
Requerido para Samsung TVs y algunos dispositivos:
```xml
<DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/">
  <item>
    <dc:title>Video Title</dc:title>
    <res protocolInfo="http-get:*:video/mp4:*">URL</res>
  </item>
</DIDL-Lite>
```

## Kodi JSON-RPC
```kotlin
// Player.Open
val request = KodiRequest(
    method = "Player.Open",
    params = mapOf("item" to mapOf("file" to url))
)
```

## Errores Comunes
- DLNA timeout: Algunos TVs tardan en responder
- Kodi auth: Verificar que JSON-RPC está habilitado
- Samsung: Requiere DIDL-Lite bien formado
