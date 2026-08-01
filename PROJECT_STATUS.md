# Estado del proyecto MiControlDiDi

> Actualizado: 26-jul-2026 — Fase 8.6 completada (strings hardcodeadas trasladadas a resources).

---

## Estado general

Capa de datos completa. Interfaz de viajes funcional. Gastos CRUD completo con edición, eliminación y protección ante ID inexistente. Navegación con barra inferior.

**Fase 5 completada.** Incrementos 1A–1D (dominio de periodos, consultas agregadas, DashboardViewModel y DashboardScreen con navegación) completados, validados e integrados en `main`.

- **113 pruebas unitarias** (testDebugUnitTest BUILD SUCCESSFUL).
- **68 pruebas instrumentadas** (65 anteriores + 3 nuevas de FABs en DashboardScreen).
- **0 flaky pendientes.**

> **Las fases 1 a 5 están cerradas. La Fase 6 es la siguiente: filtros, metas y estadísticas.**

## Completado

- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Capa de datos completa.**
- [x] **Fase 3 — Registrar y listar viajes (UI funcional).**
- [x] **Fase 4 — Gastos CRUD completo: listar, registrar, editar y eliminar.**
- [x] **Fase 5 — Incremento 1A: Cálculo de periodos del Dashboard.**
- [x] **Fase 5 — Incremento 1B: Totales por rango (ingresos y gastos).**
- [x] **Fase 5 — Incremento 1C: DashboardUiState y DashboardViewModel.**
- [x] **Fase 5 — Incremento 1D: DashboardScreen, accesos rápidos, navegación.**

---

## Fase 5 — Incremento 1A: Cálculo de periodos del Dashboard

### Implementado

- `PeriodoDashboard` enum: `DIA`, `SEMANA`, `MES`.
- `RangoPeriodo` data class con `inicioInclusivo: Long` y `finExclusivo: Long`.
- `CalculadorRangoPeriodo`: dado un timestamp y un `PeriodoDashboard`, produce el `RangoPeriodo` correspondiente:
  - **DIA**: desde las 00:00:00.000 hasta las 00:00:00.000 del día siguiente.
  - **SEMANA**: lunes 00:00:00.000 de la semana que contiene el timestamp hasta el lunes siguiente.
  - **MES**: día 1 del mes a las 00:00:00.000 hasta el día 1 del mes siguiente.
- Semana iniciada el lunes (DP-04).
- `ZoneId` explícito usando la zona horaria del dispositivo.
- Intervalos semiabiertos `[inicioInclusivo, finExclusivo)`.
- 22 pruebas unitarias que cubren:
  - Límites exactos de día, semana y mes.
  - Fechas al borde de intervalos (inicio incluido, fin excluido).
  - Semanas que cruzan cambio de mes.
  - Meses con diferente número de días (28, 29, 30, 31).
  - Año bisiesto.
  - Zona horaria explícita.

---

## Fase 5 — Incremento 1B: Totales por rango (ingresos y gastos)

### Implementado

#### ViajeDao
- Nueva consulta agregada `obtenerIngresosPorRango(inicioInclusivo, finExclusivo): Flow<Long>`
- SQL: `SELECT COALESCE(SUM(valor + propina), 0) FROM viajes WHERE fechaHora >= :inicioInclusivo AND fechaHora < :finExclusivo`
- Reactivo: retorna `Flow<Long>` que se actualiza al insertar viajes.
- La suma se realiza completamente en SQLite sin cargar listas completas en memoria.

#### GastoDao
- Nueva consulta agregada `obtenerTotalGastosPorRango(inicioInclusivo, finExclusivo): Flow<Long>`
- SQL: `SELECT COALESCE(SUM(valor), 0) FROM gastos WHERE fechaHora >= :inicioInclusivo AND fechaHora < :finExclusivo`
- Reactivo: retorna `Flow<Long>` que se actualiza al insertar, actualizar o eliminar gastos.

#### Repositorios
- `ViajeRepository.obtenerIngresosPorRango()` delega directamente en `ViajeDao`.
- `GastoRepository.obtenerTotalGastosPorRango()` delega directamente en `GastoDao`.
- Sin transformaciones adicionales; exponen el `Flow` sin modificarlo.

#### Pruebas
- **7 nuevas pruebas DAO instrumentadas** en `ViajeDaoTest`: suma correcta, límites inclusivo/exclusivo, fuera de rango, reactividad al insertar.
- **8 nuevas pruebas DAO instrumentadas** en `GastoDaoTest`: suma correcta, límites, reactividad al insertar, actualizar y eliminar.
- **4 nuevas pruebas unitarias** en `ViajeRepositoryTest` y `GastoRepositoryTest`: verificación de delegación y exposición del `Flow`.

#### Estabilización de pruebas
- Selector `onNodeWithText("Gasolina")` reemplazado por `onNode(hasText("Gasolina") and hasAnyAncestor(hasTestTag("item_gasto_$id")))` para evitar ambigüedad con nodos `EditableText` persistentes del `ExposedDropdownMenuBox`.

---

## Fase 5 — Incremento 1C: DashboardUiState y DashboardViewModel

### Implementado

#### DashboardUiState (`com.jhon.micontroldidi.ui.dashboard`)

```kotlin
data class DashboardUiState(
    val periodoSeleccionado: PeriodoDashboard = PeriodoDashboard.DIA,
    val ingresos: Long = 0L,
    val gastos: Long = 0L,
    val gananciaNeta: Long = 0L,
    val cargando: Boolean = true,
    val mensajeError: String? = null
)
```

- Valores de dominio sin formato monetario.
- Estado inicial: DIA, ceros, cargando.

#### DashboardViewModel

- Dependencias inyectables: `ViajeRepository`, `GastoRepository`, `java.time.Clock`.
- `Clock` evita `System.currentTimeMillis()` y permite pruebas con tiempo fijo.
- `ZoneId` se obtiene de `clock.zone`, usado por `CalculadorRangoPeriodo`.

**Flujo reactivo:**

```
_periodo (MutableStateFlow<PeriodoDashboard>)
  |  flatMapLatest { periodo ->
  |    CalculadorRangoPeriodo.calcular(periodo, clock.millis(), clock.zone)
  |    combine(
  |      viajeRepository.obtenerIngresosPorRango(inicio, fin),
  |      gastoRepository.obtenerTotalGastosPorRango(inicio, fin)
  |    ) { ingresos, gastos -> DashboardUiState(gananciaNeta = ingresos - gastos, ...) }
  |      .catch { ... }   // errores capturados sin exponer stack traces
  |  }
  v
collect -> _uiState
```

- `flatMapLatest` cancela la observación anterior al cambiar de periodo, evitando fugas.
- `seleccionarPeriodo(periodo)` cambia `_periodo`, disparando `flatMapLatest`.
- `combine` fusiona los Flows de ingresos y gastos en un solo estado.
- `gananciaNeta = ingresos - gastos`.
- Manejo de errores con `catch`: establece `cargando = false` y `mensajeError` legible.
- `CancellationException` se relanza (no se oculta).
- Recuperación al cambiar de periodo: `flatMapLatest` crea un nuevo Flow interno.

#### Factory

```kotlin
class Factory(
    private val viajeRepository: ViajeRepository,
    private val gastoRepository: GastoRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModelProvider.Factory
```

#### Pruebas unitarias (20)

| Prueba | Categoría |
|---|---|
| `estado inicial periodo es DIA` | Estado inicial |
| `sin movimientos devuelve ceros` | Estado inicial |
| `combina ingresos y gastos en el estado` | Combinación |
| `ganancia positiva cuando ingresos superan gastos` | Ganancia |
| `ganancia cero cuando ingresos igualan gastos` | Ganancia |
| `ganancia negativa cuando gastos superan ingresos` | Ganancia |
| `ingresos recibidos del repositorio ya incluyen propinas` | Propinas |
| `cambio a SEMANA actualiza periodo en el estado` | Periodo |
| `cambio a MES actualiza periodo en el estado` | Periodo |
| `limites del rango se delegan a los DAOs` | Límites |
| `cambio a SEMANA delega rango semanal` | Límites |
| `cambio a MES delega rango mensual` | Límites |
| `actualizacion de ingresos actualiza el estado` | Reactividad |
| `actualizacion de gastos actualiza el estado` | Reactividad |
| `cambio de periodo recalcula con la hora actual del reloj` | Reactividad |
| `no emite estados duplicados con la misma entrada` | Emisiones |
| `error en flow de ingresos establece mensajeError` | Error |
| `error en flow de gastos establece mensajeError` | Error |
| `error en ingresos no expone stack trace` | Error |
| `cambio de periodo recupera tras error` | Recuperación |

---

## Capa de datos de gastos

- [x] `CategoriaGastoEntity` con `@ColumnInfo(collate = ColumnInfo.NOCASE)` e índice único.
- [x] `GastoEntity` con FK → categorias_gasto ON DELETE RESTRICT.
- [x] DAOs y repositorios con validaciones.
- [x] `MiControlDatabase` versión 2 con `MIGRATION_1_2` explícita.
- [x] 6 categorías iniciales insertadas en migración (v1→v2) y callback (v2 nueva).
- [x] **Unicidad case-insensitive en 3 capas.**
- [x] `GastoDao`: `actualizar`, `eliminar`, `obtenerPorId`.
- [x] `ViajeDao.obtenerIngresosPorRango()` — agregado en Incremento 1B.
- [x] `GastoDao.obtenerTotalGastosPorRango()` — agregado en Incremento 1B.

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
| Unitarias | **113** | **113** | 0 |
| Instrumentadas | **68** (65 ant. + 3 nuevas) | **65** (pendiente ejecución) | 0 |
| **Total** | **~181** | **178+** | 0 |

> **Nota (25-jul-2026):**
>
> - Unitarias validadas tras integrar Incremento 1D: **113/113** (`testDebugUnitTest`, BUILD SUCCESSFUL).
> - Instrumentadas vigentes desde la validación del Incremento 1B: **65/65** (`connectedDebugAndroidTest` en HONOR ALT-LX3).
> - 3 nuevas pruebas Compose para los FABs de acceso rápido. Pendiente `connectedDebugAndroidTest`.

## Fase 8 — Calidad y cierre del MVP (en progreso)

### 8.0 — Corrección de pruebas obsoletas por cambio de UI (26-jul-2026)

El commit `7fe7648` reemplazó los FABs del dashboard por botones con texto claro, pero **3 pruebas Compose** (`fabGastoInvocaCallbackAlPulsar`, `fabViajeInvocaCallbackAlPulsar`, `fabsDeAccesoRapidoEstanVisibles`) seguían referenciando los test tags antiguos (`fab_registrar_gasto_dashboard`, `fab_registrar_viaje_dashboard`).

#### Cambio realizado
- `DashboardScreenTest.kt`: test tags actualizados a `btn_dashboard_viaje` y `btn_dashboard_gasto`.
- Nombres de pruebas renombrados: `fabsDeAccesoRapidoEstanVisibles` → `botonesDeAccesoRapidoEstanVisibles`, `fabViajeInvocaCallbackAlPulsar` → `botonViajeInvocaCallbackAlPulsar`, `fabGastoInvocaCallbackAlPulsar` → `botonGastoInvocaCallbackAlPulsar`.

#### Validación
| Suite | Resultado |
|-------|:---------:|
| `testDebugUnitTest` | BUILD SUCCESSFUL (~150 tests) |
| `connectedDebugAndroidTest` en ALT‑LX3 | **98/98 tests, 0 fallos** |

---

### 8.6 — Strings hardcodeadas trasladadas a resources (26-jul-2026)

Se identificaron y corrigieron 7 cadenas de error/validación hardcodeadas en los ViewModels:

| String | ViewModels afectados | Recurso añadido |
|---|---|---|
| "El valor es obligatorio" | Viaje, Gasto, Meta | `error_valor_obligatorio` |
| "El valor debe ser numérico" | Viaje, Gasto, Meta | `error_valor_numerico` |
| "El valor debe ser mayor que cero" | Viaje, Gasto, Meta | `error_valor_positivo` |
| "La propina debe ser numérica" | Viaje | `error_propina_numerica` |
| "La propina no puede ser negativa" | Viaje | `error_propina_negativa` |
| "Debes seleccionar una categoría" | Gasto | `error_categoria_obligatoria` |
| "Gasto no encontrado" (fallback) | RegistrarGastoScreen | Recurso existente `gasto_no_encontrado` |

#### Cambios estructurales
- Creado `ResourceProvider` (fun interface) para desacoplar ViewModels de Android.
- Creado `FakeResourceProvider` para tests unitarios.
- ViewModels actualizados: `ViajeViewModel`, `GastoViewModel`, `MetaViewModel`, `DashboardViewModel`.
- Factorys y `NavGraph` actualizados para inyectar el provider.
- `MainActivity` crea el provider vía lambda.

#### Validación
| Suite | Resultado |
|-------|:---------:|
| `testDebugUnitTest` | BUILD SUCCESSFUL (~150 tests) |
| `connectedDebugAndroidTest` en ALT‑LX3 | **98/98 tests, 0 fallos** |

---

### 8.3 — Verificación de flujos sin conexión a Internet (01-ago-2026)

Auditoría pasiva: el MVP es 100% local y **no se encontró ningún punto que requiera Internet**.

| Verificación | Resultado |
|---|---|
| `AndroidManifest.xml` (main) | Sin permiso `INTERNET` ni `ACCESS_NETWORK_STATE` — sin permisos declarados |
| Manifest fusionado (debug/release) | Solo `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (añadido por AndroidX, no es de red) |
| Dependencias Gradle | Sin Firebase, Retrofit, OkHttp, Volley ni WebView — solo Room, Compose, Navigation, Lifecycle, Coroutines |
| Código Kotlin (barrido de APIs de red) | 0 hits: HttpURLConnection, OkHttp, URL(, Socket, WebView, ktor, http(s)://, ConnectivityManager, NetworkInfo, java.net, InetAddress, DownloadManager, WorkManager, JobScheduler |
| Intents / navegador / fuentes descargables | 0 hits: sin ACTION_VIEW, sin FontProvider, sin network_security_config |
| Persistencia | `SharedPreferences` local (tema) + Room local — sin DataStore remoto |
| `proguard-rules.pro` | Sin reglas de red |

**Conclusión:** la app funciona completa sin conexión a Internet; no hay código que corregir ni permisos que quitar. El único requisito de red del proyecto es el `push` a GitHub (desarrollo, no runtime).

**Validación:** sin cambios de código en esta tarea; el estado verde vigente es el de 8.2 (`assembleDebug` BUILD SUCCESSFUL + `testDebugUnitTest` 177/177).

---

### 8.2 — Verificación de mensajes de error descriptivos en formularios (01-ago-2026)

Se auditaron los formularios de Viaje, Gasto y Meta y se corrigieron 4 problemas:

| Problema | Corrección |
|---|---|
| String visible hardcodeada `"Registrado: …"` en `RegistrarGastoScreen` | Movida a `strings.xml` como `gasto_registrado_en` con formato `%1$s` |
| Mensajes de excepción crudos (`exceptionOrNull()?.message`) expuestos al usuario en `ViajeViewModel`, `GastoViewModel` y `MetaViewModel` | Reemplazados por 6 mensajes descriptivos de recursos: `error_guardar_viaje`, `error_eliminar_viaje`, `error_guardar_gasto`, `error_eliminar_gasto`, `error_guardar_meta`, `error_eliminar_meta` |
| `errorEliminacion` de Gasto se seteaba pero nunca se mostraba en el diálogo | Ahora se muestra dentro del diálogo de confirmación de eliminación |
| Fallo de eliminación de Viaje era silencioso (sin campo ni mensaje) | Nuevo campo `errorEliminacion` en `ViajeUiState`, mensaje descriptivo y renderizado en el diálogo de confirmación |

#### Cambios estructurales
- `ViajeUiState`: nuevos campos `errorGuardado` y `errorEliminacion`.
- `RegistrarViajeScreen`: muestra `errorGuardado` (antes solo había validación de campo).
- `FakeResourceProvider`: 6 nuevos IDs mapeados para pruebas unitarias.
- `ViajeViewModelTest`: test `errorDeGuardado_noCierraFormulario` ajustado a `errorGuardado`; nuevo test `falloDeEliminacion_estableceMensajeDeError`; fake DAO ahora puede simular fallo en `eliminar`.

#### Validación
| Suite | Resultado |
|-------|:---------:|
| `assembleDebug` | BUILD SUCCESSFUL |
| `testDebugUnitTest` | **177/177, 0 fallos** |
| `connectedDebugAndroidTest` | No ejecutada (sin dispositivo conectado en esta sesión) |

---

### Estado funcional de gastos

| Funcionalidad | Implementado | Compilado | Pruebas unitarias | Pruebas DAO | Pruebas Compose | Verificación manual |
|---|---|---|---|---|---|---|
| Crear gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Listar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Eliminar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| ID inexistente | ✅ | ✅ | ✅ | — | ❌ no implementada | — |

### Unitarias (113)
| Archivo | Pruebas |
|---------|---------|
| `ViajeEntityTest` | 6 |
| `CategoriaGastoEntityTest` | 3 |
| `ViajeRepositoryTest` | **8** |
| `CategoriaGastoRepositoryTest` | 5 |
| `GastoRepositoryTest` | **11** |
| `ViajeViewModelTest` | 10 |
| `GastoViewModelTest` | 28 |
| `CalculadorRangoPeriodoTest` | **22** |
| `DashboardViewModelTest` | **20** |

### Instrumentadas (68)
| Archivo | Pruebas |
|---------|---------|
| `ViajeDaoTest` | **11** |
| `CategoriaGastoDaoTest` | 3 |
| `CategoriaUnicidadTest` | 3 |
| `GastoDaoTest` | **22** |
| `MigracionTest` | 1 |
| `ViajeComposeTest` | 8 |
| `GastoComposeTest` | **17** |
| `DashboardScreenTest` | **17** |

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

### Fase 5 — Dashboard y balance (completa)
- [x] DashboardViewModel (combinar ingresos y gastos, calcular ganancia neta).
- [x] DashboardUiState (ingresos, gastos, ganancia neta, periodo seleccionado, cargando).
- [x] DashboardScreen con resumen del periodo.
- [x] Selector visual de periodo (día / semana / mes).
- [x] Ruta `dashboard` en NavGraph.
- [x] Cambio de `startDestination` a Dashboard.
- [x] Accesos rápidos "Registrar viaje" y "Registrar gasto" desde Dashboard.
- [ ] Verificación manual en HONOR ALT-LX3.

### Otras fases
- [x] Editar y eliminar viajes (completado).
- [x] **Fase 6: Filtros, metas, dashboard y estadísticas — Completada ✅**
- [x] Fase 7: Preferencias (tema claro/oscuro) — Completada ✅
- [ ] Fase 8: Calidad y cierre del MVP (en progreso).
