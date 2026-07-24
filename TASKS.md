# Tareas de MiControlDiDi — Backlog refinado

> Refinado el 24-jul-2026 tras el análisis técnico del SRS v1.0.

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

## Fase 2 — Capa de datos: Viajes (Sprint 1 — Semana 1) — En progreso
| Tarea | Prioridad | Dependencias | Estado |
|---|---|---|---|
| **2.1** Crear entidad Room `ViajeEntity` | [H] | Fase 1 | ✅ Creada |
| **2.2** Crear DAO `ViajeDao` (insert, obtenerTodos ordenado DESC) | [H] | 2.1 | ✅ Creado |
| **2.3** Crear `MiControlDatabase` (versión 1, solo ViajeEntity) | [H] | 2.1 | ✅ Creada, esquema exportado |
| **2.4** Crear `ViajeRepository` con validaciones | [H] | 2.2, 2.3 | ✅ Creado |
| **2.5** Configurar Gradle: Room + KSP | [H] | Fase 1 | ✅ KSP 2.1.20-1.0.32, Room 2.8.4 |
| **2.6** Pruebas unitarias de entidad y repositorio | [H] | 2.1, 2.4 | ✅ 11 pruebas creadas y pasan |
| **—** Otras entidades (CategoriaGasto, Gasto, Meta, Config) | [H/M/B] | — | ⏳ Pendiente (siguiente sprint) |

### Criterios de aceptación — Fase 2 (viajes)
- ✅ `ViajeEntity` con campos: id, fechaHora, valor, propina, observacion.
- ✅ `ingresoTotal` es calculado (valor + propina), no persistido como columna.
- ✅ `ViajeDao.insertar` inserta y retorna el id.
- ✅ `ViajeDao.obtenerTodos` retorna Flow<List<ViajeEntity>> ordenado DESC.
- ✅ `MiControlDatabase` versión 1 con `exportSchema = true`.
- ✅ `ViajeRepository.insertar` valida: valor > 0, propina >= 0.
- ✅ KSP + Room configurados, `assembleDebug` BUILD SUCCESSFUL.
- ✅ 11 pruebas unitarias pasan (`testDebugUnitTest` BUILD SUCCESSFUL).
- ❌ Pruebas instrumentadas con Room in-memory: no ejecutadas (no hay dispositivo conectado).

---

## Fase 3 — Viajes CRUD (Sprint 1 — Semana 2)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 3.1 Crear `TripViewModel` con estados: lista, formulario, detalle | [H] | 2.11 |
| 3.2 Crear pantalla de listado de viajes ordenada por fecha descendente | [H] | 3.1 |
| 3.3 Crear pantalla de formulario de viaje (fecha, valor, propina opcional, observación opcional) | [H] | 3.1 |
| 3.4 Implementar validación: valor > 0, propina >= 0, fecha obligatoria | [H] | 3.3 |
| 3.5 Implementar edición de viaje reutilizando el formulario | [H] | 3.3 |
| 3.6 Implementar eliminación de viaje con diálogo de confirmación | [H] | 3.1 |
| 3.7 Manejar estado vacío en listado de viajes | [H] | 3.2 |
| 3.8 Implementar cálculo de ingreso del viaje (valor + propina) | [H] | 3.1 |
| 3.9 Crear pruebas unitarias para `TripViewModel` y validaciones | [H] | 3.1 |

### Criterios de aceptación — Fase 3
- Registrar viaje: guarda datos válidos, muestra error si inválidos, actualiza listado.
- Consultar viajes: lista ordenada descendente, muestra valor total (viaje + propina), estado vacío si no hay datos.
- Editar viaje: precarga datos existentes, guarda cambios, actualiza listado.
- Eliminar viaje: muestra confirmación, desaparece del listado tras aceptar.
- Pruebas unitarias pasan.

---

## Fase 4 — Gastos CRUD (Sprint 2 — Semana 1)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 4.1 Crear `ExpenseViewModel` con estados: lista, formulario, detalle | [H] | 2.12, 2.13 |
| 4.2 Crear pantalla de listado de gastos ordenada por fecha descendente | [H] | 4.1 |
| 4.3 Crear pantalla de formulario de gasto (fecha, categoría, valor, descripción opcional) | [H] | 4.1 |
| 4.4 Implementar selector de categorías desde las sembradas | [H] | 4.3 |
| 4.5 Implementar validación: valor > 0, fecha obligatoria, categoría obligatoria | [H] | 4.3 |
| 4.6 Implementar edición de gasto reutilizando el formulario | [H] | 4.3 |
| 4.7 Implementar eliminación de gasto con diálogo de confirmación | [H] | 4.1 |
| 4.8 Manejar estado vacío en listado de gastos | [H] | 4.2 |
| 4.9 Vincular navegación Viajes ↔ Gastos desde Dashboard | [H] | 3.2, 4.2 |
| 4.10 Crear pruebas unitarias para `ExpenseViewModel` y validaciones | [H] | 4.1 |

### Criterios de aceptación — Fase 4
- Registrar gasto: guarda con categoría, muestra error si falta categoría o valor inválido.
- Consultar gastos: lista ordenada descendente con categoría visible.
- Editar y eliminar: mismo comportamiento que viajes.
- Pruebas unitarias pasan.

---

## Fase 5 — Dashboard y balance (Sprint 2 — Semana 2)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 5.1 Crear lógica de cálculo de balance (ingresos, gastos, ganancia neta por periodo) | [H] | 2.11, 2.12 |
| 5.2 Crear `DashboardViewModel` consolidando datos de viajes y gastos | [H] | 5.1 |
| 5.3 Crear pantalla Dashboard con resumen del día (ingresos, gastos, ganancia neta) | [H] | 5.2 |
| 5.4 Implementar selector de periodo (día, semana, mes) en Dashboard | [H] | 5.3 |
| 5.5 Mostrar accesos rápidos "Registrar viaje" y "Registrar gasto" en Dashboard | [H] | 5.3 |
| 5.6 Manejar estado vacío en Dashboard (ceros y mensaje informativo) | [H] | 5.3 |
| 5.7 Aplicar RN-07: mostrar valores en COP con formato legible | [H] | 5.3 |
| 5.8 Crear pruebas unitarias para cálculos de balance y periodos | [H] | 5.1 |

### Criterios de aceptación — Fase 5
- Dashboard muestra día actual al abrir la app.
- Al cambiar a semana, suma correctamente los 7 días (inicio lunes, según DP-04 pendiente).
- Al cambiar a mes, suma correctamente desde el día 1 hasta el último.
- Ganancia neta = ingresos − gastos del mismo periodo.
- Los valores se muestran en formato COP.
- Acceso rápido a registro de viaje y gasto funciona.
- Pruebas unitarias pasan.

---

## Fase 6 — Edición global, filtros y metas (Sprint 3 — Semana 1)
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 6.1 Implementar filtro por rango de fechas en listado de viajes | [M] | 3.2 |
| 6.2 Implementar filtro por rango de fechas y categoría en listado de gastos | [M] | 4.2 |
| 6.3 Implementar pantalla de configuración de meta (diaria o mensual) | [M] | 2.14 |
| 6.4 Mostrar progreso de meta en Dashboard | [M] | 5.3, 6.3 |
| 6.5 Validar meta: valorObjetivo > 0, tipoPeriodo obligatorio | [M] | 6.3 |
| 6.6 Crear pantalla de estadísticas básicas (comparación por día/mes) | [M] | 5.1 |
| 6.7 Crear `StatsViewModel` | [M] | 5.1 |
| 6.8 Crear pruebas unitarias para filtros, metas y estadísticas | [M] | 6.1–6.7 |

### Criterios de aceptación — Fase 6
- Filtrar viajes por fecha: solo muestra los del rango.
- Filtrar gastos por fecha y categoría: solo muestra los del rango y categoría.
- Meta guardada persiste entre reinicios de app.
- Progreso de meta = (ganancia neta / meta) × 100.
- Estadísticas muestran cifras verificables contra los datos guardados.
- Pruebas pasan.

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
| Tarea | Prioridad | Dependencias |
|---|---|---|
| 8.1 Revisar estados vacío, error y carga en todas las pantallas | [H] | Fases 3–7 |
| 8.2 Verificar mensajes de error descriptivos en formularios | [H] | Fases 3–7 |
| 8.3 Probar todos los flujos principales sin conexión a Internet | [H] | Fases 3–7 |
| 8.4 Ejecutar suite completa de pruebas unitarias | [H] | Fases 3–7 |
| 8.5 Revisar accesibilidad: contraste, etiquetas, áreas táctiles | [M] | Fases 3–7 |
| 8.6 Verificar que no hay strings hardcodeadas (todo en resources) | [H] | Fases 3–7 |
| 8.7 Probar persistencia: cerrar y abrir app, datos intactos | [H] | Fases 3–7 |
| 8.8 Corregir errores críticos encontrados | [H] | 8.1–8.7 |
| 8.9 Preparar APK de demostración para usuario piloto | [M] | 8.8 |

### Criterios de aceptación — Fase 8
- No hay defectos críticos ni bloqueantes en los flujos principales del MVP.
- Todas las pruebas unitarias pasan.
- La app funciona completamente sin conexión a Internet.
- Los datos persisten después de cerrar y reabrir la app.
- No hay textos visibles hardcodeados en el código Kotlin.
- Se genera un APK funcional para prueba piloto.

---

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
