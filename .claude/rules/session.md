# Session State

Sistema para mantener continuidad entre sesiones de desarrollo.

---

## /continue-dev

**Cuando el usuario escriba `/continue-dev`, Claude debe:**
1. Leer esta sección completa
2. Revisar el estado actual del branch con `git status` y `git log -3`
3. Resumir al usuario: qué se estaba haciendo, qué falta, próximos pasos
4. Preguntar si continuar con las tareas pendientes o iniciar algo nuevo

---

## /save-dev

**Cuando el usuario escriba `/save-dev`, Claude debe:**
1. Ejecutar `git status` y `git log -1` para ver el estado actual
2. Preguntar al usuario:
   - ¿Qué tareas quedaron pendientes?
   - ¿Hay algo en progreso a medias?
   - ¿Alguna decisión técnica importante que recordar?
3. Actualizar automáticamente este archivo:
   - "Última Sesión" con fecha y resumen
   - "Tareas Pendientes" con lo que falta
   - "En Progreso" si hay algo a medias
   - "Decisiones Técnicas" si hay nuevas
4. Confirmar que se guardó el estado
5. Sugerir hacer commit si hay cambios sin commitear

---

## Estado Actual
- **Branch**: `dev-ui`
- **Fase**: Configuración Claude Code completada

### Última Sesión (2026-01-29)
- Reorganización CLAUDE.md en `.claude/rules/` con reglas condicionales por path
- Statusline corregido: tokens calculados desde % (ej: 41% 82k/200k)
- CLAUDE.md raíz como índice + fallback

### Tareas Pendientes
- [ ] Verificar que todos los endpoints funcionan correctamente con v4.1
- [ ] Probar flujo completo: listar magnets -> ver archivos -> reproducir

### En Progreso
- Nada pendiente, todo commiteado

### Decisiones Técnicas Recientes
- **Estructura memoria**: `.claude/rules/*.md` con paths condicionales
- **Statusline**: tokens calculados como `% × context_size` (no total_input_tokens)
- **CLAUDE.md raíz**: índice que explica estructura + fallback con info esencial
