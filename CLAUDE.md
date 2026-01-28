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
- **Fase**: Refactorización UI Material 3

### Última Sesión (2026-01-28)
- **Refactorización UI completa (3 fases)**:
  - Fase 1: Iconos unificados a `Icons.Rounded` (DeviceItem, SettingsScreen, ApiKeyManagerScreen)
  - Fase 2: Creado `FormatUtils.kt`, corregido bug doble botón en SearchResultItem, fix fontWeight
  - Fase 3: Creado `Spacing.kt` con escala estandarizada (xs=4, sm=8, md=12, lg=16, xl=20, xxl=24)
- **Archivos nuevos**:
  - `ui/util/FormatUtils.kt` - función `formatSize()` centralizada
  - `ui/theme/Spacing.kt` - escala de spacing consistente
- **Iconos AutoMirrored**: Login/Logout usan `Icons.AutoMirrored.Rounded` para soporte RTL

### Tareas Pendientes
<!-- Marcar [x] cuando se complete, agregar nuevas al final -->
- [ ] Commitear refactorización UI (9 archivos modificados, 2 nuevos)
- [ ] Commitear scan híbrido (DeviceDiscoveryManager, DeviceRepository, DevicesViewModel)
- [ ] Verificar que todos los endpoints funcionan correctamente con v4.1
- [ ] Probar flujo completo: listar magnets → ver archivos → reproducir

### En Progreso
<!-- Tarea actual que se estaba trabajando -->
- **Refactorización UI (sin commit)**: 9 archivos modificados + 2 nuevos
  - Componentes: DeviceItem, DownloadCard, SearchResultItem
  - Screens: DevicesScreen, SettingsScreen, ApiKeyManagerScreen
  - Theme: Spacing.kt (nuevo)
  - Utils: FormatUtils.kt (nuevo)
- **Scan híbrido (sin commit)**: DeviceDiscoveryManager.kt, DeviceRepository.kt, DevicesViewModel.kt

### Últimos Cambios Importantes
- **Refactorización UI M3**: Iconos Rounded, Spacing consistente, FormatUtils centralizado
- **Bug fix**: Doble botón anidado en SearchResultItem (IconButton dentro de FilledIconButton)
- **Swipe-to-delete**: `SwipeToDelete.kt`, `DownloadsScreen.kt`, `DevicesScreen.kt`
- **Login/API Keys**: `ApiKeyManagerScreen.kt`, `ApiKeyManagerViewModel.kt`, `WebLoginScreen.kt`

### Decisiones Técnicas Recientes
- **Icons Material 3**:
  - Usar `Icons.Rounded` para consistencia visual
  - Usar `Icons.AutoMirrored.Rounded` para iconos direccionales (Login, Logout, ArrowBack)
  - Mantener `Icons.Outlined` solo para estados unselected en BottomNav
- **Spacing scale**: Basada en 4dp (xs=4, sm=8, md=12, lg=16, xl=20, xxl=24, xxxl=32)
- **formatSize()**: Centralizado en `ui/util/FormatUtils.kt` para evitar duplicación
- **Swipe-to-delete pattern**: Scrim antes del contenido, sin zIndex

### Próximos Pasos Sugeridos
<!-- Actualizar según la dirección del proyecto -->
- Commitear los cambios de UI en 2 commits separados (UI refactor + scan híbrido)
- Extender Spacing a más componentes gradualmente
- Testear la app completamente

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
