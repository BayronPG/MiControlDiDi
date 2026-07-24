# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Fase 2 (viajes) completada.

---

## Estado general
Fase 1 completada. Primera parte de Fase 2 (persistencia de viajes) completada y compilada.

## Completado
- [x] Documento SRS elaborado.
- [x] Análisis técnico del SRS completado.
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 (viajes) — Persistencia local del módulo de viajes.**
  - [x] Room 2.8.4 + KSP 2.1.20-1.0.32 configurados.
  - [x] `ViajeEntity`: id, fechaHora, valor, propina, observacion. `ingresoTotal` calculado.
  - [x] `ViajeDao`: insertar (suspend), obtenerTodos ordenado DESC (Flow).
  - [x] `MiControlDatabase`: versión 1, exportSchema, singleton.
  - [x] `ViajeRepository`: validación valor > 0, propina >= 0, delegación al DAO.
  - [x] Directorio `schemas/` con esquema Room exportado.
  - [x] `assembleDebug` → **BUILD SUCCESSFUL** (KSP + Room compilados).
  - [x] `testDebugUnitTest` → **BUILD SUCCESSFUL** (11 pruebas unitarias).
  - [x] Gradle: versiones fijas, sin `+`, KSP en lugar de KAPT.

## Pendiente — Continúa Fase 2
- [ ] Entidad y DAO de CategoriaGasto.
- [ ] Entidad y DAO de Gasto.
- [ ] Entidad y DAO de Meta.
- [ ] Entidad y DAO de Configuracion.
- [ ] Repositorios correspondientes.
- [ ] Sembrar categorías de gasto.

## Pendiente — Fase 3
- [ ] ViewModel de viajes.
- [ ] Pantalla de listado de viajes.
- [ ] Pantalla de formulario de viaje.
- [ ] Validaciones en UI.
- [ ] Edición y eliminación.
- [ ] Pruebas unitarias.

## Pendiente — Fase 4
- [ ] CRUD completo de gastos.

## Pendiente — Fase 5
- [ ] Dashboard y balance.

## Pendiente — Fase 6
- [ ] Filtros, metas y estadísticas.

## Pendiente — Fase 7
- [ ] Preferencias de tema.

## Pendiente — Fase 8
- [ ] Pruebas, accesibilidad y estabilización.

## Decisiones técnicas

| Elemento | Valor |
|---|---|
| Room | 2.8.4 |
| KSP | 2.1.20-1.0.32 (compatible con Kotlin 2.1.20) |
| ViajeEntity.ingresoTotal | **No persistido** — calculado como `valor + propina` |
| Moneda interna | `Long` en COP |
| DB name | `micontrol_didi.db` |
| DB version | 1 |
| Esquema exportado | `app/app/schemas/` |
| Pruebas unitarias | 11 tests en 2 clases (entity + repository) |
