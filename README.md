# MiControlDiDi

Aplicación Android para controlar ingresos, gastos y ganancias de un conductor de moto, adaptada al
control del día de trabajo. Funciona **completamente sin conexión a Internet** y el manifiesto **no
declara ningún permiso**.

## Estado del proyecto (actualizado el 13-sep-2026)

- **Fases 0 a 6 y Fase 8 cerradas.** MVP completo y en uso.
- **Fase 7 retirada del alcance** por decisión de producto (02-ago-2026): la app conserva un único tema claro.
- **Control diario del trabajo:** incrementos **A** (auditoría + ADR), **B** (perfil de trabajo),
  **C** (horario laboral), **D** (registro de jornada) y **E** (viaje adaptado a inDrive) cerrados y
  validados en dispositivo. Los incrementos **F–K no están autorizados**.
- **Estabilización previa al incremento E cerrada** (13-sep-2026): corregidos los hallazgos H-01, H-02,
  H-05, H-06 y H-08 de la auditoría completa (`docs/AUDITORIA_COMPLETA_2026-09-13.md`).
- **Instantánea de pruebas al 13-sep-2026:** 344 unitarias y 142 instrumentadas (dispositivo ALT-LX3).

> La **fuente única** del estado detallado, de la trazabilidad por fase y del desglose de pruebas por
> archivo es **`PROJECT_STATUS.md`**. Los conteos de este documento son una instantánea.

## Stack técnico

- **Kotlin** — lenguaje principal.
- **Jetpack Compose** + **Material 3** — interfaz declarativa.
- **Room** — persistencia local con SQLite.
- **KSP** — procesador de anotaciones de Room.
- **MVVM** + **Repository Pattern** — separación entre interfaz y datos.
- **StateFlow / Flow** y **Coroutines** — estado reactivo y operaciones asíncronas.
- **Navigation Compose** — navegación entre pantallas.
- **Gradle Kotlin DSL** — configuración del proyecto.
- **JUnit 4** y **Compose UI Test** — pruebas unitarias e instrumentadas.

## Arquitectura

```
Compose UI → ViewModel → Repository → DAO → Room
```

Los composables no acceden a Room y los ViewModel no acceden a los DAO.

## Estructura del repositorio

```
C:\Proyectos\MiControlDiDi\   ← raíz Git (espacio de trabajo)
├── AGENTS.md · PROJECT_STATUS.md · TASKS.md · README.md
├── app\                       ← raíz del proyecto Gradle
│   └── app\                   ← módulo Android :app
├── docs\                      SRS (.docx), ADR-001 y auditorías
├── recursos\ · disenos\        material de apoyo
└── dist\                       APK demo y capturas (ignorado por Git)
```

La raíz Git y la raíz Gradle **no coinciden**: abre en Android Studio la carpeta `app`.

## Funcionalidades implementadas

**Viajes**
- Registrar, consultar, editar y eliminar viajes (valor, propina y observación).
- Datos de plataforma del viaje: plataforma (prellenada desde el perfil), zona, distancia en kilómetros,
  forma de pago (efectivo, transferencia, tarjeta u otro) y peaje como dato informativo del viaje.
- Listado descendente por fecha y filtro por rango de fechas.
- Al editar un viaje se muestra su fecha original, no la fecha actual.

**Gastos**
- Registrar, consultar, editar y eliminar gastos con categoría, valor y descripción.
- Seis categorías iniciales (Gasolina, Mantenimiento, Parqueadero, Lavado, Cuota de la moto, Otros)
  con unicidad de nombre case-insensitive.
- Filtro por rango de fechas y protección ante un ID inexistente.

**Balance y metas**
- Ingresos, gastos y ganancia neta por día, semana y mes.
- Dashboard con meta activa, progreso y accesos rápidos a registrar viaje y gasto.
- Estadísticas del periodo actual frente al anterior.

**Control diario del trabajo**
- Perfil de trabajo editable (plataforma, vehículo, combustible, ciudad, días, horario, umbral de
  kilómetros vacíos y reservas por kilómetro calculadas).
- Horario laboral por bloques con pausas, progreso y modo regreso (dominio puro).
- Registro de jornada con odómetro inicial, combustible, zona, meta del día, energía, clima y
  **revisión previa de 12 puntos de seguridad** con aviso no bloqueante en llantas, frenos y luces.

## Navegación

- Destino inicial: **`dashboard`**.
- Barra inferior con cinco destinos e iconos diferenciados: Inicio, Viajes, Gastos, Datos y Ajustes.
- Rutas: `dashboard`, `lista_viajes`, `registrar_viaje`, `registrar_viaje/{viajeId}`, `lista_gastos`,
  `registrar_gasto`, `registrar_gasto/{gastoId}`, `configurar_meta`, `configurar_perfil`,
  `registrar_jornada`, `estadisticas` y `configuracion`.

## Base de datos

- **Archivo:** `micontrol_didi.db` · **Versión Room: 7**
- **Tablas (6):** `viajes`, `categorias_gasto`, `gastos`, `metas`, `perfil_trabajo`, `jornadas`
- **Migraciones explícitas (6):** `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5`,
  `MIGRATION_5_6`, `MIGRATION_6_7`. **Sin** `fallbackToDestructiveMigration()`.
- **`exportSchema = false`** (deuda técnica por incompatibilidad entre Room 2.8.4 y Kotlin 2.1.20):
  sin esquemas exportados no se puede usar `MigrationTestHelper`, así que las migraciones se validan con
  `MigracionTest` escrito a mano más `PersistenciaTest` sobre una base de archivo real.
- Dinero y distancias se guardan como `Long` (pesos colombianos enteros y metros).

## Batería de pruebas

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Unitarias (`src/test`) | 344 | ✅ 0 fallos, 0 omitidas |
| Instrumentadas (`src/androidTest`) | 142 | ✅ 0 fallos, 0 errores, 0 omitidas |
| **Total** | **486** | ✅ |

- Las pruebas instrumentadas se ejecutan **solo en el dispositivo físico ALT-LX3** (nunca en emulador) y
  **una sola corrida a la vez**: las corridas solapadas reinstalan la app y generan fallos espurios.
- El desglose por archivo, la trazabilidad por fase y las incidencias corregidas están en
  `PROJECT_STATUS.md`.

## Cómo abrir el proyecto

El proyecto Android está en la carpeta `app`. Ábrela directamente en **Android Studio** como proyecto
existente.

### Comandos básicos

Ejecutar desde `C:\Proyectos\MiControlDiDi\app`:

```bash
# Compilar el proyecto
.\gradlew.bat assembleDebug

# Ejecutar pruebas unitarias
.\gradlew.bat testDebugUnitTest

# Ejecutar pruebas instrumentadas (requiere el dispositivo ALT-LX3 conectado)
.\gradlew.bat connectedDebugAndroidTest
```

## Documentos importantes

- `docs/SRS_MiControlDiDi_v1.docx` — requisitos del producto.
- `AGENTS.md` — reglas obligatorias para el desarrollo.
- `TASKS.md` — backlog de trabajo y criterios de aceptación por incremento.
- `PROJECT_STATUS.md` — estado actual detallado (fuente única).
- `docs/ADR-001-control-diario.md` — decisiones de arquitectura del control diario.
- `docs/AUDITORIA_CONTROL_DIARIO.md` y `docs/AUDITORIA_COMPLETA_2026-09-13.md` — auditorías.

## Fuera del alcance

Firebase, backend o sincronización; PDF, Excel, voz o escaneo; inteligencia artificial; GPS;
notificaciones del sistema y permisos nuevos; modo oscuro; integración directa con DiDi.
