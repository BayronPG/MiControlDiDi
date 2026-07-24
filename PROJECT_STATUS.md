# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Fase 1 completada.

---

## Estado general
Fase 1 completada. Proyecto Android creado y compilado. APK generado.

## Completado
- [x] Documento SRS elaborado.
- [x] Análisis técnico del SRS completado.
- [x] **Fase 1 — Proyecto Android base.**
  - [x] Proyecto Gradle con Kotlin DSL y Gradle Wrapper.
  - [x] AGP 8.13.0, Gradle 8.13, Kotlin 2.1.20, Compose BOM 2025.01.01.
  - [x] compileSdk = 36 (API 36), minSdk = 26, targetSdk = 36.
  - [x] `local.properties` creado y excluido por `.gitignore`.
  - [x] Estructura de paquetes: data/ y ui/ con subpaquetes + .gitkeep.
  - [x] Tema Material 3 con colores personalizados y tipografía.
  - [x] `MainActivity` con `enableEdgeToEdge`, `Scaffold` y pantalla "MiControlDiDi".
  - [x] Recursos: strings.xml, colors.xml, themes.xml, icono adaptativo.
  - [x] `assembleDebug` → **BUILD SUCCESSFUL**.
  - [x] `testDebugUnitTest` → **BUILD SUCCESSFUL** (NO-SOURCE, no hay pruebas aún).
  - [x] APK generado: `app/app/build/outputs/apk/debug/app-debug.apk` (9,4 MB).

## En curso
- [ ] Pendiente de iniciar — Fase 2: Capa de datos (Room).

## Pendiente — Fase 2
- [ ] Crear entidades Room (Viaje, Gasto, CategoriaGasto, Meta, Configuracion).
- [ ] Crear DAOs para cada entidad.
- [ ] Crear MiControlDatabase.
- [ ] Crear repositorios.
- [ ] Sembrar categorías de gasto iniciales.

## Pendiente — Fase 3
- [ ] Implementar CRUD completo de viajes.
- [ ] Validaciones y pruebas unitarias.

## Pendiente — Fase 4
- [ ] Implementar CRUD completo de gastos.
- [ ] Validaciones y pruebas unitarias.

## Pendiente — Fase 5
- [ ] Dashboard y cálculo de balance por periodo.
- [ ] Pruebas unitarias.

## Pendiente — Fase 6
- [ ] Filtros por fecha y categoría.
- [ ] Configuración de metas.
- [ ] Estadísticas básicas.

## Pendiente — Fase 7
- [ ] Preferencias de tema visual.

## Pendiente — Fase 8
- [ ] Pruebas completas, accesibilidad y estabilización.
- [ ] APK de demostración.

## Bloqueos conocidos
- Ninguno. Entorno completamente funcional.

## Decisiones técnicas

| Elemento | Valor |
|---|---|
| Namespace / App ID | `com.jhon.micontroldidi` |
| compileSdk | 36 (API 36 — Android 16) |
| minSdk | 26 (Android 8.0) |
| targetSdk | 36 |
| AGP | 8.13.0 |
| Gradle | 8.13 (Wrapper) |
| Kotlin | 2.1.20 |
| Compose BOM | 2025.01.01 |
| JDK | OpenJDK 17.0.19 LTS |
| Icono | Adaptativo con drawable vectorial |
