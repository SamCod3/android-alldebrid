---
name: continue-dev
description: Retoma el desarrollo donde lo dejaste
---

# Skill: continue-dev

Retoma el trabajo de desarrollo donde se dejó en la última sesión.

## Instrucciones

1. Lee el archivo `.claude/rules/session.md` que contiene:
   - Estado Actual (branch, fase)
   - Última Sesión (qué se hizo)
   - Tareas Pendientes
   - En Progreso
   - Decisiones Técnicas

2. Ejecuta `git status --short app/src` y `git log -3 --oneline` para ver el estado actual del repositorio

3. Presenta un resumen conciso al usuario:
   - **Branch y fase actual**
   - **Última sesión:** qué se hizo
   - **En progreso:** archivos sin commit (si hay)
   - **Pendiente:** tareas por hacer

4. Pregunta brevemente qué quiere hacer:
   - Continuar con lo pendiente
   - Iniciar algo nuevo

**IMPORTANTE:** Sé conciso. No repitas toda la info del CLAUDE.md, solo lo relevante para retomar.
