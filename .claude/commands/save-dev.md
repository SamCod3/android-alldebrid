---
name: save-dev
description: Guarda el estado de la sesión antes de terminar
---

# Skill: save-dev

Guarda el estado de desarrollo antes de terminar la sesión.

## Instrucciones

1. Ejecuta `git status --short app/src CLAUDE.md` y `git log -1 --oneline` para ver el estado actual

2. Analiza automáticamente:
   - Archivos modificados sin commit → "En Progreso"
   - Commits recientes → "Última Sesión"
   - Tareas que faltan según el contexto → "Tareas Pendientes"

3. Actualiza el archivo `CLAUDE.md` en la sección `/continue-dev`:
   - "Última Sesión" con fecha de hoy y resumen de lo hecho
   - "En Progreso" si hay archivos sin commitear
   - "Tareas Pendientes" si hay algo identificado
   - "Decisiones Técnicas" si hubo alguna importante

4. Haz commit del CLAUDE.md actualizado con mensaje: `docs: save-dev session state`

5. Confirma brevemente que el estado se guardó

**IMPORTANTE:** No hagas muchas preguntas. Infiere el estado del código y commits. Solo confirma al finalizar.
