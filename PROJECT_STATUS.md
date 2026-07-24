# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Capa de datos de gastos completada con COLLATE NOCASE.

---

## Estado general
Capa de datos completa para viajes y gastos. Unicidad case-insensitive reforzada con COLLATE NOCASE en SQLite. 57 pruebas automatizadas.

## Completado
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Capa de datos completa.**
- [x] **Fase 3 — Registrar y listar viajes (UI funcional).**

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

## Batería de pruebas

| Tipo | Cantidad | Estado |
|---|---|---|
| Unitarias | **34** | ✅ |
| Instrumentadas | **23** | ✅ |
| **Total** | **57** | ✅ |

## Composición instrumentadas (23)
- 4 ViajeDao (preexistentes)
- 8 ViajeCompose UI (preexistentes)
- 3 CategoriaGastoDao
- 4 GastoDao
- 1 Migracion v1→v2
- 3 CategoriaUnicidad

## Deuda técnica
| Elemento | Detalle |
|---|---|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 |
| Persistencia tras cierre en HONOR | Verificada manualmente (no automatizada) |

## Pendiente
- [ ] Editar y eliminar viajes (UI).
- [ ] UI completa de gastos (ViewModel + pantallas + navegación).
- [ ] Fase 5: Dashboard y balance.
- [ ] Fase 6–8: Filtros, metas, preferencias, calidad.
