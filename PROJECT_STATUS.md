# Estado del proyecto MiControlDiDi

> Actualizado: 13-sep-2026 — Incrementos A, B, C, D, E y F del control diario cerrados y todo **validado en ALT-LX3** (383/383 unitarias + 148/148 instrumentadas, total 531).
> Estado funcional anterior: 16-ago-2026 — Fase 8 cerrada (MVP): 322/322 pruebas; APK demo regenerado; push a origin/main completado.

---

## Estado general

Capa de datos completa. Interfaz de viajes funcional. Gastos CRUD completo con edición, eliminación y protección ante ID inexistente. Navegación con barra inferior.

**Fase 5 completada.** Incrementos 1A–1D (dominio de periodos, consultas agregadas, DashboardViewModel y DashboardScreen con navegación) completados, validados e integrados en `main`.

- **113 pruebas unitarias** — instantánea histórica al 25-jul-2026 (valores vigentes: ver «Batería de pruebas»).
- **68 pruebas instrumentadas** — instantánea histórica al 25-jul-2026 (65 anteriores + 3 nuevas de FABs en DashboardScreen).
- **0 flaky pendientes.**

> **Las fases 1 a 6 y la Fase 8 están cerradas; la Fase 7 fue retirada del alcance por decisión de producto. El MVP está completo (ver «Fase 8 — Calidad y cierre del MVP», cerrada el 16-ago-2026).**

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

| Tipo | Cantidad | Estado |
|------|---:|---:|
| Unitarias | **383** | 383/383 (0 fallos) — 194 MVP + 49 B + 29 C + 37 D + 7 H-05/H-08 + 28 E + 39 F |
| Instrumentadas | **148** | 148/148 en ALT-LX3 (13-sep-2026), 0 omitidas — incluye migraciones v4→v5, v5→v6, v6→v7 y v7→v8 |
| **Total** | **531** | 531/531, 0 fallos |

> **Nota (25-jul-2026):**
>
> - Unitarias validadas tras integrar Incremento 1D: **113/113** (`testDebugUnitTest`, BUILD SUCCESSFUL).
> - Instrumentadas de esa fecha (Incremento 1B): **65/65** (`connectedDebugAndroidTest` en HONOR ALT-LX3).
> - 3 nuevas pruebas Compose para los FABs de acceso rápido. Pendiente `connectedDebugAndroidTest`.

## Fase 8 — Calidad y cierre del MVP (cerrada 16-ago-2026 ✅)

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

### 8.4 — Persistencia automatizada con BD Room temporal (01-ago-2026)

Se creó `PersistenciaTest` (instrumentado): a diferencia de los DAO tests (que usan `inMemoryDatabaseBuilder` y no prueban persistencia real), este test usa una **BD temporal en archivo** y verifica el ciclo cierre → reapertura con una instancia NUEVA de Room.

| Test | Verifica |
|---|---|
| `cerrarYReabrir_conservaViaje` | Viaje sobrevive al reabrir (valores, propina, observación, ingresoTotal) |
| `cerrarYReabrir_conservaGastoYCategorias` | Gasto + nombre de categoría sobreviven al reabrir |
| `cerrarYReabrir_conservaMetaActiva` | Meta activa sobrevive al reabrir |
| `cerrarYReabrir_conservaTodoElConjuntoDeDatos` | Viaje + gasto + meta + consulta agregada de ingresos tras reabrir |

- La BD temporal se limpia en `tearDown` (`deleteDatabase`).
- Se usan las migraciones 1→2→3→4 y el callback de prepoblado de categorías (misma configuración que la app real).
- Corrección en el camino: el test de gastos asumía que la primera categoría era "Gasolina", pero las categorías prepobladas no tienen orden garantizado; ahora compara contra el nombre de la categoría realmente usada.

**Validación:** `connectedDebugAndroidTest` en ALT-LX3 — clase PersistenciaTest 4/4; suite completa **128/128, 0 fallos**.

---

### 8.9 — Preparación de APK de demostración (01-ago-2026 · regenerado 16-ago-2026)

- `assembleRelease` BUILD SUCCESSFUL (valida R8/ProGuard con todo el código actual).
- APK de demo regenerado: `dist/MiControlDiDi-v1.0.0-demo.apk` (4.36 MB, firmado con debug key → instalable directamente; verificado: 130 entradas, `classes.dex`, `AndroidManifest.xml`, `resources.arsc`, firma META-INF presentes).
- Incluye el código vigente: Mejora 2 (dashboard fintech, modo claro) y correcciones 8.8 (filtros por fecha, paneles con scroll).
- `dist/` en `.gitignore` (artefacto de distribución, no se commitea).
- Nota: el APK release sin firmar requiere configuración de firma para producción; para prueba piloto se usa el APK de debug firmado.

**Validación:** `assembleRelease` BUILD SUCCESSFUL. Sin cambios de código en esta tarea.

---

### Backend — Enum PeriodoMeta (01-ago-2026)

Refactor de type-safety: se eliminaron las strings crudas `"DIA"`/`"MES"` del dominio de meta.

- Nuevo `domain/PeriodoMeta` (enum con `DIA`, `MES` y `fromNombre()`).
- `MetaEntity` conserva la columna `tipoPeriodo` TEXT (contrato de BD sin cambios) y expone la propiedad tipada `periodo` (degrada a `DIA` ante datos desconocidos).
- `MetaRepository.guardar/actualizar` ahora reciben `PeriodoMeta` y mapean a `nombre` al persistir.
- `MetaUiState.tipoPeriodo`, `MetaViewModel.seleccionarPeriodo`, `ConfigurarMetaScreen` y `DashboardScreen` usan el enum.
- Se eliminó la validación `require(periodo == "DIA" || "MES")` del repositorio (ahora garantizada por el tipo).
- Pruebas: 179 unitarias (2 nuevas: propiedad `periodo` de la entidad; test `fromNombre`).

**Validación:** `assembleDebug` BUILD SUCCESSFUL · `testDebugUnitTest` 179/179. Commit `f989047`.

---

### Backend — Índices de fechaHora + migración v3→v4 (01-ago-2026)

Optimización de consultas por rango de fechas (dashboard y filtros).

- `ViajeEntity` y `GastoEntity` declaran `@Index(value = ["fechaHora"])`.
- `MiControlDatabase` sube a versión 4 con `MIGRATION_3_4` que crea `index_viajes_fechaHora` e `index_gastos_fechaHora` (nombres exactos que Room genera, para que el identity hash coincida).
- `MigracionTest`: los tests v1→v2 y v2→v3 ahora incluyen `MIGRATION_3_4`; nuevo test v3→v4 que verifica datos conservados (viajes, gastos, meta) y existencia de ambos índices.

**Validación:** `assembleDebug` BUILD SUCCESSFUL · `testDebugUnitTest` 179/179 · `connectedDebugAndroidTest` en ALT-LX3 **124/124, 0 fallos** (incluye `migracionTresACuatro_creaIndicesFechaYConservaDatos`). Commits `f989047`, `0af17a6`.

**Corrección respecto al plan inicial:** la BD ya estaba en versión 3 (la v2→v3 creó `metas`), así que los índices implicaron v3→v4, no v2→v3.

---

### 8.5 — Revisión de accesibilidad (01-ago-2026)

Auditoría de contraste, etiquetas y áreas táctiles en todas las pantallas.

#### Contraste (WCAG AA ≥ 4.5:1) — todo pasa ✅

| Par | Claro | Oscuro |
|---|---|---|
| Primary / OnPrimary | 6.42:1 | 7.75:1 |
| PrimaryContainer / OnPC | 13.21:1 | 7.24:1 |
| Secondary / OnSecondary | 6.49:1 | 7.69:1 |
| OnBackground / Background | 16.79:1 | 13.30:1 |
| Error / OnError | 6.46:1 | 7.72:1 |
| Error sobre Background | 6.31:1 | 10.12:1 |
| onSurfaceVariant (M3) sobre Surface | 9.13:1 | 10.08:1 |
| tertiaryContainer / onTertiaryContainer (M3 default) | 13.32:1 | 7.23:1 |

#### Correcciones aplicadas (2 hallazgos)

1. **Dashboard — tarjeta de progreso de meta**: el icono de ajustes anunciaba "Configurar meta" (contentDescription) pero **no era clickable** (16dp, sin acción). Además, `onNavegarAConfigurarMeta` estaba cableado en `NavGraph` pero **nunca se pasaba** a `DashboardContent` (código muerto).
   - `DashboardContent` ahora recibe y propaga `onNavegarAConfigurarMeta` a `MetaProgressCard`.
   - El icono ahora es un `IconButton` (48dp de área táctil mínima) con `testTag` `boton_configurar_meta` que navega a `CONFIGURAR_META`.

2. **Settings — opciones de tema**: solo el `RadioButton` era clickable; el texto de la etiqueta no estaba asociado ni era parte del área táctil.
   - La fila completa ahora usa `Modifier.selectable(role = Role.RadioButton)` (48dp), el `RadioButton` pasa a `onClick = null` (la fila maneja el clic) y el texto queda dentro de la fila seleccionable.

#### Verificación adicional
- Todos los `Icon` con contenido informativo tienen `contentDescription`; el único `contentDescription = null` es el icono de `NavigationBarItem`, que ya tiene `label` (correcto por guía de Material 3).
- Sin colores hardcodeados en composables (todo vía `MaterialTheme.colorScheme`).
- Tipografía en `sp` (respeta escala de fuente del sistema).
- Observación (no bloqueante): el tema no define colores `tertiary`; se usan los default de M3, que pasan contraste, pero convendría alinearlos a la paleta verde en una tarea futura de identidad visual.

#### Validación
| Suite | Resultado |
|-------|:---------:|
| `assembleDebug` | BUILD SUCCESSFUL |
| `testDebugUnitTest` | **177/177, 0 fallos** |
| `connectedDebugAndroidTest` | No ejecutada (sin dispositivo conectado en esta sesión) |

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

### 8.7 — Verificación manual de persistencia (02-ago-2026)

**Formulación de cierre:** Fase 8.7 completada: persistencia manual aprobada. Se detectaron dos hallazgos funcionales ajenos a la persistencia que quedan pendientes de decisión para la Fase 8.8.

**Persistencia manual APROBADA** en ALT-LX3 (`APMDBB5616100336`, físico, sin emulador; `com.jhon.micontroldidi` 1.0.0/code 1, instalación no reemplazada, sin `pm clear` ni borrado de datos).

**Datos de control:** viaje 1× (02/08/2026 09:18 · $25.000 + $5.000 = $30.000 · “Viaje control 8.7”) · gasto 1× (Gasolina · 02/08/2026 09:24 · $20.000 · “Gasto control 8.7”) · meta 1 activa ($50.000 · Diaria · progreso 20%).

**Tres ciclos aprobados:** ① segundo plano/regreso (PID 26138→26138) · ② retirada de recientes (26138→vacío→5184; muerte real del proceso) · ③ force-stop `adb -s APMDBB5616100336 shell am force-stop com.jhon.micontroldidi` (5184→vacío→17877; reconstrucción correcta desde Room/Flows).

**Resultado:** viaje, gasto (con categoría Gasolina), meta y cálculos (dashboard DÍA/SEMANA/MES: $30.000/$20.000/$10.000; estadísticas correctas) íntegros tras los tres ciclos. Sin pérdidas, duplicados, campos alterados, registros inesperados, cierres inesperados ni pantallas vacías permanentes.

**Hallazgos abiertos (ajenos a la persistencia, sin corregir):**

- **H-8.7-01 — Filtro por fecha (gravedad MEDIA, fallo funcional):** con fecha inicial y final 02/08/2026 el gasto del 02/08 09:24 no apareció; al limpiar el filtro reaparece intacto. Causa raíz **no confirmada** (hipótesis de conversión UTC/zona local del DatePicker, expresamente no definitiva).
- **H-8.7-02 — Panel de filtros recortado en ALT-LX3 (gravedad MEDIA, defecto de UI):** los DatePicker expandidos ocupan la pantalla; chips de categoría no visibles; Aplicar/Cancelar recortados; filtro por categoría Gasolina no validable desde la UI. Reproducido solo en ALT-LX3.

**Fase 8.8:** ~~pendiente de autorización · recomendada · no iniciada~~ — **autorizada, implementada y validada el 16-ago-2026** (ver sección 8.8; instrumentadas 128/128 en ALT-LX3).

**Evidencias (fuera del repositorio):** `C:\Proyectos\MiControlDiDi_Evidencia_8.7\` — 8.7-C_linea_base (18) · 8.7-D_viaje_previo (11) · 8.7-E_gasto_previo (18) · 8.7-F_meta_estado_previo (19) · 8.7-G_cierres_reaperturas (44) = **110 evidencias**, inventariadas con SHA-256.

---

### 8.8 — Corrección de errores críticos: H-8.7-01, H-8.7-02 y H-8.8-03 (03-ago-2026)

**Auditoría técnica completada** (autorizada el 03-ago-2026): causa raíz confirmada en ambos hallazgos de la 8.7.

#### H-8.7-01 — Filtro por fecha excluye el día seleccionado (CONFIRMADO y CORREGIDO)

**Causa raíz:** `DatePickerState.selectedDateMillis` de Material 3 expone la fecha seleccionada como **medianoche UTC**. El código lo interpretaba como hora local del dispositivo (Bogotá, UTC-5), desplazando el día seleccionado un día hacia atrás: con inicial y final 02/08/2026 el rango real era `[01/08 00:00, 02/08 00:00)` y el gasto del 02/08 09:24 quedaba fuera.

**Corrección:** nuevo `util/CalculadorRangoFiltro.kt` que interpreta la selección como UTC (`Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()`) y calcula `atStartOfDay(zona)` en la zona del dispositivo. Aplicado en `FiltroGastosContent` (gastos) y `SelectorFechaContent` (viajes), eliminando la duplicación de `inicioDelDia`/`finDelDia` y la inicialización incorrecta del picker con filtros ya aplicados (`aUtcMedianoche`).

#### H-8.7-02 — Panel de filtros recortado en ALT-LX3 (CONFIRMADO y CORREGIDO)

**Causa raíz:** `FiltroGastosContent`/`SelectorFechaContent` eran `Column` fijos sin scroll con dos `DatePicker` expandidos + chips + botones, excediendo la altura de pantalla del ALT-LX3 (chips y Aplicar/Cancelar inaccesibles).

**Corrección:** ambos paneles ahora usan `Modifier.verticalScroll(rememberScrollState())`, dejando todo el contenido alcanzable por desplazamiento.

#### H-8.8-03 — Sin validación de rango inicio ≤ fin (NUEVO, CORREGIDO)

`CalculadorRangoFiltro.rangoFiltro()` devuelve `null` si la fecha inicial es posterior a la final; la UI muestra `error_filtro_rango_invalido` ("La fecha inicial debe ser anterior o igual a la fecha final") y no aplica el filtro. Aplicado en gastos y viajes.

#### Validación (03-ago-2026, sin dispositivo)

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ Correcto |
| `testDebugUnitTest` | ✅ **194/194** (179 previas + 15 nuevas de `CalculadorRangoFiltroTest`) |
| `connectedDebugAndroidTest` en ALT-LX3 | ✅ **128/128, 0 fallos, 0 omitidas** (16-ago-2026) |

**Pendiente:** ~~verificación manual en el físico del filtro por fecha (H-8.7-01) y del panel con scroll (H-8.7-02)~~ — descartada por decisión del usuario (16-ago-2026); la validación automatizada en ALT-LX3 (128/128) es la vigente.

---

### Estado funcional de gastos

| Funcionalidad | Implementado | Compilado | Pruebas unitarias | Pruebas DAO | Pruebas Compose | Verificación manual |
|---|---|---|---|---|---|---|
| Crear gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Listar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Eliminar gastos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| ID inexistente | ✅ | ✅ | ✅ | — | ❌ no implementada | — |

### Unitarias (113) — instantánea al 25-jul-2026
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

### Instrumentadas (68) — instantánea al 25-jul-2026
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

## Deuda técnica (vigente)
| Elemento | Detalle |
|---|---|
| `exportSchema` | `false` — Room 2.8.4 incompatible con Kotlin 2.1.20 |

## Deuda cerrada o mitigada
| Elemento | Detalle |
|---|---|
| Persistencia en dispositivo | Automatizada mediante `PersistenciaTest` en la Fase 8.4 (BD Room temporal en archivo: ciclo cierre → reapertura con instancia nueva de Room). Verificada manualmente durante la Fase 8.7 en ALT-LX3 (3 ciclos: segundo plano, retirada de recientes y force-stop). |

## Mejoras backend (01-ago-2026)
- ✅ Enum `PeriodoMeta` (type-safety en meta, sin cambios de BD) — commit `f989047`.
- ✅ Índices `fechaHora` en viajes/gastos + `MIGRATION_3_4` — commit `0af17a6`.
- ✅ Instrumentadas ejecutadas en ALT-LX3, incl. `migracionTresACuatro` (124/124 el 01-ago-2026; 128/128 tras 8.8 el 16-ago-2026).

## Mejora 2 — Rediseño del Dashboard y modo claro únicamente (02-ago-2026)

- **Identidad visual fintech aplicada al dashboard**: paleta semántica (azul = balance/acciones, verde = ingresos, rojo = gastos/errores, neutros = fondo/superficies), `GananciaHeroCard`, `ResumenCard` con iconos, tarjetas sin elevación, `FilterChip` con borde, `MetaProgressCard` reestilizada.
- **Esquema de color claro exclusivamente**: solo `lightColorScheme`; la app no responde al modo oscuro del sistema ni a colores dinámicos.
- **Eliminación de la selección y gestión de tema**: retirados `ThemePreferenceManager.kt`, `ThemeMode` (persistencia, StateFlow y propagación) y el selector de tema en Configuración; eliminados 4 recursos `settings_tema_*`.
- Fase 7 retirada del alcance por decisión de producto. Se eliminó la funcionalidad de selección de tema y la aplicación conserva únicamente el esquema claro.

### Validación (02-ago-2026)

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ Correcto |
| `assembleRelease` con R8 | ✅ Correcto |
| `testDebugUnitTest` | ✅ 179/179 |
| `connectedDebugAndroidTest` en ALT-LX3 | ✅ 128/128 |
| **Total** | **307/307, 0 fallos y 0 pruebas omitidas** |

## Pendiente

### Fase 5 — Dashboard y balance (completa)
- [x] DashboardViewModel (combinar ingresos y gastos, calcular ganancia neta).
- [x] DashboardUiState (ingresos, gastos, ganancia neta, periodo seleccionado, cargando).
- [x] DashboardScreen con resumen del periodo.
- [x] Selector visual de periodo (día / semana / mes).
- [x] Ruta `dashboard` en NavGraph.
- [x] Cambio de `startDestination` a Dashboard.
- [x] Accesos rápidos "Registrar viaje" y "Registrar gasto" desde Dashboard.
- [x] Verificación manual en ALT-LX3 — descartada por decisión del usuario (16-ago-2026); la validación automatizada en ALT-LX3 es la vigente.

### Otras fases
- [x] Editar y eliminar viajes (completado).
- [x] **Fase 6: Filtros, metas, dashboard y estadísticas — Completada ✅**
- [x] Fase 7: Preferencias (tema claro/oscuro) — **Retirada del alcance por decisión de producto. Se eliminó la funcionalidad de selección de tema y la aplicación conserva únicamente el esquema claro.**
- [x] **Fase 8: Calidad y cierre del MVP — Cerrada el 16-ago-2026 ✅** (322/322 pruebas; APK demo regenerado; push a origin/main; verificación manual descartada por decisión del usuario).
- [x] **Control diario del trabajo — Incrementos A ✅ (documentación), B ✅ (perfil de trabajo), C ✅ (horario laboral), D ✅ (registro de jornada), E ✅ (viaje adaptado) y F ✅ (tanqueos y cierre de jornada); 383/383 unitarias y 148/148 instrumentadas; incrementos G–K ⛔ no autorizados** (ver secciones siguientes).

---

## Adaptación al control diario — Incremento A (12-sep-2026)

**Naturaleza del incremento: exclusivamente documental.** No se modificó código funcional, ni Gradle, ni Room, ni pruebas, ni APK; no se crearon migraciones. La base de datos permanece en **versión 4** con las mismas cuatro tablas.

### Entregables

| Documento | Contenido |
|---|---|
| `docs/AUDITORIA_CONTROL_DIARIO.md` | Auditoría del estado real de MiControlDiDi, brecha frente a la propuesta, conflictos con `AGENTS.md`, plan desglosado en incrementos, riesgos técnicos y condiciones de cierre |
| `docs/ADR-001-control-diario.md` | Decisiones de arquitectura D-01 a D-19 (contexto de trabajo, gasolina y doble conteo, recordatorios, tema, modelo de kilómetros, unidades, indicadores derivados, GPS, migraciones y alcance del propio incremento) |

### Decisiones registradas (resumen)

- Plataforma principal **inDrive**; vehículo **TVS Raider 125 FI**; combustible **gasolina extra**; ciudad **Medellín**; horario **lunes a viernes, 6:00 a. m. a 3:00 p. m.** (valores iniciales del perfil de trabajo, incremento B).
- El tanqueo **genera o enlaza** un `GastoEntity` de categoría Gasolina, con relación identificable y sincronización al editar o eliminar (evita doble conteo).
- Recordatorios **internos** (banners y tarjetas) en la primera versión; **sin** notificaciones del sistema, `WorkManager` ni permisos nuevos.
- Tema **exclusivamente claro**; no se restaura el modo oscuro.
- Modelo de **odómetro**: totales = final − inicial; con pasajero = suma de distancias de viajes; vacíos = totales − con pasajero. Tramos vacíos opcionales en etapa posterior.
- Distancias en **metros (`Long`)**; dinero en **`Long`**.
- Indicadores derivados (rendimiento, costo por kilómetro, netos) **se calculan y no se persisten**.
- **Sin GPS** y **sin `destructiveMigration`**.

### Roadmap propuesto (estado)

| # | Incremento | Migración | Estado |
|---|---|---|---|
| A | Auditoría documentada + ADR | — | ✅ Completado |
| B | Perfil de trabajo | v4→v5 | ✅ Implementado (13-sep-2026) |
| C | Horario laboral (dominio) | — | ✅ Implementado (13-sep-2026) |
| D | Registro de jornada + checklist | v5→v6 | ✅ Implementado (13-sep-2026) |
| E | Viaje adaptado a plataforma | v6→v7 | ✅ Implementado (13-sep-2026) |
| F | Gasolina extra / tanqueos y cierre | v7→v8 | ✅ Implementado (13-sep-2026) |
| G | Kilómetros y netos (dominio) | — | ⛔ No autorizado |
| H | Bienestar | v8→v9 | ⛔ No autorizado |
| I | Dashboard adaptado | — | ⛔ No autorizado |
| J | Reportes ampliados | — | ⛔ No autorizado |
| K | Calidad y cierre | — | ⛔ No autorizado |

### Validación

| Verificación | Resultado |
|---|---|
| Cambios en código funcional | Ninguno (solo documentación en `docs/`, `PROJECT_STATUS.md` y `TASKS.md`) |
| Migraciones nuevas | Ninguna |
| Versión de la base de datos | Sin cambios (v4) |
| Batería de pruebas | No ejecutada: no hubo cambios de código (estado vigente 322/322) |

> **Estado:** A, B, C, D, E y F cerrados. **Ningún incremento posterior a F está autorizado.**

---

## Incremento B — Perfil de trabajo (13-sep-2026)

**Naturaleza del incremento: código funcional.** Base de datos **v4 → v5** con migración explícita. Sin dependencias nuevas, sin GPS, sin notificaciones y sin red.

### Implementado

- **`PerfilTrabajoEntity`** (tabla `perfil_trabajo`, fila única con `id = 1`): plataforma principal, plataformas disponibles, vehículo, tipo de combustible, ciudad, días laborales (CSV con numeración ISO 1–7), hora de inicio y de fin en minutos desde la medianoche, porcentaje máximo de kilómetros vacíos y, para cada uno de los seis mantenimientos, su costo y su intervalo en kilómetros.
- **Reserva por kilómetro calculada, no persistida** (ADR-001 D-16): `reservaPorKm = costo ÷ intervalo`, con el total de los seis mantenimientos como propiedad derivada. Los intervalos se guardan en kilómetros (`Long`), coherente con D-14.
- **`PerfilTrabajoDao`**: observar, obtener y guardar (upsert con `REPLACE`), siempre sobre la fila única.
- **`PerfilTrabajoRepository`** con validaciones: plataforma, vehículo y ciudad obligatorios; al menos un día laboral; horas dentro del día y con inicio anterior al fin; porcentaje entre 0 y 100; costos no negativos y, si hay costo, intervalo mayor que cero. El guardado fuerza el id único.
- **`PerfilTrabajoViewModel`** con `Clock` inyectable (patrón del proyecto): carga el perfil —o los valores por defecto si todavía no existe fila—, valida el formulario en cada edición, guarda y expone `reservaTotalPorKm`.
- **`ConfigurarPerfilScreen`** con secciones de datos del trabajo, horario (chips de días), umbral de kilómetros vacíos y reservas; accesible desde **Configuración** mediante la nueva ruta `configurar_perfil`.
- **Migración `MIGRATION_4_5`**: crea `perfil_trabajo` y siembra el perfil por defecto partiendo de los valores por defecto de la entidad, de modo que migración y entidad no puedan desincronizarse. El callback de creación hace lo mismo en instalaciones nuevas.
- **`strings.xml`**: 44 cadenas nuevas (etiquetas, secciones, días y mensajes de validación). Sin textos visibles hardcodeados.

### Decisiones del incremento

- La **meta diaria no se duplica**: sigue viviendo en la tabla `metas` del MVP.
- **Costos e intervalos** son datos de entrada; la reserva por kilómetro y su total son derivados y no se persisten.
- El perfil se siembra con los valores del ADR-001 (inDrive, TVS Raider 125 FI, gasolina extra, Medellín, lunes a viernes de 06:00 a 15:00, umbral 25 %); el usuario puede editarlos.
- Se añadió una **pantalla propia** (`ConfigurarPerfilScreen`) en lugar de incrustar el formulario en Configuración, siguiendo el patrón ya existente de «Configurar meta».

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL — `app-debug.apk` regenerado |
| `testDebugUnitTest` | ✅ **243/243, 0 fallos, 0 omitidas** (194 previas + 49 nuevas) |
| Pruebas nuevas | `PerfilTrabajoEntityTest` (13), `PerfilTrabajoRepositoryTest` (18), `PerfilTrabajoViewModelTest` (18) |
| `MigracionTest` | Ampliado: nueva prueba **v4→v5** y `MIGRATION_4_5` añadida a las tres pruebas existentes |
| `PersistenciaTest` | Actualizado con `MIGRATION_4_5` (mantiene la paridad con la configuración real de la app) |
| `connectedDebugAndroidTest` en ALT-LX3 | ✅ **129/129, 0 fallos** (13-sep-2026), incluida la migración v4→v5 |

### Incidencia encontrada y corregida durante el incremento

La primera corrida de `testDebugUnitTest` falló con **1 prueba de 243**: una aserción mal escrita en `PerfilTrabajoViewModelTest` (se esperaba el intervalo de mantenimiento como reserva por kilómetro). Se corrigió la prueba —no el código de producción— y la corrida siguiente quedó **243/243**. No se desactivó ni omitió ninguna prueba.

> **Validado en dispositivo (13-sep-2026):** `connectedDebugAndroidTest` en **ALT-LX3**: **129/129, 0 fallos, 0 omitidas**. La migración **v4→v5** quedó verificada sobre una base SQLite real (`migracionCuatroACinco_creaPerfilTrabajoYConservaDatos`), con los viajes, gastos y metas anteriores conservados y el perfil sembrado.

---

## Incremento C — Horario laboral (13-sep-2026)

**Naturaleza del incremento: dominio puro.** Sin cambios en la base de datos, sin pantallas nuevas y sin dependencias. Prepara el cálculo que consumirán la jornada (incremento D), los recordatorios internos (H) y el dashboard (I).

### Implementado

- **`BloqueJornada`** (`domain/`): los ocho bloques del horario con su tipo (trabajo o pausa) y su duración — principal 150 min, primera pausa 15, segundo bloque 135, snack y revisión 15, bloque selectivo 75, almuerzo 30, bloque final 90 y regreso productivo 30. Total **540 minutos**: 480 de trabajo y 60 de pausa.
- **`EstadoHorarioLaboral`** (`domain/`): bloque actual, hora de fin del bloque, siguiente pausa, progreso (0–100), minutos conectado, minutos en pausa, minutos restantes, modo regreso y advertencia de finalización.
- **`CalculadorHorarioLaboral`** (`domain/`): función pura que, dada la hora actual y el horario del perfil, devuelve el estado. Los bloques se **anclan a la hora de inicio del perfil**, así que todo el horario se desplaza cuando el conductor cambia su hora de inicio; los rangos son semiabiertos y una jornada más corta recorta los últimos bloques.
- **La jornada nunca se cierra sola** (requisito de la FASE 3): el calculador solo informa. El modo regreso se activa durante el bloque de regreso productivo (14:30–15:00 por defecto) y la advertencia de finalización desde la hora de fin.

### Decisiones del incremento

- Los bloques **no se guardan en la base de datos**: son reglas del dominio ancladas al horario del perfil, así que cambiar la hora de inicio no exige migración.
- El calculador recibe `LocalTime` y devuelve un estado inmutable: no depende de Android, de Room ni del reloj, y por eso es directo de probar.
- La hora del dispositivo se usará en el incremento que consuma el calculador, con `Clock` inyectable como ya hace el Dashboard.

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL — `app-debug.apk` regenerado |
| `testDebugUnitTest` | ✅ **272/272, 0 fallos, 0 omitidas** (243 previas + 29 nuevas) |
| Pruebas nuevas | `CalculadorHorarioLaboralTest` (29 pruebas) |
| Cambios en la base de datos | Ninguno (sigue en v5) |
| `connectedDebugAndroidTest` en ALT-LX3 | ✅ **129/129, 0 fallos** (13-sep-2026) |

### Incidencia encontrada y corregida durante el incremento

La primera corrida falló **4 pruebas de 272**: cuatro expectativas mal sumadas en las propias pruebas (minutos de trabajo acumulados a las 11:15, 12:30, 13:00 y 14:30). Se verificó contra el reporte que el calculador devolvía los valores correctos (285, 360, 360 y 450) y se corrigieron **las pruebas**, no el código. Corrida siguiente: **272/272**. No se desactivó ni omitió ninguna prueba.

### Cobertura de la FASE 3

| Requisito | Estado |
|---|---|
| Bloque actual | ✅ `bloqueActual` |
| Hora de finalización del bloque | ✅ `finBloque` |
| Siguiente pausa | ✅ `proximaPausa` (se salta la pausa en curso) |
| Progreso de la jornada | ✅ `progresoPorcentaje` (0–100) |
| Tiempo conectado | ✅ `minutosConectado` (480 al completar el horario) |
| Tiempo en pausa | ✅ `minutosEnPausa` (60 al completar el horario) |
| Tiempo restante | ✅ `minutosRestantes` |
| Mensaje de modo regreso desde 2:30 p. m. | ✅ `modoRegreso` |
| Advertencia de finalización a las 3:00 p. m. | ✅ `advertenciaFinalizacion` |
| No obligar a finalizar la jornada | ✅ solo informa; no existe ninguna acción de cierre |
| Mostrarlo en la interfaz | ⏳ Pendiente: llega con la jornada (D) y el dashboard (I) |

> **Validado en dispositivo (13-sep-2026):** `connectedDebugAndroidTest` en **ALT-LX3**: **129/129, 0 fallos, 0 omitidas**. Con esta corrida quedó probada también la migración v4→v5 del incremento B sobre una base SQLite real.

---

## Incremento D — Registro de jornada (13-sep-2026)

**Naturaleza del incremento: código funcional.** Base de datos **v5 → v6** con migración explícita. Sin dependencias nuevas, sin GPS, sin notificaciones y sin red.

### Implementado

- **`JornadaEntity`** (tabla `jornadas`): fecha y hora de inicio automáticas, odómetro inicial (**en metros**, coherente con el resto de distancias), nivel aproximado de combustible, precio del galón extra, zona inicial, plataforma, meta bruta del día, energía (0–10), clima, observaciones y los **doce puntos de la revisión previa** como columnas booleanas (true = en buen estado).
- **`PuntoRevision`** (`domain/`): los doce puntos con la marca `critico` en **llantas, frenos y luces**.
- **`NivelCombustible`** (`domain/`): reserva, 1/4, 1/2, 3/4 y lleno. Es **aproximado a propósito**: el consumo real se medirá con los tanqueos, no con las barras del indicador.
- **`JornadaDao`**: insertar, observar todas y observar la última.
- **`JornadaRepository`** con validaciones: odómetro no negativo, precio del galón mayor que cero, meta del día mayor que cero, energía entre 0 y 10 y plataforma obligatoria.
- **`JornadaViewModel`** con `Clock` inyectable: prellena la plataforma con la del perfil, detecta si ya hay una jornada registrada **hoy** y valida el formulario campo por campo.
- **`RegistrarJornadaScreen`**: datos del inicio, chips de combustible, los doce puntos de la revisión y un **aviso claro cuando frenos, llantas o luces están en mal estado**. El aviso **no bloquea** el guardado (requisito de la FASE 4): el botón sigue habilitado.
- **Ruta** `registrar_jornada`, accesible desde **Configuración**.
- **`DateFormatter.formatHora`**: hora en formato `HH:mm` para el aviso de jornada ya registrada.
- **Migración `MIGRATION_5_6`**: crea la tabla `jornadas`. No siembra filas.
- **`strings.xml`**: 37 cadenas nuevas.

### Decisiones del incremento

- **Los doce puntos de la revisión y los valores derivados** (puntos en mal estado, críticos pendientes) se calculan; no se persisten.
- **La meta bruta del día se guarda en la jornada** porque es un dato del día que el conductor escribe al arrancar. La tabla `metas` del MVP sigue siendo la meta general que usa el dashboard.
- **El cierre de la jornada aún no existe**: guardar el odómetro final corresponde al cálculo de kilómetros (incremento F), junto con `fechaHoraFin`. Por eso la jornada no expone todavía ninguna acción de cierre.
- **La entrada está en Configuración** por ahora; el acceso destacado desde el dashboard —con el bloque actual y el modo regreso en vivo— llega con el incremento I.
- La revisión **avisa pero no impide**: sin bloqueos técnicos, como pide la especificación.

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL — `app-debug.apk` regenerado |
| `testDebugUnitTest` | ✅ **309/309, 0 fallos, 0 omitidas** (272 previas + 37 nuevas) |
| Pruebas nuevas | `JornadaEntityTest` (10), `JornadaRepositoryTest` (11), `JornadaViewModelTest` (16) |
| `MigracionTest` | Ampliado: nueva prueba **v5→v6** y `MIGRATION_5_6` añadida a las cuatro pruebas existentes |
| `PersistenciaTest` | Actualizado con `MIGRATION_5_6` |
| `connectedDebugAndroidTest` en ALT-LX3 | ✅ **130/130, 0 fallos, 0 omitidas** (13-sep-2026) |

### Incidencias encontradas y corregidas durante el incremento

1. **Error de compilación** (detectado al compilar): se llamaba a una función `@Composable` dentro de un lambda de `joinToString` para listar los puntos críticos. Se corrigió resolviendo los nombres con `LocalContext` fuera del lambda.
2. **Bug detectado antes de probar**: al prellenar la plataforma desde el perfil no se recalculaban los errores de validación, así que el botón de guardar habría quedado deshabilitado para siempre. Se corrigió revalidando tras el prellenado.

Ninguna prueba se desactivó ni se omitió.

### Cobertura de la FASE 4

| Requisito | Estado |
|---|---|
| Fecha y hora inicial automáticas | ✅ tomadas del reloj al guardar |
| Kilometraje inicial | ✅ (guardado en metros) |
| Nivel aproximado de combustible | ✅ cinco niveles |
| Precio actual de gasolina extra | ✅ |
| Zona inicial | ✅ |
| Plataforma utilizada | ✅ prellenada desde el perfil |
| Meta bruta del día | ✅ |
| Nivel de energía de 0 a 10 | ✅ validado |
| Clima | ✅ |
| Observaciones | ✅ |
| Lista de revisión de 12 puntos | ✅ |
| Advertencia en frenos, llantas y luces | ✅ visible y **no bloqueante** |
| Ver la jornada en el dashboard | ⏳ llega con el incremento I |
| Cierre de jornada con odómetro final | ⏳ llega con los kilómetros (incremento F) |

---

## Estabilización previa al incremento E (13-sep-2026)

**Naturaleza: correcciones puntuales.** Sin cambios en la base de datos (sigue en **v6**), sin migraciones, sin dependencias nuevas, sin GPS, sin notificaciones y sin red. Se corrigieron los cinco hallazgos de la auditoría completa (`docs/AUDITORIA_COMPLETA_2026-09-13.md`) que el responsable autorizó, uno por commit.

### Hallazgos cerrados

| ID | Hallazgo | Corrección |
|---|---|---|
| H-02 | Paleta XML vestigial verde y `statusBarColor` desalineado del `primary` azul de Compose | `colors.xml` reducido a `barra_estado` (#FF2563EB) y `fondo_ventana` (#FFF8FAFC), alineados con `ui/theme/Color.kt`; `themes.xml` fija además `windowLightStatusBar=false` y `windowBackground`. El nombre del estilo no cambia. |
| H-06 | Los tres primeros ítems de la barra inferior compartían icono | Dashboard → `Icons.Filled.Home`; Viajes conserva `Icons.AutoMirrored.Filled.List`; Gastos → `Icons.Filled.ShoppingCart`. Solo iconos de `material-icons-core` (sin dependencias nuevas); los `testTag` no cambian. |
| H-05 | Al editar un viaje se mostraba la fecha actual en lugar de la original | La fecha informativa sale de `CalculadorFechaFormularioViaje` (función pura): en creación la actual, en edición la original y, mientras la edición carga, ninguna. Nuevo `testTag` `fecha_viaje`. |
| H-08 | `JornadaViewModel` tragaba en silencio el error al cargar el perfil (`catch { }`) | El error publica `mensajeError` con `error_cargar_perfil`; los dos `catch` del `init` relanzan `CancellationException` (coherencia con dashboard, gasto y viaje). Se reutiliza una cadena existente. |
| H-01 | `README.md` desactualizado y contradictorio con este documento | Reescrito al estado real: v6, 6 tablas, 5 migraciones, `dashboard` como origen, 316/131 pruebas, incrementos A–D cerrados y E–K no autorizados. |

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL tras cada hallazgo |
| `testDebugUnitTest` | ✅ **316/316, 0 fallos, 0 omitidas** (309 previas + 4 de H-05 + 3 de H-08) |
| `connectedDebugAndroidTest` en ALT-LX3 (Android 14, API 34) | ✅ **131/131, 0 fallos, 0 omitidas** (130 previas + 1 nueva de H-05) |
| Versión de la base de datos | Sin cambios (**v6**); ninguna migración nueva |
| Dependencias nuevas | Ninguna |
| Pruebas desactivadas u omitidas | Ninguna |

### Incidencia encontrada y corregida

La prueba instrumentada nueva de H-05 usaba `assertTextMatches`, que no existe en la versión de `ui-test-junit4` del proyecto: falló la compilación de `debugAndroidTest` (`Unresolved reference`) y se corrigió **la prueba** (verifica el formato `dd/MM/yyyy HH:mm` leyendo la semántica del nodo). No se desactivó ni se omitió ninguna prueba; la corrida siguiente quedó **131/131**.

### Decisiones pendientes

- **H-02:** ALT-LX3 es Android 14 (API 34), así que `statusBarColor` **sí** se aplica ahí; en Android 15+ (API ≥ 35) el sistema lo ignora por el modo extremo a extremo obligatorio. El rediseño edge-to-edge sigue fuera de alcance.
- **H-06:** no hay prueba automática que impida volver a repetir un icono; la verificación es visual.
- **H-01:** los conteos del `README.md` son una instantánea y remiten a este documento como fuente única.
- **Incrementos E–K:** E quedó implementado; **F–K siguen ⛔ no autorizados**.

---

## Incremento E — Viaje adaptado a inDrive (13-sep-2026)

**Naturaleza: código funcional.** Base de datos **v6 → v7** con migración explícita. Sin dependencias nuevas, sin GPS, sin permisos nuevos y sin red. `valor` sigue siendo el precio cobrado y `ingresoTotal = valor + propina` no cambia.

### Implementado

- **`ViajeEntity`**: cinco campos nuevos — `plataforma` (texto editable), `zona` (texto), `distanciaMetros` (`Long`, en metros), `formaPago` (texto estable del catálogo `FormaPago`), `peaje` (`Long`).
- **`FormaPago`** (`domain/`): catálogo cerrado **EFECTIVO, TRANSFERENCIA, TARJETA, OTRO**, con `fromNombre` tolerante a mayúsculas y `null` para vacío o valor desconocido.
- **Migración `MIGRATION_6_7`**: cinco `ALTER TABLE` sobre `viajes` con `DEFAULT` compatible (`''` y `0`), sin tablas nuevas, sin recrear la tabla, conservando las seis tablas y el índice `index_viajes_fechaHora`.
- **`peaje`** es un dato informativo del viaje: no se suma a `ingresoTotal`, no se resta de la ganancia, no crea ningún gasto y puede valer `0`.
- **Reglas de validación:** un viaje **nuevo** exige plataforma, zona y forma de pago; `distanciaMetros >= 0` y `peaje >= 0`. La forma de pago debe pertenecer al catálogo o venir vacía (viajes migrados).
- **Viajes migrados:** quedan con textos vacíos y ceros; se **visualizan y editan sin errores** (la exigencia de datos de plataforma solo aplica a viajes nuevos).
- **`ViajeViewModel`**: prellena la plataforma desde `PerfilTrabajoRepository` sin pisar lo que el usuario escriba y sin bloquear el registro si el perfil falla (avisa con un mensaje).
- **UI:** cinco campos nuevos en `RegistrarViajeScreen` (plataforma, zona, distancia en km, forma de pago y peaje) y los detalles de plataforma, zona y distancia en la tarjeta de `ListaViajesScreen`. Cadenas nuevas en `strings.xml`; ningún texto visible hardcodeado.

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL |
| `testDebugUnitTest` | ✅ **344/344, 0 fallos, 0 omitidas** |
| `connectedDebugAndroidTest` en ALT-LX3 (Android 14, API 34) | ✅ **142/142, 0 fallos, 0 errores, 0 omitidas** (corrida no solapada, 13-sep 10:58) |
| Pruebas nuevas | 28 unitarias (6 de catálogo, 6 de entidad, 6 de repositorio, 10 de ViewModel) y 11 instrumentadas (5 de DAO, 1 de migración v6→v7, 3 de formulario, 2 de lista) |
| Base de datos | **v7**; `MIGRATION_6_7` declarada y registrada; 6 tablas; ninguna migración destructiva |
| Dependencias, GPS y permisos | Sin cambios |
| Pruebas desactivadas u omitidas | Ninguna |

### Incidencia de proceso (no es un defecto)

Durante la implementación se lanzaron **varias corridas instrumentadas solapadas** sobre ALT-LX3. La reinstalación e interferencia entre procesos de esas corridas produjo fallos espurios en `DashboardScreenTest` (ajenos a este incremento). **No son defectos de producción ni de las pruebas nuevas**: la única corrida **no solapada** terminó **142/142, 0 fallos**. Regla adoptada: **una sola corrida instrumentada a la vez** sobre el dispositivo.

### Resolución de contradicción F/G

- **Contradicción documental F/G resuelta:** el **cierre de jornada con odómetro final y hora de fin** quedó implementado e integrado en el incremento **F** junto con la base de datos **v8** (columnas `fechaHoraFin` y `kilometrajeFinalMetros` en `jornadas`). El incremento **G** se limita exclusivamente a la capa de dominio de **kilómetros vacíos, semáforo de eficiencia y cálculos netos** (operativo y económico).

---

## Incremento F — Gasolina extra, tanqueos y cierre de jornada (13-sep-2026)

**Naturaleza: código funcional.** Base de datos **v7 → v8** con migración explícita. Sin dependencias nuevas, sin GPS, sin permisos nuevos y sin red.

### Implementado

- **`TanqueoEntity`** (tabla `tanqueos`): id, fechaHora, odometroMetros (`Long`), litrosMililitros (`Long`), importePagado (`Long`), esLleno (`Boolean`), tipoCombustible (`String`), observacion (`String`), gastoId (`Long`, FK → `gastos.id` con `ON DELETE RESTRICT`).
  - Litros y precio por litro son propiedades calculadas de dominio (`litros` y `precioLitro`); no se persisten.
  - Dinero en `Long` entero y distancias en metros (`Long`); sin `Double` ni `Float`.
- **Atomicidad y sincronización tanqueo-gasto:**
  - `TanqueoDao`: DAO coordinador con transacciones Room (`@Transaction`).
  - Crear tanqueo crea atómicamente su `GastoEntity` de categoría Gasolina y enlaza `gastoId`.
  - Editar tanqueo actualiza atómicamente los datos del gasto vinculado (valor, fechaHora, observación).
  - Eliminar tanqueo elimina primero el tanqueo hijo y luego su gasto padre en la misma transacción.
  - La FK `ON DELETE RESTRICT` impide borrar directamente el gasto desde la base de datos mientras exista el tanqueo.
  - Evita doble conteo: el combustible vive una sola vez como gasto en la contabilidad general.
- **Protección en la interfaz de gastos:**
  - `GastoDao.obtenerTodos()` y `obtenerPorId()` marcan `esTanqueo = (t.id IS NOT NULL)` mediante `LEFT JOIN tanqueos`.
  - `ListaGastosScreen` muestra etiqueta identificativa y deshabilita los botones de editar y eliminar para gastos de tanqueo.
  - `GastoViewModel` bloquea `cargarGastoParaEditar` y `mostrarDialogoEliminar` ante gastos vinculados a tanqueos, mostrando mensaje informativo.
- **Cierre de jornada (completado en F):**
  - Columnas `fechaHoraFin` y `kilometrajeFinalMetros` agregadas a `jornadas` en migración `MIGRATION_7_8` (0 por defecto, lo que conserva jornadas anteriores abiertas).
  - `jornadaDao.cerrar` valida atómicamente en SQL `WHERE id = :id AND fechaHoraFin = 0`, garantizando **cierre único**.
  - `JornadaRepository.cerrar` valida fecha fin ≥ fecha inicio y odómetro final ≥ inicial.
  - `JornadaViewModel` y `RegistrarJornadaScreen` exponen la sección de cierre con odómetro final y hora automática del reloj.
- **Pantallas y navegación:**
  - `ListaTanqueosScreen` (LazyColumn, card con fecha, litros, importe, odómetro, switch de lleno, botones editar y eliminar con diálogo de confirmación).
  - `RegistrarTanqueoScreen` (odómetro, litros, importe, selector de combustible prellenado desde el perfil de trabajo, switch de lleno y validaciones).
  - Rutas `lista_tanqueos`, `registrar_tanqueo` y `registrar_tanqueo/{tanqueoId}` en `NavGraph`.
  - Acceso directo desde `SettingsScreen`.
- **Migración `MIGRATION_7_8`:**
  - Crea tabla `tanqueos` con FK hacia `gastos` e índices sobre `fechaHora` y `gastoId`.
  - `ALTER TABLE jornadas` para añadir `fechaHoraFin` y `kilometrajeFinalMetros`.
  - Sin `destructiveMigration` y con conservación estricta de datos previos.

### Validación

| Verificación | Resultado |
|---|---|
| `assembleDebug` | ✅ BUILD SUCCESSFUL |
| `testDebugUnitTest` | ✅ **383/383, 0 fallos, 0 omitidas** (344 previas + 39 nuevas de F) |
| `connectedDebugAndroidTest` en ALT-LX3 (Android 14, API 34) | ✅ **148/148, 0 fallos, 0 errores, 0 omitidas** (corrida no solapada) |
| Pruebas nuevas unitarias (39) | `TanqueoEntityTest` (5), `TanqueoRepositoryTest` (8), `TanqueoViewModelTest` (8), `GastoVinculadoTest` (4), `JornadaEntityCierreTest` (3), `JornadaCierreRepositoryTest` (6), `JornadaCierreViewModelTest` (5) |
| Pruebas nuevas instrumentadas (6) | `MigracionTest` (1: prueba v7→v8 `migracionSieteAOcho_creaTanqueosYAnadeCierreDeJornada`), `TanqueoComposeTest` (4), más actualización de `PersistenciaTest` con `MIGRATION_7_8` |
| Base de datos | **v8**; `MIGRATION_7_8` registrada; 7 tablas; 0 migraciones destructivas |
| Dependencias, GPS y permisos | Sin cambios |
| Pruebas desactivadas u omitidas | Ninguna |

### Incidencias encontradas y corregidas durante la validación

1. **`MigracionTest`**: en el paso 9 de `migracionSieteAOcho_creaTanqueosYAnadeCierreDeJornada`, la aserción esperaba `0` gastos tras eliminar el tanqueo, pero el gasto previo de control (18.000) persistía intacto. Se corrigió la expectativa a `1` gasto conservado.
2. **`TanqueoComposeTest`**: la prueba `errorDeCarga_muestraEstadoDeError` fijaba `errorCarga = true` después de que el ViewModel ya había recolectado el flow en `setUp()`. Se corrigió reinicializando el ViewModel para que recolectara el flujo con error.

