# Estado del proyecto MiControlDiDi

> Actualizado: 24-jul-2026 — Fase 3 (registrar y listar viajes) completada.

---

## Estado general
Fase 3 completada. UI de viajes funcional con ViewModel, navegación, validaciones y 25 pruebas automatizadas.

## Completado
- [x] **Fase 1 — Proyecto Android base.**
- [x] **Fase 2 — Persistencia local del módulo de viajes.**
- [x] **Fase 3 — Registrar y listar viajes (primera interfaz funcional).**
  - [x] `ViajeViewModel` con `StateFlow`, validaciones y `ViewModelProvider.Factory`.
  - [x] `ListaViajesScreen`: estado vacío, LazyColumn con claves, formato COP y fecha.
  - [x] `RegistrarViajeScreen`: valor obligatorio, propina opcional, observación opcional, botón Guardar/Cancelar.
  - [x] `NavGraph` con dos destinos: lista_viajes y registrar_viaje.
  - [x] `CurrencyFormatter` y `DateFormatter` para formato COP y fechas legibles.
  - [x] Navegación Compose integrada en `MainActivity`.
  - [x] Strings en español en `strings.xml`.
  - [x] `assembleDebug` → **BUILD SUCCESSFUL**.
  - [x] `testDebugUnitTest` → **BUILD SUCCESSFUL** (10 tests ViewModel + 11 previos = 21 unitarios).
  - [x] `connectedDebugAndroidTest` → **BUILD SUCCESSFUL** (4 tests en ALT-LX3).
  - [x] App instalada y lanzada en HONOR ALT-LX3.
  - [x] Esquema Room: exportSchema desactivado por incompatibilidad con kotlinx-serialization.

## Pendiente — Editar y eliminar viajes
- [ ] Pantalla de edición de viaje.
- [ ] Eliminación con confirmación.

## Pendiente — Fase 4
- [ ] Entidad y DAO de CategoriaGasto.
- [ ] Entidad y DAO de Gasto.
- [ ] CRUD completo de gastos.

## Pendiente — Fase 5 a 8
- [ ] Dashboard, filtros, metas, preferencias, calidad.

## Decisiones técnicas añadidas

| Elemento | Valor |
|---|---|
| Navigation Compose | `2.8.5` |
| lifecycle-viewmodel-compose | `2.8.7` |
| lifecycle-runtime-compose | `2.8.7` |
| Navegación | 2 rutas: `lista_viajes`, `registrar_viaje` |
| Moneda en UI | `$ #.##` con `NumberFormat`, locale `es_CO` |
| Fechas en UI | `dd/MM/yyyy HH:mm` con `SimpleDateFormat` |
| Room schema export | `false` (desactivado por incompatibilidad) |
| Pruebas totales | **25** (11 unitarias previas + 10 ViewModel + 4 instrumentadas) |
