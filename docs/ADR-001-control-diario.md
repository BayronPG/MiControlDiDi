# ADR-001 — Control diario del trabajo en MiControlDiDi

> **Decisiones de arquitectura del proyecto MiControlDiDi.** Normativo para incrementos futuros.
> Actualizado: 12-sep-2026 — Incremento A (documentación). Sin cambios de código.

---

## Estado

**Aceptado** en D-01 a D-19. La implementación queda **condicionada a autorización explícita por incremento**: este ADR fija decisiones, no autoriza desarrollo.

| Campo | Valor |
|---|---|
| ID | ADR-001 |
| Fecha | 12-sep-2026 |
| Proyecto | MiControlDiDi (`C:\Proyectos\MiControlDiDi`) |
| Estado | Aceptado (decisiones); implementación no autorizada |
| Autorizado por | Responsable del proyecto |
| Ámbito | Alcance funcional, modelo de datos, unidades, recordatorios, tema y migraciones |
| Sustituye a | Ninguno |
| Modifica el SRS | No |
| Documento relacionado | `docs/AUDITORIA_CONTROL_DIARIO.md` |

---

## Contexto

MiControlDiDi es esta app y este proyecto. Su MVP está cerrado (Fases 0–6 y 8, 322/322 pruebas) sobre Room v4 con cuatro tablas: `viajes`, `categorias_gasto`, `gastos` y `metas`.

La propuesta que compartiste —jornada con checklist de seguridad, viajes con datos de plataforma, kilómetros vacíos, tanqueos, ganancia neta operativa y económica, y bienestar— fija el contexto de trabajo en **inDrive**. Permite que la app deje de ser un libro de cuentas y pase a acompañar el día de trabajo completo. Se apoya en lo que ya existe y **no** reemplaza nada del MVP.

El riesgo real es de **integridad de datos**: las unidades y el modelo que se decidan ahora condicionan todas las migraciones y cálculos siguientes. Este ADR los fija antes de escribir código.

---

## Decisiones

### D-01 a D-05 — Contexto de trabajo

| ID | Decisión | Justificación |
|---|---|---|
| **D-01** | Plataforma principal: **inDrive**. | Es la plataforma donde el conductor concentra su operación diaria; define el contexto de trabajo del que parte todo el plan. |
| **D-02** | Vehículo: **TVS Raider 125 FI**. | Define el consumo de referencia y el plan de mantenimiento. |
| **D-03** | Combustible: **gasolina extra**. | Es el combustible del vehículo; afecta precio y rendimiento. |
| **D-04** | Ciudad de operación: **Medellín**. | Contexto de precios, zonas y desplazamientos. |
| **D-05** | Horario laboral: **lunes a viernes, 6:00 a. m. a 3:00 p. m.** | Base de bloques, pausas y objetivos diarios. |

> D-01 a D-05 **no** son constantes en el código: son los valores iniciales del perfil de trabajo, editables por el usuario (incremento B).

### D-06 a D-08 — Gasolina, gasto y doble conteo

| ID | Decisión | Justificación |
|---|---|---|
| **D-06** | El tanqueo **genera o enlaza** un `GastoEntity` de categoría **Gasolina**. | El dinero de la gasolina debe existir una sola vez en los gastos. |
| **D-07** | Existirá una **relación identificable** entre tanqueo y gasto. | Trazabilidad de cada peso gastado en combustible. |
| **D-08** | Editar o eliminar un tanqueo **mantiene sincronizado** su gasto. | Evita registros huérfanos y descuadres del neto operativo. |

**Consecuencia:** el neto operativo no sumará el tanqueo aparte cuando ya esté contabilizado como gasto (riesgo R-02 del documento de auditoría).

### D-09 y D-10 — Recordatorios

| ID | Decisión | Justificación |
|---|---|---|
| **D-09** | Primera versión con **recordatorios internos** (banners y tarjetas). | Sin dependencias ni permisos nuevos. |
| **D-10** | **No** implementar todavía notificaciones del sistema, `WorkManager` ni permisos nuevos. | Mantener el proyecto sin dependencias innecesarias ni servicios en segundo plano. |

### D-11 — Tema

| ID | Decisión | Justificación |
|---|---|---|
| **D-11** | Mantener el tema **exclusivamente claro**; no restaurar el modo oscuro. | Coherente con la Mejora 2 (02-ago-2026) y el retiro de la Fase 7. |

### D-12 y D-13 — Kilómetros

| ID | Decisión | Justificación |
|---|---|---|
| **D-12** | Modelo de **odómetro**:<br>• totales = odómetro final − inicial;<br>• con pasajero = suma de distancias de los viajes;<br>• vacíos = totales − con pasajero. | Mínimo de datos digitados y cada cifra verificable. |
| **D-13** | Tramos vacíos **opcionales** (recogida, reposicionamiento, regreso a casa) en una etapa posterior. | Mejora la lectura de los vacíos sin imponer trabajo extra al inicio. |

### D-14 y D-15 — Unidades

| ID | Decisión | Justificación |
|---|---|---|
| **D-14** | Distancias en **metros con `Long`**. | Sin punto flotante; precisión exacta. |
| **D-15** | Dinero en **`Long`**. | Regla del proyecto (pesos colombianos enteros). |

### D-16 — Indicadores derivados

| ID | Decisión | Justificación |
|---|---|---|
| **D-16** | Los indicadores derivados (rendimiento, costo por kilómetro, netos) **se calculan y no se persisten**. | No guardar valores que puedan calcularse de otros datos. |

### D-17 y D-18 — Restricciones técnicas

| ID | Decisión | Justificación |
|---|---|---|
| **D-17** | **Sin GPS**: las distancias se ingresan a mano. | Alcance del MVP. |
| **D-18** | **Sin `destructiveMigration`**: toda evolución del esquema con migración explícita. | Preservar los datos del usuario. |

### D-19 — Alcance del Incremento A

| ID | Decisión | Justificación |
|---|---|---|
| **D-19** | El Incremento A **no modifica código funcional ni crea migraciones**. | Incremento exclusivamente documental. |

---

## Consecuencias

**A favor**

- El modelo queda definido antes de la primera migración: menos riesgo de rehacer esquema y cálculos.
- Unidades y reglas de derivación alineadas con las reglas del proyecto.
- El enlace tanqueo–gasto elimina el doble conteo de combustible.
- Los recordatorios internos mantienen la app sin permisos ni red.

**Compromisos**

- Todo incremento que toque la base de datos lleva migración explícita y prueba de migración escrita a mano (`exportSchema = false` impide usar `MigrationTestHelper`).
- El checklist crecerá `jornadas`; si pierde claridad, se evaluará una tabla aparte.
- El neto económico depende de las reservas por kilómetro: no se implementa antes que el perfil (incremento B).
- Distancias y tiempos son manuales: la precisión depende del usuario.

---

## Alternativas descartadas

| Alternativa | Por qué no |
|---|---|
| Renombrar `viajes.valor` a `precioFinal` | Rompería consultas, repositorios y las 322 pruebas sin beneficio funcional |
| Tanqueo solo como dato físico, sin gasto | El combustible desaparecería de los gastos y descuadraría el neto |
| Tanqueo como gasto, sin entidad propia | Se perderían kilometraje, tipo de combustible y lleno/parcial, base del rendimiento |
| Persistir rendimiento y costo por km | Viola la regla de no guardar valores calculables |
| Notificaciones del sistema desde el inicio | Permisos y dependencias nuevas sin necesidad |
| Desglose manual obligatorio de vacíos | Más trabajo para el conductor y datos menos fiables |
| Restaurar el modo oscuro | Contradice una decisión de producto ya validada |

---

## Cumplimiento

- Este ADR **no autoriza** implementación: cada incremento requiere autorización explícita e individual.
- Los cambios futuros sobre estas decisiones se registran en un ADR nuevo que sustituya o complemente a ADR-001.
- El avance se registra en `docs/AUDITORIA_CONTROL_DIARIO.md` y en `TASKS.md`.
