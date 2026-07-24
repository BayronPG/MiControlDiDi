# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Gastos CRUD completo con verificación manual en HONOR ALT-LX3.

---

## Estado general
Capa de datos completa. Interfaz de viajes funcional. Gastos CRUD completo con edición, eliminación y protección ante ID inexistente. Navegación con barra inferior. 116 pruebas existentes (109 superadas en última ejecución, 7 flaky).

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
| Instrumentadas | **49** | **42** | **7** |
| **Total** | **116** | **109** | **7** |

> **Nota sobre pruebas flaky:** Las 7 pruebas afectadas pertenecen a `GastoComposeTest`. El fallo es `Can't retrieve node at index '0'` al buscar botones de editar/eliminar. La causa identificada es la presencia de datos residuales de ejecuciones anteriores que desplazan los gastos recién creados fuera del viewport de `LazyColumn`. El aislamiento de las pruebas instrumentadas frente a datos persistentes permanece pendiente.
>
> Las pruebas unitarias y de DAO cubren completamente la lógica de edición y eliminación.

### Estado funcional de gastos

| Funcionalidad | Implementado | Compilado | Pruebas unitarias | Pruebas DAO | Pruebas Compose | Verificación manual |
|---|---|---|---|---|---|---|
| Crear gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Listar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar gastos | ✅ | ✅ | ✅ | ✅ | ⚠️ flaky | ✅ |
| Eliminar gastos | ✅ | ✅ | ✅ | ✅ | ⚠️ flaky | ✅ |
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

### Instrumentadas (49)
| Archivo | Pruebas |
|---------|---------|
| `ViajeDaoTest` | 4 |
| `CategoriaGastoDaoTest` | 3 |
| `CategoriaUnicidadTest` | 3 |
| `GastoDaoTest` | **14** |
| `MigracionTest` | 1 |
| `ViajeComposeTest` | 8 |
| `GastoComposeTest` | **16** |

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
