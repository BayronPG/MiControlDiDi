# MiControlDiDi

Aplicación Android para controlar ingresos, gastos y ganancias de un conductor de moto.

## Estado del proyecto

MVP en desarrollo. Capa de datos completa (viajes, categorías de gasto, gastos).  
Interfaz de viajes funcional (registrar y listar). Interfaz de gastos y dashboard pendientes.

## Stack técnico

- **Kotlin** — lenguaje principal.
- **Jetpack Compose** — UI declarativa.
- **Material 3** — diseño siguiendo Material Design 3.
- **Room** — persistencia local con SQLite.
- **KSP** — procesador de anotaciones de Room.
- **MVVM** — separación de capas UI y datos.
- **Repository Pattern** — abstracción entre ViewModel y DAO.
- **Coroutines y Flow** — operaciones asíncronas y reactivas.
- **Navigation Compose** — navegación entre pantallas.
- **Gradle Kotlin DSL** — configuración del proyecto.

## Funcionalidades implementadas

- [x] Registrar viajes con valor, propina y observación.
- [x] Listar viajes ordenados por fecha descendente.
- [x] Persistencia local de viajes (Room).
- [x] Capa de datos de categorías de gasto con 6 categorías iniciales.
- [x] Capa de datos de gastos con clave foránea hacia categorías.
- [x] Migración de base de datos Room versión 1 → 2.
- [x] Unicidad case-insensitive en nombres de categoría (COLLATE NOCASE).

## Funcionalidades pendientes

- [ ] Interfaz de gastos (listado, formulario, editar, eliminar).
- [ ] Dashboard diario, semanal y mensual.
- [ ] Editar y eliminar viajes desde la interfaz.
- [ ] Filtros por rango de fechas (viajes y gastos).
- [ ] Metas de ganancia.
- [ ] Estadísticas básicas.
- [ ] Preferencias (tema claro/oscuro).

## Batería de pruebas

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Unitarias | 34 | ✅ |
| Instrumentadas | 23 | ✅ |
| **Total** | **57** | ✅ |

## Deuda técnica

- `exportSchema` permanece temporalmente en `false` por incompatibilidad entre Room 2.8.4 y Kotlin 2.1.20.

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
