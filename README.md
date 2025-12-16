# Android AllDebrid Manager

Native Android application for managing AllDebrid downloads with device casting support.

## Features

- 🔐 **AllDebrid Integration** - Manage your magnets/torrents directly
- 🔍 **Jackett Search** - Search for torrents across multiple trackers
- 📺 **Device Discovery** - Find Kodi and DLNA devices on your network
- 📡 **Casting** - Stream unlocked content to your TV/media player

## Screenshots

*Coming soon*

## Requirements

- Android 8.0+ (API 26)
- AllDebrid Premium account
- (Optional) Jackett server for torrent search
- (Optional) Kodi/DLNA device for casting

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Clean Architecture
- **Networking:** Retrofit + OkHttp
- **DI:** Hilt
- **Storage:** DataStore Preferences
- **Device Discovery:** SSDP + JSON-RPC

## Building

```bash
# Clone the repository
git clone https://github.com/SamCod3/android-alldebrid.git
cd android-alldebrid

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/samcod3/alldebrid/
├── data/
│   ├── api/           # Retrofit API interfaces
│   ├── model/         # Data models
│   ├── repository/    # Data repositories
│   └── datastore/     # Settings persistence
├── discovery/         # Device discovery (SSDP/Kodi)
├── di/               # Hilt dependency injection
└── ui/
    ├── components/   # Reusable UI components
    ├── navigation/   # Navigation graph
    ├── screens/      # App screens
    └── theme/        # Material theme
```

## Configuration

### AllDebrid API Key

1. Go to [AllDebrid API Keys](https://alldebrid.com/apikeys/)
2. Create a new API key
3. Enter the key in Settings

### Jackett (Optional)

1. Install Jackett on your server
2. Configure indexers
3. Enter Jackett URL and API key in Settings

## License

MIT License - See [LICENSE](LICENSE) for details.

## Author

**SamCod3** - [GitHub](https://github.com/SamCod3)

---

*This is a companion app for the [edge-alldebrid-manager](https://github.com/SamCod3/edge-alldebrid-manager) Chrome extension.*
