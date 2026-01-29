# Android AllDebrid

Cliente Android para AllDebrid - torrents/magnets + casting Kodi/DLNA.

## Documentación Claude Code

Las instrucciones están organizadas en `.claude/`:

```
.claude/
├── CLAUDE.md       # Principal (comandos, stack, prompts)
├── commands/       # Skills: /continue-dev, /save-dev
└── rules/          # Reglas condicionales por área
    ├── api.md      # API AllDebrid v4.1
    ├── ui.md       # Compose, Material 3, ViewModels
    ├── casting.md  # DLNA, Kodi, UPnP
    └── session.md  # Estado de sesión
```

## Quick Reference (fallback)

```bash
./gradlew installDebug    # Build + instalar
```

- **Stack**: Kotlin, Compose, Hilt, Retrofit, jupnp
- **API**: v4.1, todos POST, `/magnet/status` (sin archivos), `/magnet/files` (archivos)
- **Icons**: `Icons.Rounded`, direccionales `AutoMirrored.Rounded`
- **Spacing**: `Spacing.xs/sm/md/lg/xl/xxl` (4/8/12/16/20/24)
