# Estado del proyecto MiControlDiDi

> Actualizado: 25-jul-2026 — Cierre documental de fases 1 a 4.

---

## Estado general
Capa de datos completa. Interfaz de viajes funcional. Gastos CRUD completo con edición, eliminación y protección ante ID inexistente. Navegación con barra inferior. 117 pruebas automatizadas existentes, 117 superadas, 0 flaky pendientes.

> **Las fases 1 a 4 están implementadas, verificadas y documentadas. La Fase 5 (Dashboard y balance) no ha sido iniciada y requiere autorización explícita del propietario del proyecto.**

## Completado
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Capa de datos completa.**
- [x] **Fase 3 — Registrar y listar viajes (UI funcional).**
- [x] **Fase 4 — Gastos CRUD completo: listar, registrar, editar y eliminar.**

## Capa de datos de gastos
- [x] `CategoriaGastoEntity` con `@ColumnInfo(collate = ColumnInfo.NOCASE)` e índice único.
- [x] `GastoEntity` con FK → categorias_gasto ON DELETE RESTRICT.
- [x] DAOs y repositorios con validaciones.
- [x] `MiControlDatabase` versión 2 con `MIGRATION_1_2` explícita.
- [x] 6 categorías iniciales insertadas en migración (v1→v2) y callback (v2 nueva).
- [x] **Unicidad case-insensitive en 3 capas.**
- [x] `GastoDao`: `actualizar`, `eliminar`, `obtenerPorId`.

## Interfaz de gastos
- [x] `GastoUiState` con `ModoFormulario` enum: `CREACION`, `CARGANDO_EDICION`, `EDICION`, `ERROR_EDICION`.
- [x] `GastoViewModel` con combine de gastos + categorías, validación, protección doble clic, edición y eliminación.
- [x] `ListaGastosScreen` con LazyColumn, FAB, estado vacío, botones editar/eliminar y AlertDialog.
- [x] `RegistrarGastoScreen` reutilizada para crear y editar, con selector ExposedDropdownMenu.
- [x] Barra de navegación inferior entre Viajes y Gastos.
- [x] Rutas: `lista_gastos`, `registrar_gasto`, `registrar_gasto/{gastoId}`.
- [x] ID inexistente → pantalla de error, nunca inserta ni actualiza.

## Batería de pruebas

| Tipo | Existentes | Superadas (última ejecución) | Flaky/fallidas |
|------|---:|---:|---:|
| Unitarias | **67** | **67** | 0 |
| Instrumentadas | **50** | **50** | 0 |
| **Total** | **117** | **117** | 0 |

> **Nota (25-jul-2026):** Trazabilidad completa de la validación de pruebas.
>
> ### Validación completa registrada (antes del reemplazo sintáctico)
> - `testDebugUnitTest`: **67/67**.
> - `connectedDebugAndroidTest`: **50/50** (dos ejecuciones consecutivas).
> - `GastoComposeTest`: **17/17** (tres ejecuciones consecutivas).
> - **Total: 117/117**.
>
> ### Validación posterior al reemplazo de aserciones
> Después del cambio de aserciones de Kotlin (`assert(…)`) por aserciones JUnit (`assertEquals`, `assertTrue`, `assertNotNull`), se ejecutó y validó específicamente `GastoComposeTest`: **17/17**.
> **La suite completa no se repitió después de ese último cambio sintáctico.**
>
> ---
>
> ### Antecedente: incidencia flaky (corregida)
> **Causa raíz:** Dos asignaciones consecutivas de `Modifier.testTag("a").testTag("b")` impedían que `onAllNodesWithTag` encontrara los nodos. Se simplificó a un único `testTag` por botón.
>
> **Selectores:** Cada prueba identifica el gasto por su ID único obtenido de Room, usando `boton_editar_gasto_<id>` y `boton_eliminar_gasto_<id>`. Sin selectores por índice.
>
> **Limpieza selectiva:** `@Before` y `@After` ejecutan `limpiarGastosDePrueba()`, que filtra gastos por el prefijo `__TEST_GASTO_COMPOSE__` y solo elimina los que coinciden. Gastos normales, viajes y categorías nunca se eliminan. Las descripciones de prueba incluyen el prefijo reservado y un UUID. La prueba `limpiezaSelectiva_conservaGastosSinPrefijo` verifica la conservación, y retira su gasto de control (sin prefijo) mediante try/finally con su ID.
>
> **Validación:** Eliminación verifica `$item_gasto_<id>$` y descripción única vía `waitUntil` + confirmación Room. Edición verifica que el texto anterior desaparece (el card permanece).
>
> **Resultado previo a la corrección:** 42/49. Corregido en esa tarea.

### Estado funcional de gastos

| Funcionalidad | Implementado | Compilado | Pruebas unitarias | Pruebas DAO | Pruebas Compose | Verificación manual |
|---|---|---|---|---|---|---|
| Crear gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Listar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Eliminar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| ID inexistente | ✅ | ✅ | ✅ | — | ❌ no implementada | — |

### Unitarias (67)
| Archivo | Pruebas |
|---------|---------|
| `ViajeEntityTest` | 6 |
| `CategoriaGastoEntityTest` | 3 |
| `ViajeRepositoryTest` | 6 |
| `CategoriaGastoRepositoryTest` | 5 |
| `GastoRepositoryTest` | 9 |
| `ViajeViewModelTest` | 10 |
| `GastoViewModelTest` | **28** |

### Instrumentadas (50)
| Archivo | Pruebas |
|---------|---------|
| `ViajeDaoTest` | 4 |
| `CategoriaGastoDaoTest` | 3 |
| `CategoriaUnicidadTest` | 3 |
| `GastoDaoTest` | **14** |
| `MigracionTest` | 1 |
| `ViajeComposeTest` | 8 |
| `GastoComposeTest` | **17** |

## Verificación manual — HONOR ALT-LX3 (24-jul-2026)

### Incremento 1 ✅
- ✅ Viajes continúa funcionando.
- ✅ Navegación hacia Gastos desde barra inferior.
- ✅ Seis categorías visibles en el selector.
- ✅ Registro: Gasolina, $18.000, "Tanqueo de prueba".
- ✅ Gasto aparece con categoría, valor y descripción.
- ✅ Cierre desde recientes + reapertura: gasto persiste.
- ✅ Viajes existentes siguen visibles.

### Incremento 2 ✅
- ✅ Editar gasto: abrir, modificar categoría, valor y descripción, guardar.
- ✅ Persistencia de edición: cerrar y reabrir, cambios conservados.
- ✅ Sin duplicados tras editar.
- ✅ Cancelar eliminación: diálogo se cierra, gasto permanece.
- ✅ Confirmar eliminación: gasto desaparece de la lista.
- ✅ Persistencia de eliminación: cerrar y reabrir, gasto no reaparece.
- ✅ Otros gastos permanecen intactos.
- ✅ Categorías siguen disponibles.
- ✅ Sin cierres inesperados.

## Deuda técnica
| Elemento | Detalle |
|---|---|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 |
| Persistencia en HONOR | Verificada manualmente (no automatizada) |

## Pendiente
- [ ] Editar y eliminar viajes (UI).
- [ ] Dashboard y balance (Fase 5).
- [ ] Fase 6–8: Filtros, metas, preferencias, calidad.
