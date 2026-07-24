# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Fase 3 cerrada.

---

## Estado general
Fase 3 completada y verificada. UI de viajes funcional con 33 pruebas automatizadas y persistencia real confirmada en HONOR ALT-LX3.

## Completado
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Persistencia local del módulo de viajes.**
- [x] **Fase 3 — Registrar y listar viajes (interfaz funcional).**
  - [x] `ViajeViewModel` con `StateFlow`, validaciones, Factory y protección contra doble clic.
  - [x] `ListaViajesScreen`: LazyColumn, formato COP, testTags.
  - [x] `RegistrarViajeScreen`: 3 campos, validación, testTags.
  - [x] `NavGraph`: navegación lista ↔ formulario.
  - [x] `CurrencyFormatter`, `DateFormatter` con `Locale.of("es", "CO")`.
  - [x] `assembleDebug` → **BUILD SUCCESSFUL**.
  - [x] `testDebugUnitTest` → **BUILD SUCCESSFUL** (21 unitarios).
  - [x] `connectedDebugAndroidTest` → **BUILD SUCCESSFUL** (12 instrumentados en ALT-LX3).
  - [x] **Persistencia real verificada manualmente en HONOR ALT-LX3.**
    - Valor: $12.500, Propina: $1.500, Total mostrado: $14.000
    - Observación: "PRUEBA PERSISTENCIA"
    - Cerrada desde recientes y reabierta → viaje visible.

## Deuda técnica registrada
| Elemento | Detalle |
|---|---|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 para exportar esquemas JSON. `AbstractMethodError` en `FieldBundle$$serializer`. Requiere Room ≥ 2.8.5+ o Kotlin < 2.1. |
| Plugin `kotlin.plugin.serialization` | Eliminado — se agregó como intento de solución pero no resuelve el error de exportación. |

## Pendiente — Editar y eliminar viajes
- [ ] Pantalla de edición de viaje.
- [ ] Eliminación con confirmación.

## Pendiente — Fase 4
- [ ] Entidad y DAO de CategoriaGasto.
- [ ] Entidad y DAO de Gasto.
- [ ] CRUD completo de gastos.

## Pendiente — Fase 5 a 8
- [ ] Dashboard, filtros, metas, preferencias, calidad.

## Batería de pruebas verificada

| Tipo | Cantidad | Estado |
|---|---|---|
| Unitarias — ViajeEntity | 6 | ✅ |
| Unitarias — ViajeRepository | 5 | ✅ |
| Unitarias — ViajeViewModel | 10 | ✅ |
| Instrumentadas — ViajeDao (Room in-memory) | 4 | ✅ |
| Instrumentadas — Compose UI (semántica) | 8 | ✅ |
| **Total automatizadas** | **33** | **✅** |
| **Persistencia real (manual)** | 1 | ✅ Manuelmente en dispositivo |
