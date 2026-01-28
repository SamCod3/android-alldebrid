# CLAUDE.md - Android AllDebrid

## Proyecto
Cliente Android para AllDebrid - gestiona descargas de torrents/magnets y reproduce en Kodi/DLNA.

## Stack
- Kotlin 100% | Jetpack Compose | Material 3
- MVVM + Clean Architecture
- Hilt (DI) | Retrofit + OkHttp | DataStore
- UPnP/SSDP (jupnp) para descubrimiento de dispositivos

## Estructura Principal
```
app/src/main/java/com/samcod3/alldebrid/
├── data/
│   ├── api/           # AllDebridApi, KodiApi, JackettApi, DashboardApi
│   ├── model/         # Magnet, Device, Link, SearchResult, User
│   ├── repository/    # AllDebridRepository, DeviceRepository, JackettRepository
│   └── datastore/     # SettingsDataStore
├── discovery/         # DeviceDiscoveryManager (SSDP)
├── di/                # AppModule (Hilt)
└── ui/
    ├── screens/       # downloads/, search/, devices/, settings/, login/
    ├── components/    # DownloadCard, DeviceItem, SearchResultItem
    └── theme/         # Theme, Color, Type, Shapes
```

## API AllDebrid (v4/v4.1)
| Endpoint | Método | Uso |
|----------|--------|-----|
| `/user` | GET | Info usuario |
| `/magnet/status` (v4.1) | POST | Lista magnets (SIN archivos) |
| `/magnet/files` | POST | Archivos de un magnet |
| `/magnet/upload` | POST | Subir magnet/URL |
| `/magnet/upload/file` | POST multipart | Subir .torrent |
| `/magnet/delete` | POST | Eliminar magnet |
| `/link/unlock` | POST | Desbloquear link premium |

## Modelos Clave
- `Magnet`: id, filename, size, status, statusCode, downloaded, downloadSpeed
- `FileNode`: n (name), s (size), l (link), e (children) - estructura anidada
- `FlatFile`: filename, path, size, link - versión aplanada para UI
- `Device`: id, name, address, port, type (KODI/DLNA), controlUrl

## Casting
- **Kodi**: JSON-RPC a `/jsonrpc` → Player.Open, Playlist.Add
- **DLNA**: SOAP a `/upnp/control/AVTransport1` → SetAVTransportURI + Play
  - Requiere DIDL-Lite metadata para Samsung TVs

## Errores Especiales
- `IpAuthorizationRequiredException`: IP nueva/VPN detectada → navegar a IpAuthorizationScreen
- Códigos: AUTH_BLOCKED, NO_SERVER, AUTH_BAD_APIKEY

## Comandos de Build
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Instalar en dispositivo
./gradlew clean                  # Limpiar build
```

## Archivos Críticos para Cambios
- `AllDebridApi.kt` - Endpoints de la API
- `AllDebridRepository.kt` - Lógica de negocio principal
- `DeviceRepository.kt` - Casting y descubrimiento
- `DownloadsViewModel.kt` - Estado de pantalla principal
- `DownloadsScreen.kt` - UI principal
- `AppModule.kt` - Configuración de Hilt/Retrofit

---

## /continue-dev

Sistema para mantener continuidad entre sesiones de desarrollo.

**Cuando el usuario escriba `/continue-dev`, Claude debe:**
1. Leer esta sección completa
2. Revisar el estado actual del branch con `git status` y `git log -3`
3. Resumir al usuario: qué se estaba haciendo, qué falta, próximos pasos
4. Preguntar si continuar con las tareas pendientes o iniciar algo nuevo

---

### Estado Actual
- **Branch**: `dev-ui`
- **Fase**: UI refinements + Swipe-to-delete

### Última Sesión (2026-01-28)
- **Swipe-to-delete implementado**:
  - Nuevo componente `SwipeToDeleteContainer` en `ui/components/SwipeToDelete.kt`
  - Usa `AnchoredDraggableState` con porcentaje configurable (50% del ancho)
  - Patrón parent-controlled: `isRevealed`/`onRevealChange` para coordinar múltiples items
  - `derivedStateOf` para observar cambios en el offset del swipe
  - `anchoredDrag { dragTo(0f) }` para cerrar programáticamente (settle() no funcionaba)
- **Tap-outside-to-close**:
  - Scrim invisible con `pointerInput` + `detectTapGestures`
  - IMPORTANTE: El scrim debe declararse ANTES del contenido (sin zIndex) para que el contenido reciba toques primero
  - En DownloadsScreen cubre filtro+chips+lista
  - En DevicesScreen cubre solo el área del listado
- **Fix DevicesScreen**: El scrim tenía `zIndex(1f)` que bloqueaba el botón "Eliminar"

### Tareas Pendientes
<!-- Marcar [x] cuando se complete, agregar nuevas al final -->
- [ ] Verificar que todos los endpoints funcionan correctamente con v4.1
- [ ] Probar flujo completo: listar magnets → ver archivos → reproducir
- [ ] Revisar configuración MikroTik para SSDP/multicast (no detecta dispositivos)

### En Progreso
<!-- Tarea actual que se estaba trabajando -->
- (ninguna)

### Últimos Cambios Importantes
- **Swipe-to-delete**: `SwipeToDelete.kt` (nuevo), `DownloadsScreen.kt`, `DevicesScreen.kt`, `DownloadCard.kt`
- **Login/API Keys**: `ApiKeyManagerScreen.kt`, `ApiKeyManagerViewModel.kt`, `WebLoginScreen.kt`, `DashboardApi.kt`
- **Migración API v4.1**: `AllDebridApi.kt`, `Magnet.kt`, `AllDebridRepository.kt`
- Kodi/DLNA playback controls implementados

### Decisiones Técnicas Recientes
- **Swipe-to-delete pattern**:
  - Parent mantiene estado de cuál item está revelado (`revealedId`)
  - Scrim invisible intercepta toques fuera del item revelado
  - Scrim debe estar ANTES del contenido en orden de declaración (NO usar zIndex)
  - Dispositivos se cachean en DataStore y persisten entre sesiones/redes
- **Login flow**: Cookies de WebView son independientes de API key guardada
- **API v4.1**: Todos los endpoints POST, `/magnet/files` separado de `/magnet/status`
- DLNA necesita DIDL-Lite metadata para Samsung TVs
- SSDP puede fallar en algunos routers (MikroTik) - usar scan manual como alternativa

### Próximos Pasos Sugeridos
<!-- Actualizar según la dirección del proyecto -->
- Testear la app completamente con la nueva API
- Configurar MikroTik para permitir multicast/SSDP

---

## /save-dev

Sistema para guardar el estado antes de terminar una sesión.

**Cuando el usuario escriba `/save-dev`, Claude debe:**
1. Ejecutar `git status` y `git log -1` para ver el estado actual
2. Preguntar al usuario:
   - ¿Qué tareas quedaron pendientes?
   - ¿Hay algo en progreso a medias?
   - ¿Alguna decisión técnica importante que recordar?
3. Actualizar automáticamente este CLAUDE.md:
   - "Última Sesión" con fecha y resumen
   - "Tareas Pendientes" con lo que falta
   - "En Progreso" si hay algo a medias
   - "Decisiones Técnicas" si hay nuevas
4. Confirmar que se guardó el estado
5. Sugerir hacer commit si hay cambios sin commitear

---

## Convenciones
- ViewModels usan `StateFlow` + `UiState` data class
- Repositorios retornan `Result<T>`
- Composables reciben ViewModel via `hiltViewModel()`
- Errores de IP lanzan `IpAuthorizationRequiredException`

## Testing
```bash
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
```
