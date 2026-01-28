# Android AllDebrid

Cliente Android para AllDebrid - torrents/magnets + casting Kodi/DLNA.

## Comandos Rápidos
```bash
./gradlew installDebug    # Build + instalar
./gradlew assembleDebug   # Solo build
adb shell am start -n com.samcod3.alldebrid.debug/com.samcod3.alldebrid.MainActivity
```

## Prompts Efectivos

### Para Bugs
> El casting DLNA falla en Samsung TVs.
> Revisa DeviceRepository.kt → sendToDevice().
> Puede ser el DIDL-Lite metadata.

### Para Features
> Añadir selección de calidad de video.
> Ver patrón de BottomSheet en DownloadCard.kt.

### Para Refactoring
> Centralizar lógica duplicada.
> Seguir patrón de ui/util/FormatUtils.kt.

## Archivos Clave
| Área | Archivo |
|------|---------|
| API | AllDebridApi.kt, AllDebridRepository.kt |
| UI | DownloadsScreen.kt, DownloadCard.kt |
| Casting | DeviceRepository.kt, KodiApi.kt |
| DI | AppModule.kt |

## API v4.1
- Todos los endpoints son **POST**
- `/magnet/status` → lista magnets SIN archivos
- `/magnet/files` → archivos de un magnet específico

## Stack
- Kotlin | Jetpack Compose | Material 3
- MVVM + Clean Architecture
- Hilt | Retrofit | DataStore
- UPnP/SSDP (jupnp)

## Convenciones
- ViewModels: `StateFlow` + `UiState` data class
- Repositorios: `Result<T>`
- Icons: `Icons.Rounded` (direccionales: `AutoMirrored.Rounded`)
- Spacing: Objeto `Spacing` (xs=4, sm=8, md=12, lg=16, xl=20, xxl=24)

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
- **Fase**: Configuración Claude Code completada

### Última Sesión (2026-01-28)
- **CLAUDE.md Global creado** (`~/.claude/CLAUDE.md`):
  - Anti-alucinaciones
  - Consulta info técnica oficial (context7, WebSearch)
  - Plugins y agentes a usar automáticamente
  - Workflow preferido
- **CLAUDE.md Proyecto mejorado**:
  - Estructura más concisa
  - Prompts efectivos arriba
  - Comandos rápidos visibles
- **Statusline personalizado**:
  - `~/.claude/statusline.sh` - directorio (branch) | [Modelo] %usado + alerta

### Tareas Pendientes
<!-- Marcar [x] cuando se complete, agregar nuevas al final -->
- [x] Commitear refactorización UI
- [x] Commitear scan híbrido
- [x] Crear CLAUDE.md global
- [x] Mejorar CLAUDE.md proyecto
- [ ] Verificar que todos los endpoints funcionan correctamente con v4.1
- [ ] Probar flujo completo: listar magnets → ver archivos → reproducir

### En Progreso
<!-- Tarea actual que se estaba trabajando -->
- Nada pendiente, todo commiteado

### Últimos Cambios Importantes
- **CLAUDE.md Global**: Preferencias para todos los proyectos
- **Statusline**: % contexto usado + alerta compact
- **Refactorización UI M3**: Iconos Rounded, Spacing, FormatUtils

### Decisiones Técnicas Recientes
- **Memoria Claude Code**:
  - Global: `~/.claude/CLAUDE.md` (todos proyectos)
  - Proyecto: `./CLAUDE.md` (específico)
  - Local: `./CLAUDE.local.md` (personal, no git)
- **Anti-alucinaciones**: Verificar sintaxis antes de usar APIs
- **Statusline**: Claude Code no soporta alineación derecha

### Próximos Pasos Sugeridos
<!-- Actualizar según la dirección del proyecto -->
- Reiniciar Claude Code y verificar `/memory`
- Testear la app con API v4.1

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
