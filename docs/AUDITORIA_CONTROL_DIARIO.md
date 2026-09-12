# Auditoría y plan de adaptación — MiControlDiDi: control diario del trabajo

> **Documento del proyecto MiControlDiDi** (`C:\Proyectos\MiControlDiDi`). NO autoriza implementación.
> Actualizado: 12-sep-2026 — Incremento A (documentación). Solo se tocaron documentos: no hubo cambios de código.

---

## 1. Qué es este documento

MiControlDiDi es la app para controlar ingresos, gastos y ganancias del trabajo diario del conductor. Hoy registra viajes y gastos y calcula el balance por día, semana y mes (MVP cerrado, 322/322 pruebas).

La propuesta que compartiste —jornada con checklist, kilómetros vacíos, tanqueos, neto operativo y neto económico, bienestar— fija el contexto de trabajo en la plataforma **inDrive**. **Este documento la aterriza en MiControlDiDi**, el mismo proyecto en el que venimos trabajando, y la desglosa en incrementos ejecutables.

Aquí queda escrito, sin inventar nada:

1. Lo que la app **ya tiene** (verificado en el código de este proyecto).
2. La **brecha** frente a la propuesta.
3. Qué de la propuesta **choca** con las reglas del proyecto.
4. El **plan por incrementos**, cada uno ejecutable como una sola tarea.
5. Los **riesgos**, sobre todo al migrar la base de datos.

Las decisiones aprobadas quedan en `docs/ADR-001-control-diario.md`.

---

## 2. Cómo se verificó

Leí directamente el proyecto (`AGENTS.md`, `PROJECT_STATUS.md`, `TASKS.md`, `README.md`, el SRS y el código de `app/app/src/main/`). **Solo lectura:** no compilé, no ejecuté pruebas instrumentadas y no alteré ningún archivo de la app.

---

## 3. Lo que la app ya tiene

### 3.1 Base de datos

Room **v4**, archivo `micontrol_didi.db`, `exportSchema = false`, **sin** `fallbackToDestructiveMigration()`.

| Tabla | Campos |
|---|---|
| `viajes` | `id`, `fechaHora`, `valor`, `propina`, `observacion` (+ `ingresoTotal` calculado, no persistido) |
| `gastos` | `id`, `fechaHora`, `categoriaId`, `valor`, `descripcion` |
| `categorias_gasto` | `id`, `nombre`, `activa` |
| `metas` | `id`, `tipoPeriodo` (DIA/MES), `valorObjetivo`, `activa`, `createdAt` |

Migraciones explícitas: `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`. Índices por `fechaHora` en viajes y gastos; categoría única `COLLATE NOCASE`; `ON DELETE RESTRICT` entre gasto y categoría. Seis categorías sembradas: Gasolina, Mantenimiento, Parqueadero, Lavado, Cuota de la moto, Otros.

### 3.2 Pantallas y navegación

Rutas: `dashboard`, `lista_viajes`, `registrar_viaje`, `registrar_viaje/{viajeId}`, `lista_gastos`, `registrar_gasto`, `registrar_gasto/{gastoId}`, `configurar_meta`, `estadisticas`, `configuracion`. Barra inferior con Inicio, Viajes, Gastos, Datos y Ajustes.

### 3.3 Cálculos ya resueltos

`CalculadorRangoPeriodo` (día/semana/mes, semana desde el lunes, intervalos semiabiertos), `CalculadorRangoFiltro` (corrección UTC → hora local + validación inicio ≤ fin), `ViajeDao.obtenerIngresosPorRango()`, `GastoDao.obtenerTotalGastosPorRango()` (ambos `Flow<Long>` con `COALESCE`), `DashboardViewModel` con `Clock` inyectable y `flatMapLatest` + `combine`, `CurrencyFormatter` (COP), `DateFormatter`, `ResourceProvider`, `StatsViewModel`/`StatsScreen`.

### 3.4 Calidad

| Métrica | Valor |
|---|---|
| Unitarias | **194/194** |
| Instrumentadas (ALT-LX3) | **128/128** |
| **Total** | **322/322, 0 fallos** |
| Sin conexión | Verificado: sin permiso `INTERNET` ni librerías de red |
| Accesibilidad | Revisada (contraste WCAG AA, etiquetas, áreas táctiles) |

### 3.5 Deuda técnica (no tocar sin autorización)

`exportSchema = false` (Room 2.8.4 ↔ Kotlin 2.1.20: sin validación con `MigrationTestHelper`), pruebas Compose del DatePicker pendientes, APK de producción sin firma definitiva.

---

## 4. Brecha: propuesta vs. app

| # | Punto de la propuesta | En MiControlDiDi hoy | Falta |
|---|---|---|---|
| 1 | Perfil de trabajo editable (plataforma, vehículo, combustible, ciudad, horario, umbrales, reservas por km) | ❌ No existe | Entidad de configuración + sección en Ajustes |
| 2 | Horario con bloques y pausas | ❌ No existe (solo rangos día/semana/mes) | Dominio nuevo con `Clock` inyectable |
| 3 | Jornada + checklist de 12 puntos | ❌ No existe | Entidad + pantalla + validación |
| 4 | Viaje con datos de plataforma (precio ofrecido/final, distancias, zonas, pago, peajes) | 🟡 Solo `valor`, `propina`, `fechaHora`, `observacion` | Ampliar entidad y formulario |
| 5 | Kilómetros totales / con pasajero / vacíos y semáforo | ❌ Ningún campo de distancia | Odómetro + cálculo |
| 6 | Tanqueos: rendimiento y costo por km | 🟡 Solo la categoría de gasto "Gasolina" | Entidad de tanqueos + enlace con el gasto |
| 7 | Neto operativo y neto económico con reservas | 🟡 Solo bruto (ingresos − gastos) | Cálculo nuevo |
| 8 | Bienestar del conductor | ❌ No existe | Campos en jornada + recordatorios |
| 9 | Dashboard del conductor | 🟡 Ingresos, gastos, neto, meta | Extender, no rehacer |
| 10 | Reportes por bloque, plataforma, zona y día | 🟡 Solo por periodo | Extender Estadísticas |
| 11 | Migraciones seguras | ✅ Base sana (v4, migraciones explícitas, `MigracionTest`) | Encadenar v5…v9 |
| 12 | Calidad y cierre | ✅ Base sana (322 pruebas, accesibilidad) | Pruebas de lo nuevo |

**Se reutiliza sin cambios:** `RangoPeriodo`, `CalculadorRangoPeriodo`, `PeriodoDashboard`, `PeriodoMeta`, `CalculadorRangoFiltro`, `CurrencyFormatter`, `DateFormatter`, `ResourceProvider`, patrón `Clock` + Factory + Repository + DAO, migraciones explícitas, `NavGraph` y barra inferior, `MigracionTest` y `PersistenciaTest`.

---

## 5. Choques con las reglas del proyecto

| Regla | Choque | Cómo se resuelve |
|---|---|---|
| N.º 12 «una sola tarea por ejecución» y «detente al cerrar» | La propuesta viene como 13 fases de un tirón | Se parte en 9 incrementos, uno por ejecución |
| N.º 4 sin Firebase/backend/nube | Los recordatorios podrían pedir servicios | Recordatorios **internos** en la app (banners y tarjetas) |
| Dinero sin `Float`/`Double` | Precio por litro y rendimiento son decimales | Dinero en `Long`; lo decimal solo al mostrar |
| Fuera del MVP: sin GPS | Se necesitan distancias | Odómetro y distancias a mano |
| Mejora 2 (02-ago): solo tema claro | La propuesta pide respetar modo oscuro «si ya se usa» | Se mantiene claro: la app no usa modo oscuro por decisión de producto |
| Cierre: actualizar `PROJECT_STATUS.md` y `TASKS.md` | — | Se actualizan: A cerrado, B–K **no autorizados** |

---

## 6. Plan por incrementos (NO autorizado salvo A)

> Solo **A** está autorizado y cerrado. Los demás requieren autorización explícita, uno por uno.

| # | Incremento | Entrega | Migración | Estado |
|---|---|---|---|---|
| **A** | Auditoría + ADR | Este documento + `ADR-001` | — | ✅ Cerrado (12-sep-2026) |
| B | Perfil de trabajo | Entidad `perfil_trabajo` (fila única) + DAO + repositorio + ViewModel + sección en Ajustes | v4→v5 | ⛔ No autorizado |
| C | Horario laboral (dominio) | Bloques, pausas, progreso, modo regreso | — | ⛔ No autorizado |
| D | Jornada + checklist | Entidad `jornadas` + checklist de 12 puntos + pantallas | v5→v6 | ⛔ No autorizado |
| E | Viaje con datos de plataforma | Columnas nuevas en `viajes` sin romper `valor` | v6→v7 | ⛔ No autorizado |
| F | Tanqueos / gasolina | Entidad `tanqueos` enlazada al gasto de Gasolina | v7→v8 | ⛔ No autorizado |
| G | Kilómetros y netos (dominio) | Km vacíos, semáforo, neto operativo y económico | — | ⛔ No autorizado |
| H | Bienestar | Cansancio y molestias + recordatorios internos | v8→v9 | ⛔ No autorizado |
| I | Dashboard del conductor | Extensión de `DashboardUiState`/`ViewModel`/`Screen` | — | ⛔ No autorizado |
| J | Reportes ampliados | Extensión de `StatsViewModel`/`StatsScreen` | — | ⛔ No autorizado |
| K | Calidad y cierre | Suite completa, accesibilidad, docs, APK demo | — | ⛔ No autorizado |

### 6.1 Archivos previstos por incremento

| Incremento | Archivos |
|---|---|
| B | `entity/PerfilTrabajoEntity.kt`, `dao/PerfilTrabajoDao.kt`, `repository/PerfilTrabajoRepository.kt`, `ui/settings/PerfilTrabajoViewModel.kt`, `ui/settings/SettingsScreen.kt`, `MiControlDatabase.kt` (v5), `strings.xml` |
| C | `domain/BloqueJornada.kt`, `domain/CalculadorBloqueJornada.kt` |
| D | `entity/JornadaEntity.kt`, `dao/JornadaDao.kt`, `repository/JornadaRepository.kt`, `ui/jornada/*`, `ui/navigation/NavGraph.kt`, `MiControlDatabase.kt` (v6) |
| E | `entity/ViajeEntity.kt`, `dao/ViajeDao.kt`, `repository/ViajeRepository.kt`, `ui/viaje/ViajeUiState.kt`, `ui/viaje/ViajeViewModel.kt`, `ui/viaje/RegistrarViajeScreen.kt`, `ui/viaje/ListaViajesScreen.kt`, `MiControlDatabase.kt` (v7) |
| F | `entity/TanqueoEntity.kt`, `dao/TanqueoDao.kt`, `repository/TanqueoRepository.kt`, `ui/tanqueo/*`, `MiControlDatabase.kt` (v8) |
| G | `domain/CalculadoraKilometros.kt`, `domain/CalculadoraNetos.kt` |
| H | `entity/JornadaEntity.kt` (ampliación), `MiControlDatabase.kt` (v9), `ui/jornada/*`, `strings.xml` |
| I | `ui/dashboard/DashboardUiState.kt`, `DashboardViewModel.kt`, `DashboardScreen.kt` |
| J | `ui/stats/StatsUiState.kt`, `StatsViewModel.kt`, `StatsScreen.kt`, `StatsPeriodo.kt` |
| K | Revisión transversal + `PROJECT_STATUS.md`, `TASKS.md`, `README.md` |

---

## 7. Riesgos técnicos

| ID | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| R-01 | Reinterpretar `viajes.valor` rompería las consultas agregadas y las pruebas existentes | Alto | `valor` sigue siendo el precio final acordado; lo nuevo entra como columna con `DEFAULT` |
| R-02 | Doble conteo de la gasolina (tanqueo + gasto) | Alto | Enlace identificable tanqueo ↔ gasto, sincronizado al crear, editar y eliminar (ADR-001 D-06 a D-08) |
| R-03 | Modelo de kilómetros ambiguo | Alto | Odómetro (D-12); tramos vacíos opcionales después (D-13) |
| R-04 | Migraciones v5…v9 sin validación automática (`exportSchema = false`) | Medio | Un `MigracionTest` a mano por salto, como ya se hace |
| R-05 | Persistir valores derivados (rendimiento, costo/km, netos) | Medio | Se calculan, no se guardan (D-16) |
| R-06 | Decimales en el precio por litro | Medio | Dinero en `Long`; decimales solo al mostrar (D-15) |
| R-07 | Notificaciones del sistema: permisos y dependencias nuevas | Medio | Recordatorios internos (D-09, D-10) |
| R-08 | Crecimiento de `jornadas` por el checklist | Bajo | Booleanos con valor por defecto; tabla aparte solo si crece |
| R-09 | El neto económico depende de las reservas por km | Medio | No iniciar G antes que B |

---

## 8. Cierre del Incremento A

- [x] Auditoría verificada contra el código de este proyecto.
- [x] Brecha documentada por área.
- [x] Choques con `AGENTS.md` resueltos.
- [x] Decisiones en `ADR-001` (D-01 a D-19).
- [x] Plan por incrementos ejecutables de una sola tarea.
- [x] Riesgos con mitigación.
- [x] Sin cambios de código, Gradle, Room, pruebas ni APK; sin migraciones.
- [x] `PROJECT_STATUS.md` y `TASKS.md` actualizados.
- [ ] Revisión y autorización del siguiente incremento.

---

## 9. Referencias

- `docs/ADR-001-control-diario.md`
- `AGENTS.md`, `PROJECT_STATUS.md`, `TASKS.md`, `docs/SRS_MiControlDiDi_v1.docx`
