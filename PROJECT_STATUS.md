# Estado del proyecto MiControlDiDi

> Actualizado: 12-sep-2026 — Incremento A del control diario cerrado (documentación únicamente: auditoría + ADR-001; sin cambios de código).
> Estado funcional vigente: 16-ago-2026 — Fase 8 cerrada: APK demo regenerado con el código vigente; 322/322 pruebas; push a origin/main completado.

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
| Unitarias | **194** | 194/194 (0 fallos) |
| Instrumentadas | **128** | 128/128 en ALT-LX3 (0 fallos) — incluye PersistenciaTest (8.4) y migración v3→v4 |
| **Total** | **322** | 0 fallos |

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
- [ ] **Control diario del trabajo — Incremento A ✅ completado (documentación); incrementos B–K ⛔ no autorizados** (ver sección siguiente).

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
| B | Perfil de trabajo | v4→v5 | ⛔ No autorizado |
| C | Horario laboral (dominio) | — | ⛔ No autorizado |
| D | Registro de jornada + checklist | v5→v6 | ⛔ No autorizado |
| E | Viaje adaptado a plataforma | v6→v7 | ⛔ No autorizado |
| F | Gasolina extra / tanqueos | v7→v8 | ⛔ No autorizado |
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

> **Estado:** A cerrado y a la espera de revisión. **Ningún incremento posterior está autorizado.**
