# Auditoría completa — MiControlDiDi

> **Informe de auditoría en modo solo lectura.** Generado el 13-sep-2026.
> No se modificó código, Gradle, Room, recursos, pruebas ni configuración. Este archivo es el único
> artefacto nuevo de la auditoría.
> Alcance: repositorio completo `C:\Proyectos\MiControlDiDi` (workspace + proyecto Android en `app/`).

---

## 1. Resumen ejecutivo

MiControlDiDi es una app Android local (Kotlin + Compose + Material 3 + Room + MVVM + Repository) que
controla ingresos, gastos y balance de un conductor de moto. El MVP está cerrado y el proyecto está
ahora en la **adaptación al control diario del trabajo** (inDrive), con los incrementos A–D cerrados y
los incrementos E–K sin autorizar.

**Veredicto general: sólido y coherente con las reglas del proyecto.** La arquitectura por capas se
respeta en todo el código, el dinero se maneja en `Long`, no hay red, no hay `destructiveMigration` y la
batería de pruebas cuadra con lo documentado (309 unitarias + 130 instrumentadas = **439**, 0 fallos
según el log de la última corrida).

**Los tres hallazgos que más importan:**

| # | Hallazgo | Gravedad |
|---|---|---|
| H-01 | `README.md` está gravemente desactualizado (dice «Fase 5 en desarrollo», DB v2, 113/65 pruebas, `startDestination = lista_viajes`) y contradice `PROJECT_STATUS.md`. | Media |
| H-02 | `res/values/colors.xml` y `themes.xml` conservan la paleta verde antigua (`#FF1A6B52`) mientras `ui/theme/Color.kt` usa la paleta fintech azul. La `statusBarColor` no coincide con el `primary` real. | Media |
| H-03 | No hay integración continua (`.github/`, CI) y ninguna automatización ejecuta unitarias/instrumentadas; toda la validación es manual en el dispositivo físico ALT-LX3. | Media |

**Datos clave verificados:** Room v6 · 6 tablas · 5 migraciones explícitas · `exportSchema = false` ·
`minSdk 26` / `targetSdk 36` · sin permiso `INTERNET` · working tree limpio · `main` == `origin/main`
(último commit `22e09be`).

---

## 2. Alcance y método

**Modo:** solo lectura. No se compiló, no se ejecutaron pruebas, no se tocó ningún archivo del proyecto.
Toda afirmación se verificó leyendo los archivos del repositorio y contando evidencias con comandos de
solo lectura (`git status/log`, conteo de `@Test`, búsquedas de patrones).

**Fuentes consultadas:**

- Código de `app/app/src/main/java/**` (todo el árbol de producción).
- Configuración Gradle: `app/build.gradle.kts`, `app/app/build.gradle.kts`, `app/settings.gradle.kts`,
  `app/gradle.properties`, `app/gradle/wrapper/gradle-wrapper.properties`.
- Recursos: `strings.xml`, `themes.xml`, `colors.xml`, `AndroidManifest.xml`, fuentes Inter.
- Pruebas: `app/app/src/test/**` (21 archivos) y `app/app/src/androidTest/**` (12 archivos).
- Documentación: `AGENTS.md`, `PROJECT_STATUS.md`, `TASKS.md`, `README.md`, `docs/ADR-001-control-diario.md`,
  `docs/AUDITORIA_CONTROL_DIARIO.md`, `SESSION_HANDOFF.md`, `memory/2026-09-13.md`.
- Requisitos: `docs/SRS_MiControlDiDi_v1.docx` (texto extraído localmente, 23.843 caracteres).
- Evidencia de ejecución: `connected_test.log` (corrida de 130 pruebas en ALT-LX3).

**Limitaciones:** el SRS es un borrador «pendiente de validación»; no hay CI ni reportes XML de la
última corrida unitaria en el repositorio, por lo que el número de unitarias se verifica por conteo de
`@Test`, no por reporte. Las capturas de `dist/` (mayoría del tema oscuro ya retirado) no se auditan.

---

## 3. Estructura del repositorio

```
C:\Proyectos\MiControlDiDi\        ← workspace OpenClaw + raíz Git
├── AGENTS.md · PROJECT_STATUS.md · TASKS.md · README.md
├── docs\            SRS (.docx) + ADR-001 + AUDITORIA_CONTROL_DIARIO
├── app\             ← raíz del proyecto Gradle
│   ├── build.gradle.kts · settings.gradle.kts · gradle.properties · gradlew[.bat]
│   └── app\         ← módulo Android :app
│       ├── build.gradle.kts
│       ├── schemas\ (solo 1.json, vestigial)
│       └── src\{main,test,androidTest}\
├── dist\            APK demo + capturas (ignorado por Git)
├── recursos\ disenos\  material de apoyo
└── memory\ DREAMS.md SESSION_HANDOFF.md  (locales, ignorados por Git)
```

**Observación estructural:** la raíz Git (`C:\Proyectos\MiControlDiDi`) y la raíz Gradle (`app/`) no
coinciden, y el módulo Android vive en `app/app`. Es correcto para este workspace, pero conviene que
quien abra el repo por primera vez lo sepa (el `README.md` lo menciona).

**`.gitignore`:** cubre `.gradle/`, `build/`, `local.properties`, `.idea/`, `dist/`, `*.log`, `*.jks`,
`*.keystore`, `.env` y los archivos locales de OpenClaw (`memory/`, `MEMORY.md`, `DREAMS.md`,
`SESSION_HANDOFF.md`, `openclaw-workspace-state.json*`). Consistente.

---

## 4. Stack y configuración

| Elemento | Valor | Comentario |
|---|---|---|
| AGP | `8.13.0` | |
| Kotlin | `2.1.20` + plugin Compose `2.1.20` | |
| KSP | `2.1.20-1.0.32` | |
| Gradle | `8.13-bin` | |
| compileSdk / targetSdk / minSdk | `36 / 36 / 26` | |
| Java/Kotlin target | `17` | |
| Compose BOM | `2025.01.01` | |
| Room | `2.8.4` (`runtime`, `ktx`, `compiler`) | |
| Navigation Compose | `2.8.5` | |
| Lifecycle | `2.8.7` | |
| Pruebas | JUnit 4.13.2, coroutines-test 1.9.0, AndroidX test 1.2.1/1.6.2, compose ui-test-junit4 | |
| Release | `isMinifyEnabled = true` + `proguard-rules.pro` | R8 validado en `assembleRelease` |

**Verificaciones:**

- ✅ Sin Firebase, Retrofit, OkHttp, Volley, WebView, DataStore remoto ni WorkManager.
- ✅ `AndroidManifest.xml` **sin permisos declarados** (ni `INTERNET`). Solo la Activity `MainActivity`
  `exported=true` con LAUNCHER.
- ✅ `local.properties` presente y fuera de Git.
- ✅ Estilo Kotlin oficial, `nonTransitiveRClasses`, `useAndroidX`.
- ⚠️ `exportSchema` desactivado en `MiControlDatabase` por incompatibilidad Room 2.8.4 ↔ Kotlin 2.1.20
  (comentario en `app/app/build.gradle.kts`). Consecuencia: `schemas/` solo tiene `1.json` y **no se
  puede usar `MigrationTestHelper`**; las migraciones se validan con `MigracionTest` escrito a mano.
- ⚠️ `proguard-rules.pro` mantiene `androidx.compose.**` completo, lo que reduce el efecto de R8 sobre
  Compose (rendimiento/APK), pero es una decisión conservadora aceptable para el MVP.

---

## 5. Arquitectura por capas (verificación de `AGENTS.md`)

Flujo exigido: `Compose UI → ViewModel → Repository → DAO → Room`.

| Regla del proyecto | Cumplimiento | Evidencia |
|---|---|---|
| Composables no acceden a Room | ✅ | Ninguna screen importa `dao`/`RoomDatabase`. |
| ViewModels no acceden a DAOs | ✅ | Todos los ViewModel reciben `*Repository`; ninguno importa `data.local.dao`. |
| Repository pattern | ✅ | 6 repositorios, uno por agregado. |
| MVVM + StateFlow + Flow | ✅ | `uiState: StateFlow<...>` + `MutableStateFlow` privado en todos los ViewModel. |
| Coroutines | ✅ | `viewModelScope.launch`, `flatMapLatest`, `combine`, `catch`. |
| Capas separadas | ✅ | Paquetes `domain`, `data.local.{entity,dao,database}`, `data.repository`, `ui.*`, `util`. |
| `Clock` inyectable | 🟡 Parcial | Sí en Dashboard, Stats, Jornada y Perfil; **no** en `ViajeViewModel` ni `GastoViewModel`, que usan `System.currentTimeMillis()` directo. |
| Dinero en `Long` | ✅ | Ver §6 y §7. |
| No persistir valores calculables | ✅ | `ViajeEntity.ingresoTotal` y `PerfilTrabajoEntity.reservaPorKm/reservaTotalPorKm` son propiedades derivadas. |

**Conclusión:** la separación de capas es correcta y consistente; la única asimetría relevante es la
inyección de `Clock` (ver H-07 en §15).

---

## 6. Modelo de datos y Room

**Base:** `micontrol_didi.db`, `version = 6`, `exportSchema = false`, singleton `INSTANCIA` con
`@Volatile` + doble verificación. **Sin** `fallbackToDestructiveMigration()`.

| Tabla | Clave | Campos principales | Índices / FK |
|---|---|---|---|
| `viajes` | `id` auto | `fechaHora`, `valor`, `propina`, `observacion` | `Index(fechaHora)` |
| `categorias_gasto` | `id` auto | `nombre` (`COLLATE NOCASE`), `activa` | único en `nombre` |
| `gastos` | `id` auto | `fechaHora`, `categoriaId`, `valor`, `descripcion` | `Index(categoriaId)`, `Index(fechaHora)`, FK → `categorias_gasto` `ON DELETE RESTRICT` |
| `metas` | `id` auto | `tipoPeriodo` (TEXT `DIA`/`MES`), `valorObjetivo`, `activa`, `createdAt` | — |
| `perfil_trabajo` | `id = 1` (fila única) | plataforma, vehículo, combustible, ciudad, días CSV, horas en minutos, % km vacíos, 6× (costo, intervalo km), `actualizadoEn` | PK `id` |
| `jornadas` | `id` auto | `fechaHoraInicio`, `kilometrajeInicialMetros`, nivel combustible, precio galón, zona, plataforma, meta bruta, energía, clima, observaciones, **12 booleanos** de revisión | — |

**Migraciones explícitas:** `MIGRATION_1_2` (crea categorías+gastos y siembra 6 categorías),
`MIGRATION_2_3` (crea `metas`), `MIGRATION_3_4` (crea índices `fechaHora` con los nombres exactos que
Room genera), `MIGRATION_4_5` (crea `perfil_trabajo` y **siembra el perfil por defecto desde
`PerfilTrabajoEntity()`**, de modo que migración y entidad no se desincronicen), `MIGRATION_5_6` (crea
`jornadas`, sin sembrar filas).

**Callbacks:** `PREPOBLAR_CATEGORIAS.onCreate` inserta las 6 categorías `INSERT OR IGNORE` y llama a
`sembrarPerfilPorDefecto`. Coherente con las migraciones.

**Puntos fuertes:**
- Los nombres de índices de `MIGRATION_3_4` coinciden con los autogenerados por Room (evita desajuste de
  identity hash). Buen detalle.
- `MIGRATION_4_5` siembra desde la entidad, no con literales duplicados.

**Riesgos:**
- Sin `exportSchema`, **cada** salto de versión depende de una prueba manual; un error de SQL en una
  migración solo se detecta al ejecutar `MigracionTest` en dispositivo.
- `MetaEntity.createdAt` tiene valor por defecto `System.currentTimeMillis()` dentro de la entidad
  (no inyectable), lo que hace no determinista cualquier prueba que dependa de ese campo si no se
  proporciona explícitamente.
- El singleton `INSTANCIA` no expone reset; no afecta a la app, pero complica pruebas que quisieran
  reutilizar la instancia de producción.

---

## 7. Capa de dominio

| Archivo | Responsabilidad | Calidad |
|---|---|---|
| `PeriodoDashboard` | enum DIA/SEMANA/MES | OK |
| `RangoPeriodo` | intervalo semiabierto `[inicio, fin)` con `require(fin > inicio)` | OK |
| `CalculadorRangoPeriodo` | día/semana(lunes)/mes con `ZoneId` explícito, sin `now()` interno | Excelente: puro y testeable |
| `PeriodoMeta` | enum DIA/MES + `fromNombre` | OK |
| `PuntoRevision` | 12 puntos con flag `critico` (llantas, frenos, luces) | OK |
| `NivelCombustible` | 5 niveles + `POR_DEFECTO` | OK |
| `BloqueJornada` | 8 bloques anclados al inicio del perfil; total 540 min (480 trabajo + 60 pausa) | OK, con agregados calculados |
| `EstadoHorarioLaboral` | DTO inmutable del estado del horario | OK |
| `CalculadorHorarioLaboral` | función pura `LocalTime → EstadoHorarioLaboral`; rangos semiabiertos; recorta bloques si la jornada es corta; **no cierra la jornada** | Excelente |

**Verificación de las decisiones del ADR-001:** D-12 (odómetro), D-14 (distancias en metros `Long`) y
D-15 (dinero en `Long`) se cumplen en entidades y dominio; D-16 (derivados no persistidos) se cumple.

---

## 8. DAOs y repositorios

| DAO | Operaciones | Notas |
|---|---|---|
| `ViajeDao` | insertar(ABORT), obtenerTodos, obtenerPorRango, obtenerIngresosPorRango (SUM+COALESCE), obtenerPorId, actualizar, eliminar | `Flow<Long>` reactivo |
| `GastoDao` | insertar, actualizar, eliminar, obtenerTodos (JOIN categoría), obtenerPorId, obtenerPorRango(+categoría opcional), obtenerTotalGastosPorRango | JOIN devuelve `GastoConCategoria` |
| `CategoriaGastoDao` | insertar/insertarLista (IGNORE), obtenerActivas, obtenerPorId, existePorNombre (LOWER) | unicidad en 2 capas + índice |
| `MetaDao` | insertar, obtenerActiva, obtenerUltima, desactivarTodas, actualizar, eliminar | |
| `PerfilTrabajoDao` | observar(id), obtener(id), guardar(REPLACE) | fila única explícita |
| `JornadaDao` | insertar, observarTodas, observarUltima | |

**Repositorios** validan reglas y devuelven `Result<...>`: viaje (`valor > 0`, `propina >= 0`),
gasto (`valor > 0`, `categoriaId > 0`), meta (`valorObjetivo > 0`), perfil (textos obligatorios, días,
horas coherentes, % 0–100, costos ≥ 0 e intervalo > 0 si hay costo), jornada (odómetro ≥ 0, precio > 0,
meta > 0, energía 0–10, plataforma obligatoria).

**Observaciones menores:**
- `GastoConCategoria.ingresoTotal` devuelve `valor`; el nombre es engañoso (los gastos no son ingresos).
- Los mensajes de `require(...)` de los repositorios están en español y **no** son visibles al usuario
  (los ViewModel los sustituyen por recursos de `strings.xml`). Correcto según la regla 7.

---

## 9. Capa de presentación (ViewModels y estados)

| ViewModel | Patrón | Puntos notables |
|---|---|---|
| `ViajeViewModel` | filtro + `flatMapLatest` + `catch` | Estado de formulario, edición, eliminación, filtro por fecha. Usa `System.currentTimeMillis()`. |
| `GastoViewModel` | `combine` gastos+categorías | `ModoFormulario` (CREACION/CARGANDO_EDICION/EDICION/ERROR_EDICION); protección ID inexistente. |
| `DashboardViewModel` | `flatMapLatest` + `combine` de 3 flows (ingresos, gastos, meta) | `Clock` inyectable; `progresoMeta: Float?`; error con `catch`. |
| `MetaViewModel` | observa meta activa | guarda/actualiza/elimina. |
| `StatsViewModel` | periodo actual vs anterior | ⚠️ `cantidadGastosActual` fijo en `0`; `porcIngresos/porcGastos/porcGanancia` calculados pero **no renderizados**. |
| `PerfilTrabajoViewModel` | formulario por `Map<CampoPerfil,String>` | `cargadoDesdeFuente` evita pisar la edición en curso; validación campo a campo. |
| `JornadaViewModel` | prellena plataforma desde el perfil; detecta jornada de hoy | ⚠️ `perfilTrabajoRepository.observar().catch { }` **traga silenciosamente** el error de carga del perfil. |

**Fortalezas:** estados inmutables con `copy`, protección contra doble pulsación (`guardando`),
mensajes de error siempre desde `ResourceProvider` (desacople de Android), `CancellationException`
relanzada en los `catch` de dashboard/gasto/viaje.

**Debilidades:** la asimetría de `Clock` (§5) y el `catch {}` vacío de JornadaViewModel son las dos
cosas más señalables.

---

## 10. UI Compose y navegación

**Rutas (`NavGraph.kt`):** `dashboard` (start), `lista_viajes`, `registrar_viaje`,
`registrar_viaje/{viajeId}`, `lista_gastos`, `registrar_gasto`, `registrar_gasto/{gastoId}`,
`configurar_meta`, `configurar_perfil`, `registrar_jornada`, `estadisticas`, `configuracion`.

**Barra inferior:** Dashboard, Viajes, Gastos, Estadísticas, Configuración, con
`popUpTo(DASHBOARD) { saveState }` + `launchSingleTop` + `restoreState`. Se oculta en formularios.

**Pantallas:** `DashboardScreen` (hero de ganancia + resumen + meta + 2 botones de acceso rápido),
`ListaViajesScreen`/`ListaGastosScreen` (LazyColumn + FAB + estados vacío/error/carga + filtros con
scroll), `RegistrarViajeScreen`, `RegistrarGastoScreen` (dropdown de categorías), `ConfigurarMetaScreen`,
`ConfigurarPerfilScreen`, `RegistrarJornadaScreen`, `StatsScreen`, `SettingsScreen`.

**Bien:**
- Filtros con `verticalScroll` (corrige H-8.7-02) y validación `inicio ≤ fin` vía `CalculadorRangoFiltro`
  (corrige H-8.7-01 y H-8.7-03).
- Colores 100 % desde `MaterialTheme.colorScheme`; ninguna cadena visible hardcodeada (verificado).
- testTags consistentes para Compose UI testing.
- Botones táctiles ≥ 48 dp y `IconButton` para el acceso a meta (corrección 8.5).

**Hallazgos de UI:**
- H-02: la paleta XML (`colors.xml`/`themes.xml`) está desalineada del tema Compose.
- Los tres primeros ítems de la barra inferior comparten el mismo icono (`Icons.AutoMirrored.Filled.List`),
  lo que dificulta distinguirlos a simple vista.
- `RegistrarViajeScreen` muestra **siempre** `DateFormatter.format(System.currentTimeMillis())` como
  fecha informativa, incluso al editar un viaje, donde lo correcto sería mostrar `fechaHoraOriginal`.
- Los overloads «solo estado» de las screens (para tests) construyen `ViajeCard`/`GastoCard` con
  `onEditar = {}` y `onEliminar = {}`; es intencional, pero conviene documentarlo para no confundirlo con
  un bug.
- `DashboardScreen` declara `floatingActionButton = { }` vacío: código muerto inocuo.

---

## 11. Recursos, tema y accesibilidad

- **40+ cadenas** de texto en `strings.xml` (incluye 44 del perfil y 37 de jornada); sin literales
  visibles en Kotlin.
- **Tipografía Inter** (4 pesos, SIL OFL 1.1) empaquetada en `res/font/`; `Type.kt` define la escala M3.
- **Tema:** solo `lightColorScheme` (decisión de producto, «Mejora 2»), con paleta fintech semántica
  (azul = balance, verde = ingresos, rojo = gastos/errores, neutros).
- **Accesibilidad:** revisión 8.5 documentada con contrastes WCAG AA ≥ 4.5:1 en claro y oscuro;
  `contentDescription = null` solo en iconos de `NavigationBarItem` (que ya tienen `label`), correcto
  según M3.
- H-02 (XML desalineado) es el único defecto de tema detectado.

---

## 12. Pruebas

**Conteo verificado por `@Test` (no por reporte):**

| Suite | Archivos | Pruebas |
|---|---|---|
| Unitarias (`src/test`) | 21 | **309** |
| Instrumentadas (`src/androidTest`) | 12 | **130** |
| **Total** | **33** | **439** |

Distribución unitaria destacada: `GastoViewModelTest` 37, `CalculadorHorarioLaboralTest` 29,
`DashboardViewModelTest` 27, `CalculadorRangoPeriodoTest` 22, `PerfilTrabajoRepositoryTest` 18,
`PerfilTrabajoViewModelTest` 18, `ViajeViewModelTest` 17, `CalculadorRangoFiltroTest` 15,
`MetaViewModelTest` 15, `JornadaViewModelTest` 16…

Distribución instrumentada: `GastoDaoTest` 31, `ViajeDaoTest` 19, `GastoComposeTest` 17,
`DashboardScreenTest` 15, `StatsScreenTest` 10, `MigracionTest` **5** (v1→v2, v2→v3, v3→v4, v4→v5,
v5→v6), `PersistenciaTest` **4**, `GastoListaScreenTest` 8, `ViajeComposeTest` 8,
`ViajeListaScreenTest` 7, `CategoriaGastoDaoTest` 3, `CategoriaUnicidadTest` 3.

**Evidencia de la última corrida** (`connected_test.log`): `Starting 130 tests on ALT-LX3`,
`Finished 130 tests`, `BUILD SUCCESSFUL in 2m 47s`, 0 fallos y 0 omitidas. Coincide con lo documentado.

**Calidad de las pruebas:**
- ✅ `MigracionTest` crea esquemas SQLite **a mano** por versión y abre con Room + todas las
  migraciones: es la técnica correcta cuando `exportSchema = false`.
- ✅ `PersistenciaTest` usa BD de archivo temporal y **nueva instancia** de Room (valida persistencia
  real, no solo `inMemoryDatabaseBuilder`).
- ✅ Fakes a medida (`FakeResourceProvider`, DAOs fake) sin frameworks de mocking externos.
- ✅ Las incidencias de incrementos B, C y D se corrigieron **en las pruebas**, no en producción, y sin
  desactivar pruebas (documentado y coherente con la memoria).
- ⚠️ No hay pruebas Compose del `DatePicker` (deuda reconocida) ni pruebas unitarias del
  `NavGraph`/navegación real.
- ⚠️ Las instrumentadas **solo** se ejecutan en el dispositivo físico ALT-LX3 (restricción explícita:
  nunca emulador), lo que ata la validación a disponibilidad de hardware.

---

## 13. Requisitos (SRS / AGENTS) frente a implementación

| Requisito | Estado | Evidencia |
|---|---|---|
| RF-01 Registrar viaje | ✅ | `RegistrarViajeScreen` + `ViajeRepository` |
| RF-02 Consultar viajes | ✅ | `ListaViajesScreen` orden descendente |
| RF-03 Editar viaje | ✅ | ruta `registrar_viaje/{viajeId}` |
| RF-04 Eliminar viaje | ✅ | `AlertDialog` + `ViajeDao.eliminar` |
| RF-05 Registrar gasto | ✅ | `RegistrarGastoScreen` |
| RF-06 Categorías iniciales | ✅ | 6 sembradas en migración y `onCreate` |
| RF-07 Consultar/editar/eliminar gastos | ✅ | `ListaGastosScreen` + `GastoViewModel` |
| RF-08 Balance día/semana/mes | ✅ | `DashboardViewModel` + consultas agregadas |
| RF-09 Dashboard con meta | ✅ | `DashboardScreen` + `MetaProgressCard` |
| RF-10 Filtrar movimientos | ✅ | `CalculadorRangoFiltro` + filtros con scroll |
| RF-11 Configurar meta | ✅ | `ConfigurarMetaScreen` |
| RF-12 Estadísticas básicas | ✅ | `StatsViewModel`/`StatsScreen` |
| RF-13 Preferencias (tema) | ❌ Retirado | Decisión de producto (Mejora 2, 02-ago-2026); tema claro único |
| RF-14 Exportación futura | ⏸️ No implementado | Correcto: opcional en el MVP |

| Regla de negocio | Estado |
|---|---|
| RN-01 ganancia neta = ingresos − gastos | ✅ |
| RN-02 valores monetarios ≥ 0 | ✅ (más estricto: `> 0` donde corresponde) |
| RN-03 fecha obligatoria | ✅ (se asigna del reloj al guardar) |
| RN-04 ingreso total = valor + propina | ✅ (`ingresoTotal` derivado) |
| RN-05 eliminados fuera de balances | ✅ (borrado real de filas) |
| RN-06 zona horaria del dispositivo | ✅ (`Clock`/`ZoneId.systemDefault`) |
| RN-07 COP con formato legible | ✅ (`CurrencyFormatter`, `$ #,##0`) |
| RN-08 edición recalcula totales | ✅ (Flows reactivos) |

**RNF:** RNF-03 (offline) ✅ verificado; RNF-05 (privacidad local) ✅; RNF-06 (recuperación) ✅ probado
con `PersistenciaTest` y la verificación manual 8.7; RNF-01/02/04 parcialmente cubiertos por pruebas y
por la revisión de accesibilidad.

**Nota:** `AGENTS.md` prohíbe «desarrollar funcionalidades futuras sin autorización». Los incrementos
E–K están implementados **hasta D**, y D–K figuran como ⛔ no autorizados, consistente con `TASKS.md`.
No se detectó código de E–K en el repositorio.

---

## 14. Git, documentación y trazabilidad

- Rama `main`, sincronizada con `origin/main`; **working tree limpio**; último commit
  `22e09be feat(jornada): agrega registro de jornada con revision previa (BD v6)`.
- Historial reciente coherente con lo documentado: `8e9ac77` (docs 129/129), `93e94f0` (horario),
  `0e1d4a2` (perfil v5), `fd543f0` (A + ADR-001), `5526639` (workspace), `d8c99d0` (gitignore).
- `PROJECT_STATUS.md` y `TASKS.md` están **muy completos** y con trazabilidad fina por incremento,
  incluidos los criterios de aceptación y las incidencias corregidas.
- `docs/ADR-001-control-diario.md`: 19 decisiones (D-01…D-19) bien razonadas y con alternativas
  descartadas. Excelente nivel de documentación arquitectónica.
- `SESSION_HANDOFF.md` y `memory/2026-09-13.md` son locales (ignorados) y consistentes con el estado.
- ❌ `README.md` desactualizado (H-01): describe el proyecto a nivel de Fase 5, con DB v2, 113/65
  pruebas y `startDestination = lista_viajes`; además lista como «no implementado» cosas que ya existen
  (dashboard, filtros, metas, estadísticas, editar/eliminar viajes).
- ❌ No hay CI ni workflows.

---

## 15. Hallazgos consolidados (con gravedad)

| ID | Hallazgo | Gravedad | Ubicación | Recomendación |
|---|---|---|---|---|
| **H-01** | `README.md` obsoleto y contradictorio con `PROJECT_STATUS.md`. | Media | `README.md` | Reescribirlo al estado real (v6, 439 pruebas, control diario). |
| **H-02** | `colors.xml`/`themes.xml` con paleta verde antigua; `statusBarColor` verde ≠ `primary` azul. | Media | `res/values/*.xml` | Alinear o eliminar la paleta XML vestigial. |
| **H-03** | Sin CI; validación 100 % manual. | Media | raíz del repo | Añadir workflow de unitarias (las instrumentadas seguirán siendo manuales). |
| **H-04** | `StatsViewModel.cantidadGastosActual` siempre `0` y porcentajes `porc*` no renderizados. | Baja | `ui/stats/*` | Eliminar campo muerto o mostrarlo; si no, es deuda silenciosa. |
| **H-05** | Al editar un viaje, la fecha mostrada es «ahora», no `fechaHoraOriginal`. | Baja | `RegistrarViajeScreen` | Mostrar la fecha original en modo edición. |
| **H-06** | Los tres primeros ítems de la barra inferior comparten icono. | Baja | `NavGraph.kt` | Usar iconos diferenciados. |
| **H-07** | `Clock` no inyectado en `ViajeViewModel`/`GastoViewModel`. | Baja | `ui/viaje`, `ui/gasto` | Homogeneizar con el resto (mejora testabilidad). |
| **H-08** | `JornadaViewModel` captura el error del perfil con `catch { }` vacío (fallo silencioso). | Baja | `JornadaViewModel` | Registrar/notificar el fallo con recurso de error. |
| **H-09** | `GastoConCategoria.ingresoTotal` devuelve `valor` (nombre engañoso). | Baja | `entity/GastoConCategoria.kt` | Renombrar a `monto` o eliminar la propiedad. |
| **H-10** | `exportSchema = false`: migraciones sin validación con `MigrationTestHelper`; `schemas/1.json` vestigial. | Media (asumida) | `MiControlDatabase` | Deuda aceptada y documentada; valorar actualizar Room/Kotlin para habilitar esquemas. |
| **H-11** | Sin pruebas del `DatePicker` ni de navegación real. | Baja | `androidTest` | Añadir cuando el panel de filtros se estabilice definitivamente. |
| **H-12** | `MetaEntity.createdAt` usa `System.currentTimeMillis()` por defecto dentro de la entidad. | Baja | `MetaEntity` | Inyectar el valor al crear la meta. |

---

## 16. Deuda técnica y pendientes reconocidos

| Elemento | Estado | Comentario |
|---|---|---|
| `exportSchema = false` | Vigente | Room 2.8.4 ↔ Kotlin 2.1.20; alternativa: subir versiones (fuera de alcance sin autorización). |
| Pruebas Compose del DatePicker | Pendiente | Reconocido en `SESSION_HANDOFF.md`. |
| Firma de release definitiva | Pendiente | El APK demo usa key de depuración. |
| Cierre de jornada (odómetro final) | Pendiente | Llega con el incremento F (no autorizado). |
| Jornada en el dashboard | Pendiente | Llega con el incremento I (no autorizado). |
| `README.md` / `colors.xml` desalineados | **No documentado** | Nuevos (H-01, H-02). |
| CI | Ausente | Nuevo (H-03). |

---

## 17. Recomendaciones y próximos pasos

**Inmediatas (bajo coste, sin tocar lógica):**

1. Actualizar `README.md` al estado real (H-01) — es la puerta de entrada al proyecto.
2. Alinear o retirar `colors.xml`/`themes.xml` (H-02).
3. Limpiar el código muerto de `StatsViewModel`/`StatsUiState` (H-04) y el nombre engañoso
   `GastoConCategoria.ingresoTotal` (H-09).
4. Corregir la fecha mostrada al editar viajes (H-05) y homogeneizar iconos (H-06).

**De proceso:**

5. Añadir un workflow de CI para `assembleDebug` + `testDebugUnitTest` (H-03); las instrumentadas
   seguirán en ALT-LX3.
6. Registrar los hallazgos H-01…H-12 en `TASKS.md` como deuda técnica, sin implementarlos sin
   autorización.

**Sobre los incrementos:**

7. El **incremento E (viaje adaptado a inDrive)** está listo para arrancar cuando se autorice: la base
   documental (ADR-001 + auditoría) ya define el modelo de distancias, dinero y migración v6→v7.
8. No iniciar E–K ni tocar la base de datos sin autorización explícita (regla vigente del proyecto).

---

## Anexo A — Inventario de código de producción

`MainActivity.kt`; `data/local/database/MiControlDatabase.kt`; entidades `ViajeEntity`,
`CategoriaGastoEntity`, `GastoEntity`, `GastoConCategoria`, `MetaEntity`, `PerfilTrabajoEntity`,
`JornadaEntity`; DAOs `ViajeDao`, `GastoDao`, `CategoriaGastoDao`, `MetaDao`, `PerfilTrabajoDao`,
`JornadaDao`; repositorios `ViajeRepository`, `GastoRepository`, `CategoriaGastoRepository`,
`MetaRepository`, `PerfilTrabajoRepository`, `JornadaRepository`; dominio `PeriodoDashboard`,
`RangoPeriodo`, `CalculadorRangoPeriodo`, `PeriodoMeta`, `PuntoRevision`, `NivelCombustible`,
`BloqueJornada`, `EstadoHorarioLaboral`, `CalculadorHorarioLaboral`; UI `dashboard`, `viaje`, `gasto`,
`meta`, `settings`, `jornada`, `stats`, `navigation`, `theme`; util `CalculadorRangoFiltro`,
`CurrencyFormatter`, `DateFormatter`, `ResourceProvider`.

## Anexo B — Método de verificación

Comandos de solo lectura usados: `git status/log/remote`, listados recursivos de archivos, conteo de
`@Test` por archivo, búsquedas de patrones (`TODO/FIXME`, `Float|Double`, `destructiveMigration`,
`Text("`, `contentDescription = "`), y extracción local del texto del SRS desde el `.docx`.
No se ejecutó `gradlew`, ni pruebas, ni se escribió en ningún archivo salvo este informe.
