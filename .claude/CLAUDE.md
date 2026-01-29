# Android AllDebrid

Cliente Android para AllDebrid - torrents/magnets + casting Kodi/DLNA.

## Comandos Rápidos
```bash
./gradlew installDebug    # Build + instalar
./gradlew assembleDebug   # Solo build
adb shell am start -n com.samcod3.alldebrid.debug/com.samcod3.alldebrid.MainActivity
```

## Stack
- Kotlin | Jetpack Compose | Material 3
- MVVM + Clean Architecture
- Hilt | Retrofit | DataStore
- UPnP/SSDP (jupnp)

## Archivos Clave
| Área | Archivo |
|------|---------|
| API | AllDebridApi.kt, AllDebridRepository.kt |
| UI | DownloadsScreen.kt, DownloadCard.kt |
| Casting | DeviceRepository.kt, KodiApi.kt |
| DI | AppModule.kt |

## Prompts Efectivos

### Para Bugs
> El casting DLNA falla en Samsung TVs.
> Revisa DeviceRepository.kt -> sendToDevice().
> Puede ser el DIDL-Lite metadata.

### Para Features
> Añadir selección de calidad de video.
> Ver patrón de BottomSheet en DownloadCard.kt.

### Para Refactoring
> Centralizar lógica duplicada.
> Seguir patrón de ui/util/FormatUtils.kt.
