# MiControlDiDi

Aplicación Android para controlar ingresos, gastos y ganancias de un conductor de moto.

## Estado del proyecto

- **Fases 1 a 4 implementadas y verificadas.**
- **Fase 5 no iniciada** — requiere autorización explícita del propietario del proyecto antes de comenzar.
- La aplicación funciona completamente sin conexión a Internet.

## Stack técnico

- **Kotlin** — lenguaje principal.
- **Jetpack Compose** — UI declarativa.
- **Material 3** — diseño siguiendo Material Design 3.
- **Room** — persistencia local con SQLite.
- **KSP** — procesador de anotaciones de Room.
- **MVVM** — separación de capas UI y datos.
- **Repository Pattern** — abstracción entre ViewModel y DAO.
- **StateFlow** — estado reactivo en ViewModels.
- **Coroutines y Flow** — operaciones asíncronas y reactivas.
- **Navigation Compose** — navegación entre pantallas.
- **Gradle Kotlin DSL** — configuración del proyecto.

## Arquitectura

```
Compose UI → ViewModel → Repository → DAO → Room
```

## Funcionalidades implementadas

### Viajes

- [x] Registrar viajes con valor, propina y observación.
- [x] Listar viajes ordenados por fecha descendente.
- [x] Validar datos: valor > 0, propina ≥ 0, fecha obligatoria.
- [x] Persistir viajes en Room.

> Editar y eliminar viajes desde la interfaz **no está implementado**.

### Gastos

- [x] Registrar gastos con categoría, valor y descripción.
- [x] Listar gastos ordenados por fecha descendente.
- [x] Seleccionar categorías activas desde ExposedDropdownMenu.
- [x] Editar gastos (categoría, valor, descripción conservando ID y fechaHora).
- [x] Eliminar gastos con diálogo de confirmación y opción de cancelar.
- [x] Persistir creación, edición y eliminación.
- [x] Proteger la edición cuando el ID no existe (pantalla de error).
- [x] 6 categorías iniciales: Gasolina, Mantenimiento, Parqueadero, Lavado, Cuota de la moto, Otros.
- [x] Unicidad case-insensitive en nombres de categoría (COLLATE NOCASE).

### Navegación

- [x] Barra de navegación inferior entre Viajes (inicio) y Gastos.
- [x] Destino inicial: `lista_viajes`.
- [x] Rutas: `lista_viajes`, `registrar_viaje`, `lista_gastos`, `registrar_gasto`, `registrar_gasto/{gastoId}`.

## Base de datos

- **Archivo:** `micontrol_didi.db`
- **Versión Room:** 2
- **Tablas:** `viajes`, `categorias_gasto`, `gastos`
- **Migración:** `MIGRATION_1_2` registrada explícitamente (v1 → v2 añade categorías y gastos)
- **Sin** `fallbackToDestructiveMigration()`
- **Seis categorías iniciales** insertadas tanto en la migración como en `onCreate` para bases nuevas
- **Relación:** `gastos.categoriaId` → `categorias_gasto.id` con `ON DELETE RESTRICT`
- **`exportSchema = false`** — deuda técnica por incompatibilidad entre Room 2.8.4 y Kotlin 2.1.20

> **Sobre la validación de la migración:**
> - Existe una prueba instrumentada directa (`MigracionTest`) que crea la base v1 manualmente con SQL, la abre con Room + `MIGRATION_1_2` y verifica que los datos de viajes se conservan, las categorías iniciales existen y los gastos pueden insertarse.
> - **No** está validada mediante `MigrationTestHelper` contra un esquema `2.json` — no existe `2.json` mientras `exportSchema` permanezca desactivado.

## Batería de pruebas

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Unitarias | 67 | ✅ |
| Instrumentadas | 50 | ✅ |
| **Total** | **117** | ✅ |

### Trazabilidad

- Validación completa registrada: **67/67** unitarias + **50/50** instrumentadas = **117/117**.
- Después del reemplazo final de aserciones Kotlin (`assert(…)`) por aserciones JUnit (`assertEquals`, `assertTrue`, `assertNotNull`) se validó específicamente `GastoComposeTest`: **17/17**.
- **No se repitió la suite completa después de ese cambio sintáctico.**
- 0 pruebas flaky pendientes.

> Para detalles completos (distribución por archivo, limpieza selectiva, antecedente de incidencia flaky), consultar `PROJECT_STATUS.md`.

### Desglose por archivo

**Unitarias (67)**
| Archivo | Pruebas |
|---------|---------|
| `ViajeEntityTest` | 6 |
| `CategoriaGastoEntityTest` | 3 |
| `ViajeRepositoryTest` | 6 |
| `CategoriaGastoRepositoryTest` | 5 |
| `GastoRepositoryTest` | 9 |
| `ViajeViewModelTest` | 10 |
| `GastoViewModelTest` | 28 |

**Instrumentadas (50)**
| Archivo | Pruebas |
|---------|---------|
| `ViajeDaoTest` | 4 |
| `CategoriaGastoDaoTest` | 3 |
| `CategoriaUnicidadTest` | 3 |
| `GastoDaoTest` | 14 |
| `MigracionTest` | 1 |
| `ViajeComposeTest` | 8 |
| `GastoComposeTest` | 17 |

## Funcionalidades pendientes (no implementadas)

La Fase 5 **no debe iniciarse sin autorización explícita del propietario del proyecto**.

- [ ] Dashboard, balance y selector de periodo (día, semana, mes) — Fase 5.
- [ ] Editar y eliminar viajes desde la interfaz.
- [ ] Filtros por rango de fechas (viajes y gastos).
- [ ] Metas de ganancia.
- [ ] Estadísticas básicas.
- [ ] Categorías personalizadas desde la interfaz de usuario.
- [ ] Preferencias (tema claro/oscuro).

### Fuera del alcance del MVP

- Firebase, backend, sincronización.
- PDF, Excel, escaneo, voz.
- Inteligencia artificial.
- Migración Room 2 → 3.
- Integración directa con DiDi, GPS.

## Deuda técnica

| Elemento | Detalle |
|----------|--------|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 |
| Persistencia en HONOR | Verificada manualmente (no automatizada) |

## Cómo abrir el proyecto

El proyecto Android se encuentra en la carpeta `app`:

```
C:\Proyectos\MiControlDiDi\app
```

Ábrela directamente en **Android Studio** como proyecto existente.

## Comandos básicos

Ejecutar desde `C:\Proyectos\MiControlDiDi\app`:

```bash
# Compilar el proyecto
.\gradlew.bat assembleDebug

# Ejecutar pruebas unitarias
.\gradlew.bat testDebugUnitTest

# Ejecutar pruebas instrumentadas (requiere emulador o dispositivo)
.\gradlew.bat connectedDebugAndroidTest
```

## Documentos importantes

- `docs/SRS_MiControlDiDi_v1.docx` — requisitos del producto.
- `AGENTS.md` — reglas obligatorias para el desarrollo.
- `TASKS.md` — backlog de trabajo.
- `PROJECT_STATUS.md` — estado actual detallado.
