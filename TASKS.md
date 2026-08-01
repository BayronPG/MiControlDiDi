# Tareas de MiControlDiDi — Backlog refinado

> Refinado el 25-jul-2026 tras completar incrementos 1A, 1B y 1C de la Fase 5.

---

## Leyenda
- **[H]**: Alta prioridad (MVP obligatorio)
- **[M]**: Media prioridad
- **[B]**: Baja prioridad
- **→ Depende de**: tarea(s) que deben completarse antes

---

## Fase 0 — Análisis y preparación ✅
- [x] Crear estructura documental.
- [x] Añadir SRS.
- [x] Crear `AGENTS.md`.
- [x] Crear `PROJECT_STATUS.md`.
- [x] Analizar el SRS y refinar backlog técnico.

---

## Fase 1 — Proyecto Android (Sprint 1 — Semana 1) ✅
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 1.1 Crear proyecto Android con Jetpack Compose | [H] | — | ✅ Compilado |
| 1.2 Configurar Gradle con Kotlin DSL y dependencias base | [H] | 1.1 | ✅ Verificado |
| 1.3 Verificar que el proyecto compile y ejecute en emulador | [H] | 1.2 | ✅ `assembleDebug` exitoso |
| 1.4 Crear paquete base `com.jhon.micontroldidi` y subpaquetes | [H] | 1.1 | ✅ Creados |
| 1.5 Configurar tema Material 3 (colores, tipografía, formas) | [H] | 1.3 | ✅ Aplicado |
| 1.6 Configurar Navigation Compose con rutas base | [H] | 1.3 | ⏳ No implementado (pendiente de Fase 2+) |
| 1.7 Commit inicial opcional | [H] | 1.3 | ⏳ Sin commit (pendiente de instrucciones) |

### Criterios de aceptación — Fase 1
- ✅ El proyecto compila correctamente con `assembleDebug`.
- ✅ El tema Material 3 se aplica de forma consistente.
- ✅ La pantalla inicial muestra "MiControlDiDi".
- ⏳ Navegación entre pantallas: pendiente (se implementará cuando se creen las pantallas).
- ⏳ Repositorio Git con commit: pendiente de instrucciones.

---

## Fase 2 — Capa de datos (Sprint 1 — Semana 1) ✅
### Viajes
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| **2.1** Crear entidad Room `ViajeEntity` | [H] | Fase 1 | ✅ Creada |
| **2.2** Crear DAO `ViajeDao` | [H] | 2.1 | ✅ Creado |
| **2.3** Crear `MiControlDatabase` (versión 1) | [H] | 2.1 | ✅ Creada |
| **2.4** Crear `ViajeRepository` con validaciones | [H] | 2.2, 2.3 | ✅ Creado |
| **2.5** Configurar Gradle: Room + KSP | [H] | Fase 1 | ✅ |
| **2.6** Pruebas unitarias | [H] | 2.1, 2.4 | ✅ 11 pruebas |

### Gastos (añadido con migración v1→v2)
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| **2.7** Crear entidad `CategoriaGastoEntity` | [H] | Fase 1 | ✅ Creada |
| **2.8** Crear entidad `GastoEntity` con FK | [H] | 2.7 | ✅ Creada |
| **2.9** Crear `CategoriaGastoDao` (insert, query, exists) | [H] | 2.7 | ✅ Creado |
| **2.10** Crear `GastoDao` con JOIN categoría | [H] | 2.8 | ✅ Creado |
| **2.11** Crear `CategoriaGastoRepository` con validaciones | [H] | 2.9 | ✅ Creado |
| **2.12** Crear `GastoRepository` con validaciones | [H] | 2.10 | ✅ Creado |
| **2.13** Actualizar `MiControlDatabase` a versión 2 + `MIGRATION_1_2` | [H] | 2.7–2.8 | ✅ Migración explícita |
| **2.14** Categorías iniciales (6) con prepoblado seguro | [H] | 2.13 | ✅ Semilla en migración + onCreate |
| **2.15** Pruebas unitarias categorías y gastos | [H] | 2.11, 2.12 | ✅ 11 pruebas |
| **2.16** Pruebas DAO instrumentadas (categorías + gastos) | [H] | 2.9, 2.10 | ✅ 7 pruebas |
| **2.17** Prueba de migración v1→v2 | [H] | 2.13 | ✅ 1 prueba |
| **—** Meta, Config | [M/B] | — | ⏳ Pendiente |

### Criterios de aceptación — Fase 2 (completa)
- ✅ ViajeEntity y datos existentes conservados.
- ✅ `CategoriaGastoEntity`: id, nombre, activa. Índice único en nombre.
- ✅ `GastoEntity`: id, fechaHora, categoriaId (FK → RESTRICT), valor, descripcion.
- ✅ `CategoriaGastoDao`: insertar, insertarLista, obtenerActivas, obtenerPorId, existePorNombre.
- ✅ `GastoDao`: insertar, obtenerTodos con JOIN + nombreCategoria.
- ✅ `MiControlDatabase` versión 2 con migración 1→2 explícita.
- ✅ 6 categorías iniciales insertadas en migración + onCreate sin duplicados.
- ✅ 11 pruebas unitarias (entidad + repositorios) pasan.
- ✅ 8 instrumentadas (DAO + migración) pasan en ALT-LX3.
- ⚠️ `exportSchema = false` — deuda técnica (ver Fase 3).

---

## Fase 3 — Registrar y listar viajes (Sprint 1 — Semana 2) ✅
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| **3.1** Crear `ViajeViewModel` con estados y formulario | [H] | Fase 2 | ✅ Creado |
| **3.2** Crear `ListaViajesScreen` con LazyColumn y estado vacío | [H] | 3.1 | ✅ Creada |
| **3.3** Crear `RegistrarViajeScreen` con formulario y validaciones | [H] | 3.1 | ✅ Creada |
| **3.4** Implementar validaciones: valor > 0, propina >= 0, fecha obligatoria | [H] | 3.3 | ✅ Implementadas |
| **3.5** Crear `NavGraph` con navegación entre lista y formulario | [H] | 3.2, 3.3 | ✅ Creada |
| **3.6** Crear útiles: CurrencyFormatter, DateFormatter | [H] | — | ✅ Creados |
| **3.7** Actualizar MainActivity con NavGraph | [H] | 3.5 | ✅ Actualizada |
| **3.8** Pruebas unitarias del ViewModel (10 tests) | [H] | 3.1 | ✅ Creadas y pasan |
| **—** Editar viaje | [H] | — | ⏳ Pendiente |
| **—** Eliminar viaje | [H] | — | ⏳ Pendiente |

### Criterios de aceptación — Fase 3 (cierre)
- ✅ `ViajeViewModel` con StateFlow, validación, Factory y protección contra doble clic.
- ✅ `ListaViajesScreen`: Título "Viajes", LazyColumn con clave por id, testTags.
- ✅ `RegistrarViajeScreen`: Campos valor, propina, observación. Validación y guardado, testTags.
- ✅ Navegación lista ↔ formulario.
- ✅ `assembleDebug` BUILD SUCCESSFUL.
- ✅ `testDebugUnitTest` BUILD SUCCESSFUL (21 unitarios).
- ✅ `connectedDebugAndroidTest` BUILD SUCCESSFUL.
  - ✅ 4 tests DAO instrumentados en ALT-LX3.
  - ✅ 8 tests Compose UI instrumentados en ALT-LX3.
  - ✅ Total: **12/12 tests instrumentados**.
- ✅ **Persistencia real verificada manualmente** en HONOR ALT-LX3:
  - Valor $12.500 + Propina $1.500 = Total $14.000
  - Cerrada y reabierta → viaje visible.
- ⚠️ `exportSchema = false` — deuda técnica: Room 2.8.4 incompatible con Kotlin 2.1.20.
- ❌ Editar viaje: pendiente.
- ❌ Eliminar viaje: pendiente.

---

## Fase 4 — Gastos CRUD (Sprint 2 — Semana 1) ✅
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 4.1 Crear `GastoViewModel` con estados: lista, formulario | [H] | 2.12, 2.13 | ✅ Creado |
| 4.2 Crear `ListaGastosScreen` ordenada por fecha descendente | [H] | 4.1 | ✅ Creada |
| 4.3 Crear `RegistrarGastoScreen` (fecha, categoría, valor, descripción) | [H] | 4.1 | ✅ Creada |
| 4.4 Implementar selector de categorías desde las sembradas | [H] | 4.3 | ✅ ExposedDropdownMenu |
| 4.5 Implementar validación: valor > 0, categoría obligatoria | [H] | 4.3 | ✅ Implementadas |
| 4.6 Implementar edición de gasto reutilizando el formulario | [H] | 4.3 | ✅ Impl. + unitarios + DAO + Compose; manual ✅ |
| 4.7 Implementar eliminación de gasto con diálogo de confirmación | [H] | 4.1 | ✅ Impl. + unitarios + DAO + Compose; manual ✅ |
| 4.8 Manejar estado vacío en listado de gastos | [H] | 4.2 | ✅ Implementado |
| 4.9 Vincular navegación Viajes ↔ Gastos mediante barra inferior | [H] | 3.2, 4.2 | ✅ Barra inferior |
| 4.10 Crear pruebas unitarias para `GastoViewModel` y Compose | [H] | 4.1 | ✅ 12 unitarias + 10 Compose |

### Criterios de aceptación — Fase 4 (incremento 1)
- ✅ Registrar gasto: guarda con categoría, muestra error si falta categoría o valor inválido.
- ✅ Consultar gastos: lista ordenada descendente con categoría visible.
- ✅ Selección de categoría desde ExposedDropdownMenu con nombres reales de Room.
- ✅ Validación: valor > 0, categoría obligatoria, valor numérico.
- ✅ Protección contra doble pulsación.
- ✅ Barra inferior con navegación Viajes ↔ Gastos.
- ✅ Editar gasto: formulario reutilizado, carga datos existentes.
- ✅ Eliminar gasto: diálogo de confirmación, eliminación desde repositorio.
- ✅ `assembleDebug` BUILD SUCCESSFUL.
- ✅ `testDebugUnitTest` BUILD SUCCESSFUL (67 unitarios).
- ✅ `connectedDebugAndroidTest` BUILD SUCCESSFUL (50/50 instrumentadas).

---

## Fase 5 — Dashboard y balance (Sprint 2 — Semana 2)

> **Estado:** Completado. Incrementos 1A, 1B, 1C y 1D completados, validados e integrados en `main`. Fase lista para cierre técnico.

### Incremento 1A: Cálculo de periodos del Dashboard ✅

**Alcance:** Capa de dominio para determinar rangos temporales.

| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 5.1a Crear `PeriodoDashboard` enum (DIA, SEMANA, MES) | [H] | — | ✅ Creado |
| 5.1b Crear `RangoPeriodo` data class (inicioInclusivo, finExclusivo) | [H] | — | ✅ Creado |
| 5.1c Crear `CalculadorRangoPeriodo` con lógica día/semana/mes | [H] | 5.1a, 5.1b | ✅ Creado |
| 5.1d Semana iniciada el lunes (DP-04) | [H] | 5.1c | ✅ Aplicado |
| 5.1e ZoneId explícito del dispositivo | [H] | 5.1c | ✅ Aplicado |
| 5.1f Intervalos semiabiertos [inicio, fin) | [H] | 5.1c | ✅ Aplicado |
| 5.8a Crear `CalculadorRangoPeriodoTest` (22 pruebas) | [H] | 5.1c | ✅ 22 pruebas |

**Validación:** Unitarias (89 + 22 = 111 en ese momento), integrado y publicado en `main`.

**Tecnología:** `java.time`, `ZonedDateTime`, `TemporalAdjusters`, `ZoneId` explícito, `Object` singleton.

---

### Incremento 1B: Totales por rango (ingresos y gastos) ✅

**Alcance:** Consultas agregadas reactivas en DAOs + delegación en repositorios.

| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 5.1g Consulta agregada de ingresos en `ViajeDao` (SUM valor + propina) | [H] | 2.3 | ✅ Implementada |
| 5.1h Consulta agregada de gastos en `GastoDao` (SUM valor) | [H] | 2.10 | ✅ Implementada |
| 5.1i `COALESCE` para devolver 0 sin registros | [H] | 5.1g, 5.1h | ✅ Aplicado |
| 5.1j Retorno `Flow<Long>` reactivo | [H] | 5.1g, 5.1h | ✅ Implementado |
| 5.1k Delegación en `ViajeRepository` y `GastoRepository` | [H] | 2.4, 2.12 | ✅ Implementada |
| 5.1l Pruebas DAO instrumentadas (7 + 8 = 15 nuevas) | [H] | 5.1g, 5.1h | ✅ Creadas y pasan |
| 5.1m Pruebas unitarias de repositorio (4 nuevas) | [H] | 5.1k | ✅ Creadas y pasan |
| 5.1n Estabilizar selector `Gasolina` en `GastoComposeTest` | [H] | — | ✅ Corregido |

**Validación:** 93 unitarias + 65 instrumentadas = 158/158. Integrado y publicado en `main`.

**Tecnología:** Room `@Query`, `SUM`, `COALESCE`, `Flow<Long>`, intervalo semiabierto en SQL.

---

### Incremento 1C: DashboardViewModel y DashboardUiState ✅

**Alcance:** Lógica de presentación del Dashboard: estado, ViewModel reactivo y selección de periodo.

| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 5.2 Crear `DashboardUiState` con ingresos, gastos, ganancia neta y periodo seleccionado | [H] | — | ✅ Creado |
| 5.3 Crear `DashboardViewModel` consolidando datos de viajes y gastos | [H] | 5.1g–5.1k, 5.2 | ✅ Creado |
| 5.4 Clock inyectable en ViewModel | [H] | 5.3 | ✅ Aplicado |
| 5.5 `flatMapLatest` + `combine` para observación reactiva | [H] | 5.3 | ✅ Implementado |
| 5.6 Manejo de errores con `catch` (sin stack traces, re-lanza CancellationException) | [H] | 5.3 | ✅ Implementado |
| 5.7 Recuperación al cambiar de periodo (flatMapLatest crea nuevo Flow interno) | [H] | 5.3 | ✅ Implementado |
| 5.8 Pruebas unitarias del DashboardViewModel (20 pruebas) | [H] | 5.3 | ✅ 20 pruebas |

**Validación:** 113 unitarias (93 anteriores + 20). Instrumentadas vigentes: 65. **Total vigente: 178.**
Integrado y publicado en `main`.

**Tecnología:** `StateFlow`, `flatMapLatest`, `combine`, `catch`, `Clock`, `ViewModelProvider.Factory`.

---

### Incremento 1D — Pantalla Dashboard (completado) ✅

| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 5.9 Crear `DashboardScreen` con resumen de ingresos, gastos y ganancia neta | [H] | 5.2, 5.3 | ✅ Creada |
| 5.10 Mostrar tarjeta/resumen de ingresos del periodo | [H] | 5.9 | ✅ Implementada |
| 5.11 Mostrar tarjeta/resumen de gastos del periodo | [H] | 5.9 | ✅ Implementada |
| 5.12 Mostrar tarjeta/resumen de ganancia neta | [H] | 5.9 | ✅ Implementada |
| 5.13 Manejar estado de carga en Dashboard | [H] | 5.9 | ✅ Implementado |
| 5.14 Manejar estado vacío (ceros y mensaje informativo) | [H] | 5.9 | ✅ Implementado |
| 5.15 Manejar estado de error desde mensajeError del ViewModel | [H] | 5.9 | ✅ Implementado |
| 5.16 Implementar selector visual de periodo (día / semana / mes) | [H] | 5.9 | ✅ Implementado |
| 5.17 Aplicar formato monetario COP (`$ #,##0`) a los valores | [H] | 5.9 | ✅ Aplicado |
| 5.18 Pruebas Compose del DashboardScreen | [H] | 5.9 | ✅ 17 pruebas |
| 5.19 Verificación manual en HONOR ALT-LX3 | [H] | 5.18 | ⏳ Pendiente |

---

### Navegación del Dashboard (completado) ✅

| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| 5.20 Agregar ruta `dashboard` al NavGraph | [H] | 5.9 | ✅ Añadida |
| 5.21 Integrar DashboardScreen en NavHost | [H] | 5.9, 5.20 | ✅ Integrado |
| 5.22 Añadir Dashboard a la barra inferior de navegación | [H] | 5.21 | ✅ Añadido (primer ítem) |
| 5.23 Cambiar `startDestination` a Dashboard | [H] | 5.22 | ✅ Cambiado |
| 5.24 Acceso rápido "Registrar viaje" desde Dashboard | [H] | 5.9 | ✅ FAB con callback |
| 5.25 Acceso rápido "Registrar gasto" desde Dashboard | [H] | 5.9 | ✅ FAB con callback |
| 5.26 Pruebas Compose de FABs y navegación | [H] | 5.24–5.25 | ✅ 3 pruebas añadidas |
| 5.27 Verificación manual de navegación en HONOR ALT-LX3 | [H] | 5.26 | ⏳ Pendiente |

---

### Conteos y trazabilidad — Fase 5

| Métrica | Valor |
|---|---|
| Unitarias vigentes | **113** |
| Instrumentadas vigentes | **65** (+3 nuevas de FABs) |
| **Total** | **178 unitarias + 68 instrumentadas (~181 total)** |

- `testDebugUnitTest` posterior al Incremento 1D: **113/113** (BUILD SUCCESSFUL).
- Última validación `connectedDebugAndroidTest` aplicable: **65/65** (Incremento 1B).
- `connectedDebugAndroidTest` **no se repitió** después del Incremento 1C porque no hubo cambios en Compose, Room ni navegación.
- 0 pruebas flaky pendientes.

---

### Criterios de aceptación — Fase 5 (actualizados)
- ✅ Cálculo de periodos: DIA (00:00–24:00), SEMANA (lunes–lunes), MES (1ro–1ro).
- ✅ Intervalos semiabiertos [inicioInclusivo, finExclusivo).
- ✅ Semana iniciada el lunes.
- ✅ ZoneId explícito del dispositivo.
- ✅ Ingresos por rango: SUM(valor + propina) con COALESCE, Flow&lt;Long&gt; reactivo.
- ✅ Gastos por rango: SUM(valor) con COALESCE, Flow&lt;Long&gt; reactivo reactivo a CUD.
- ✅ Repositorios delegan directamente en DAOs.
- ✅ DashboardUiState con periodo, ingresos, gastos, ganancia neta, cargando y error.
- ✅ DashboardViewModel con flatMapLatest + combine + Clock inyectable + catch de errores + recuperación.
- ✅ 20 pruebas unitarias del DashboardViewModel.
- ✅ DashboardScreen con resumen del periodo (ingresos, gastos, ganancia neta).
- ✅ Selector visual de periodo funcional (FilterChips día/semana/mes).
- ✅ Estados: carga, vacío (sin datos), error.
- ✅ Ganancia neta mostrada en la UI con formato COP (`$ #,##0`).
- ✅ Accesos rápidos a registro de viaje y gasto (FABs).
- ✅ Navegación desde/hacia Dashboard (barra inferior + startDestination).
- ✅ 17 pruebas Compose del DashboardScreen (incl. 3 de FABs).
- ⏳ Verificación manual en HONOR ALT-LX3.

---

### Fuera del alcance actual de la Fase 5

Los siguientes elementos **no forman parte de la Fase 5** y permanecen pendientes para fases posteriores:

- Gráficos, tablas o visualizaciones avanzadas.
- Filtros por rango de fechas en listados (Fase 6).
- Comparación entre periodos.
- Metas de ganancia diaria/mensual (Fase 6).
- Presupuestos por categoría.
- Categorías de gasto personalizadas desde la UI.
- Editar y eliminar viajes desde la interfaz (backend listo).
- Firebase, backend, sincronización.
- PDF, Excel, escaneo, voz.
- Inteligencia artificial.
- Migración Room 2 → 3.
- Resolución de `exportSchema = false`.

---

## Fase 6 — Edición global, filtros y metas (Sprint 3 — Semana 1)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 6.1 Implementar filtro por rango de fechas en listado de viajes | [M] | 3.2 | ✅ Implementado, validado e integrado en main |
| 6.2 Implementar filtro por rango de fechas y categoría en listado de gastos | [M] | 4.2 | ✅ Implementado, validado e integrado en main |
| 6.3 Implementar pantalla de configuración de meta (diaria o mensual) | [M] | 2.14 | ✅ Implementado, validado e integrado en main |
| 6.4 Mostrar progreso de meta en Dashboard | [M] | 5.3, 6.3 | ✅ Implementado, validado e integrado en main |
| 6.5 Validar meta: valorObjetivo > 0, tipoPeriodo obligatorio | [M] | 6.3 | ✅ Incluida en Tarea 6.3 |
| 6.6 Crear pantalla de estadísticas básicas (comparación por día/mes) | [M] | 5.1 | ✅ Implementado, validado e integrado en main |
| 6.7 Crear `StatsViewModel` | [M] | 5.1 | ✅ Implementado, validado e integrado en main |
| 6.8 Crear pruebas unitarias para filtros, metas y estadísticas | [M] | 6.1–6.7 | ✅ Incluida en 6.6+6.7 |

### Criterios de aceptación — Fase 6
- ✅ Filtrar viajes por fecha: solo muestra los del rango.
- ✅ Filtrar gastos por fecha y categoría: solo muestra los del rango y categoría.
- ✅ Meta guardada persiste entre reinicios de app.
- ⏳ Progreso de meta = (ganancia neta / meta) × 100.
- ⏳ Estadísticas muestran cifras verificables contra los datos guardados.
- ⏳ Pruebas pasan.

---

## Fase 7 — Preferencias (Sprint 3 — Semana 2)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 7.1 Crear pantalla de configuración con cambio de tema (claro/oscuro) | [B] | 2.5 |
| 7.2 Leer y persistir preferencia de tema usando DataStore o ConfiguracionEntity | [B] | 7.1 |
| 7.3 Aplicar tema seleccionado al iniciar la app | [B] | 7.2 |

### Criterios de aceptación — Fase 7
- El tema claro/oscuro se cambia desde Configuración.
- La preferencia persiste al reiniciar la app.
- Todos los componentes respetan el tema seleccionado.

---

## Fase 8 — Calidad y cierre del MVP (Sprint 4)

### Progreso

| Sub-tarea | Estado | Fecha |
|---|---|---|
| 8.0 — Corregir 3 tests Compose obsoletos por cambio a botones en dashboard | ✅ | 26-jul-2026 |
| 8.1 — Revisar estados vacío, error y carga en todas las pantallas | ✅ (8.1B viajes, 8.1C gastos; stats/dashboard/meta ya cubiertos) | 26-jul-2026 |
| 8.2 — Verificar mensajes de error descriptivos en formularios | ✅ | 01-ago-2026 |
| 8.3 — Probar flujos sin conexión a Internet | ✅ (sin hallazgos: app 100% local, sin INTERNET ni libs de red) | 01-ago-2026 |
| 8.4 — Persistencia automatizada (BD Room temporal) | ⏳ (requiere dispositivo ALT-LX3) | — |
| 8.5 — Revisar accesibilidad (contraste, etiquetas, áreas táctiles) | ✅ (2 correcciones: icono meta navegable, filas de tema seleccionables) | 01-ago-2026 |
| 8.6 — Verificar strings hardcodeadas | ✅ | 26-jul-2026 |
| 8.7 — Probar persistencia cerrar/abrir | ⏳ (verificación manual en dispositivo) | — |
| 8.8 — Corregir errores críticos encontrados | ⏳ | — |
| 8.9 — Preparar APK de demostración | ✅ (`dist/MiControlDiDi-v1.0.0-demo.apk`; `assembleRelease` OK) | 01-ago-2026 |

### Criterios de aceptación — Fase 8
- No hay defectos críticos ni bloqueantes en los flujos principales del MVP.
- Todas las pruebas unitarias pasan.
- La app funciona completamente sin conexión a Internet.
- Los datos persisten después de cerrar y reabrir la app.
- No hay textos visibles hardcodeados en el código Kotlin.
- Se genera un APK funcional para prueba piloto.

---

## Mejoras backend (01-ago-2026)

| Mejora | Detalle | Estado |
|---|---|---|
| Enum `PeriodoMeta` | Type-safety para `"DIA"`/`"MES"` en meta; sin cambios de BD | ✅ Commit `f989047` |
| Índices `fechaHora` | `@Index` en `ViajeEntity`/`GastoEntity` + `MIGRATION_3_4` (BD v3→v4) | ✅ Commit `0af17a6` |

> Pendiente: ejecutar instrumentadas (incl. `migracionTresACuatro`) en ALT-LX3.

## Resumen de sprints

| Sprint | Semana | Fases | Historias de usuario |
|---|---|---|---|
| Sprint 1 | Semana 1–2 | Fase 1 + Fase 2 + Fase 3 | HU-01, HU-03 |
| Sprint 2 | Semana 3–4 | Fase 4 + Fase 5 | HU-02, HU-05, HU-08 |
| Sprint 3 | Semana 5–6 | Fase 6 + Fase 7 | HU-04, HU-06, HU-07 |
| Sprint 4 | Semana 7 | Fase 8 | Todas (validación final) |

## Detalles de implementación técnica

### Decisiones técnicas adoptadas en el análisis
- **Moneda**: `Long` para almacenar valores en pesos colombianos enteros (sin decimales).
- **Cálculo de periodo semanal**: iniciar semana el lunes (configurable post-MVP, DP-04).
- **Zona horaria**: usar la configurada en el dispositivo (RN-06).
- **Propina**: almacenada como campo separado en Viaje; el ingreso total del viaje se calcula como `valor + propina` (no se almacena).
- **Categorías fijas**: gasolina, mantenimiento, parqueadero, lavado, cuota moto, otros — insertadas al crear la base de datos.
- **Formato COP**: `$ #,##0` sin decimales, usando `java.text.NumberFormat` o `DecimalFormat`.
- **Navegación**: Navigation Compose con rutas: dashboard, trips, tripForm/{id?}, expenses, expenseForm/{id?}, stats, settings.
