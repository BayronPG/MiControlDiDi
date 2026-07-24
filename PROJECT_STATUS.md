# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Interfaz de gastos: listar y registrar.

---

## Estado general
Capa de datos completa. Interfaz de viajes funcional. Interfaz de gastos: listar y registrar funcional. Navegación con barra inferior. 80 pruebas automatizadas.

## Completado
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Capa de datos completa.**
- [x] **Fase 3 — Registrar y listar viajes (UI funcional).**
- [x] **Fase 4 — Gastos CRUD (incremento 1): listar y registrar gastos.**

## Capa de datos de gastos
- [x] `CategoriaGastoEntity` con `@ColumnInfo(collate = ColumnInfo.NOCASE)` e índice único.
- [x] `GastoEntity` con FK → categorias_gasto ON DELETE RESTRICT.
- [x] DAOs y repositorios con validaciones.
- [x] `MiControlDatabase` versión 2 con `MIGRATION_1_2` explícita.
- [x] 6 categorías iniciales insertadas en migración (v1→v2) y callback (v2 nueva).
- [x] **Unicidad case-insensitive en 3 capas:**
  1. **SQLite:** `COLLATE NOCASE` en columna + `UNIQUE INDEX`
  2. **Repositorio:** `existePorNombre()` con `LOWER()` + `Result.failure`
  3. **DAO:** `OnConflictStrategy.IGNORE` → duplicados silenciosamente ignorados
- [x] Repositorio detecta retorno `-1L` del DAO como fallo de inserción.

## Interfaz de gastos (nuevo)
- [x] `GastoUiState` con lista, categorías, formulario y validaciones.
- [x] `GastoViewModel` con combine de gastos + categorías, validación y protección contra doble clic.
- [x] `ListaGastosScreen` con LazyColumn, FAB, estado vacío y testTags.
- [x] `RegistrarGastoScreen` con selector de categorías (ExposedDropdownMenu), campos valor y descripción.
- [x] Barra de navegación inferior entre Viajes y Gastos.
- [x] Rutas: `lista_gastos`, `registrar_gasto` (sin gastoId).

## Batería de pruebas

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Unitarias | **47** | ✅ |
| Instrumentadas | **33** | ✅ |
| **Total** | **80** | ✅ |

### Unitarias (47)
| Archivo | Pruebas |
|---------|---------|
| `ViajeEntityTest` | 6 |
| `CategoriaGastoEntityTest` | 3 |
| `ViajeRepositoryTest` | 6 |
| `CategoriaGastoRepositoryTest` | 5 |
| `GastoRepositoryTest` | 5 |
| `ViajeViewModelTest` | 10 |
| `GastoViewModelTest` | **12** |

### Instrumentadas (33)
| Archivo | Pruebas |
|---------|---------|
| `ViajeDaoTest` | 4 |
| `CategoriaGastoDaoTest` | 3 |
| `CategoriaUnicidadTest` | 3 |
| `GastoDaoTest` | 4 |
| `MigracionTest` | 1 |
| `ViajeComposeTest` | 8 |
| `GastoComposeTest` | **10** |

## Verificación manual — HONOR ALT-LX3 (24-jul-2026)
- ✅ Viajes continúa funcionando.
- ✅ Navegación hacia Gastos desde barra inferior.
- ✅ Seis categorías visibles en el selector.
- ✅ Registro: Gasolina, $18.000, "Tanqueo de prueba".
- ✅ Gasto aparece como $ 18.000 con categoría y descripción.
- ✅ Cierre desde recientes + reapertura: gasto persiste.
- ✅ Viajes existentes siguen visibles.

## Deuda técnica
| Elemento | Detalle |
|---|---|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 |
| Persistencia tras cierre en HONOR | Verificada manualmente (no automatizada) |

## Pendiente
- [ ] Editar y eliminar gastos (UI).
- [ ] Editar y eliminar viajes (UI).
- [ ] Dashboard y balance (Fase 5).
- [ ] Fase 6–8: Filtros, metas, preferencias, calidad.
