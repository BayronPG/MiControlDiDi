# Instrucciones del proyecto MiControlDiDi

## Objetivo
Desarrollar una aplicación Android para controlar ingresos, gastos y ganancias de un conductor de moto.

La fuente principal de requisitos es:
`docs/SRS_MiControlDiDi_v1.docx`

No inventes requisitos que no aparezcan en el SRS o en `TASKS.md`.

## Stack obligatorio
- Kotlin
- Jetpack Compose
- Material 3
- Room
- MVVM
- Repository Pattern
- Coroutines y Flow
- Navigation Compose
- Gradle Kotlin DSL
- Pruebas unitarias con JUnit

## Arquitectura
Mantén separadas las capas de interfaz y datos.

Flujo esperado:
`Compose UI -> ViewModel -> Repository -> DAO -> Room`

Los composables no deben acceder directamente a Room y los ViewModel no deben acceder directamente a los DAO.

## Reglas de desarrollo
1. Trabaja únicamente en la tarea indicada.
2. No desarrolles funcionalidades futuras sin autorización.
3. No cambies versiones de dependencias sin necesidad.
4. No introduzcas Firebase, backend, servicios en la nube ni inicio de sesión.
5. No almacenes dinero usando Float o Double; usa Long para pesos colombianos enteros.
6. No guardes valores que puedan calcularse a partir de otros datos.
7. Mantén los textos visibles en recursos de strings.
8. Antes de modificar código, inspecciona los archivos relacionados.
9. Después de cada cambio, compila y ejecuta las pruebas disponibles.
10. Si la compilación falla, corrige el error antes de continuar.
11. No ocultes errores ni desactives pruebas para hacer que el proyecto pase.
12. Trabaja una sola tarea por ejecución.

## Seguridad
- Nunca escribas claves API dentro del repositorio.
- No copies secretos ni variables de entorno al código.
- No ejecutes comandos destructivos.
- No uses `git reset --hard` ni `git push --force`.
- No elimines la base de datos para ocultar errores de migración.

## Cierre de cada tarea
1. Indica archivos creados y modificados.
2. Explica brevemente las decisiones tomadas.
3. Muestra el resultado de compilación y pruebas.
4. Actualiza `PROJECT_STATUS.md` y `TASKS.md`.
5. Propón un mensaje de commit.
6. Detente y no continúes automáticamente.

## MVP
- Registrar, consultar, editar y eliminar viajes.
- Registrar, consultar, editar y eliminar gastos.
- Calcular ingresos, gastos y ganancia neta.
- Dashboard diario, semanal y mensual.
- Funcionamiento local sin Internet.

## Fuera del MVP
No implementar todavía IA, Firebase, inicio de sesión, sincronización, Google Drive, PDF, Excel, voz, escaneo, GPS, integración directa con DiDi ni mantenimiento avanzado.

## Tools

### Local notes (migrated from TOOLS.md)

# TOOLS.md - Local Notes

Skills define _how_ tools work. This file is for _your_ specifics — the stuff that's unique to your setup: camera names and locations, SSH hosts and aliases, preferred TTS voices, speaker/room names, device nicknames, anything environment-specific.

## Examples

```markdown
### Cameras

- living-room → Main area, 180° wide angle
- front-door → Entrance, motion-triggered

### SSH

- home-server → 192.168.1.100, user: admin

### TTS

- Preferred voice: "Nova" (warm, slightly British)
- Default speaker: Kitchen HomePod
```

## Why Separate?

Skills are shared. Your setup is yours. Keeping them apart means you can update skills without losing your notes, and share skills without leaking your infrastructure.

---

Add whatever helps you do your job. This is your cheat sheet.

## Related

- [Agent workspace](/concepts/agent-workspace)
