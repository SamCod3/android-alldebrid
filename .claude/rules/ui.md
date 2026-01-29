---
paths:
  - "**/ui/**/*.kt"
---

# UI - Jetpack Compose + Material 3

## ViewModels
```kotlin
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

class FeatureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
}
```

## Icons
- Usar `Icons.Rounded.*` para todos los iconos
- Direccionales: `Icons.AutoMirrored.Rounded.*` (flechas, chevrons)

## Spacing
Usar objeto `Spacing` en vez de valores hardcodeados:
- `Spacing.xs` = 4.dp
- `Spacing.sm` = 8.dp
- `Spacing.md` = 12.dp
- `Spacing.lg` = 16.dp
- `Spacing.xl` = 20.dp
- `Spacing.xxl` = 24.dp

## Formateo
Usar `FormatUtils` para:
- `formatFileSize(bytes)` - "1.5 GB"
- `formatDuration(seconds)` - "2h 30m"
- `formatDate(timestamp)` - Fecha localizada

## Componentes Reutilizables
- `LoadingIndicator` - Spinner centrado
- `ErrorMessage` - Mensaje con retry
- `EmptyState` - Estado vacío con icono
