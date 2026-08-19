# Guía de Development Sprint Driven (DSD)

Esta guía sigue el marco de trabajo DSD para desarrollo individual, organizando el avance en tareas y etapas sin ceremonias de reunión. El objetivo es **capturar las especificaciones del proyecto F1 y registrar todas las actualizaciones** realizadas a lo largo de su construcción.

---

## 1. Características del Framework DSD

### 1.1 Trabajo por Tareas
- El proyecto se divide en tareas concretas y pequeñas.
- Cada tarea define un entregable claro y verificable.
- Al terminar una tarea, se registra en el historial de actualizaciones.

### 1.2 Inventario de Tareas
- Antes de avanzar, se listan las tareas pendientes con:
  - **Prioridad** (Alta, Media, Baja).
  - **Estado** (Pendiente, En progreso, Completada, Bloqueada).
  - **Dependencias** (qué debe estar hecho antes).

### 1.3 Especificaciones del Proyecto
- Todo el alcance del proyecto se documenta antes de codificar.
- Los cambios de alcance se reflejan aquí y quedan rastreados en el historial.

### 1.4 Historial de Actualizaciones
- Registro cronológico de cada cambio relevante.
- Cada entrada incluye: fecha, descripción del cambio y estado.

---

## 2. Especificación del Proyecto

**Nombre:** Simulación de Fórmula 1

**Tecnología:** Java 17 (Maven)

### 2.1 Objetivos
- Simulación interactiva de Fórmula 1.
- Administración de circuitos, pilotos y vehículos.
- Personalización de carreras (clima, reglajes, estrategias).

### 2.2 Módulos previstos
| Módulo       | Funcionalidades |
|--------------|-----------------|
| Circuitos    | CRUD + búsqueda por nombre o ubicación |
| Pilotos      | CRUD + atributos (experiencia, habilidades) |
| Vehículos    | CRUD + atributos (velocidad, aceleración, consumo) |
| Simulación   | Configuración de condiciones y ejecución de carrera |

### 2.3 Persistencia
- Persistencia real en archivos JSON dentro de `data/` (Jackson), con `HashMap` en memoria para acceso rápido.
- Las referencias entre entidades (vehículo → piloto) se guardan por nombre y se resuelven al cargar.
- Pendiente futuro: persistencia de resultados de carrera (historial).

---

## 3. Historial de Actualizaciones

| Fecha       | Descripción del cambio | Estado |
|-------------|------------------------|--------|
| 04/08/2026  | Creación del framework DSD y esta guía | Completada |
| 04/08/2026  | Se incorporó README.md con especificación inicial del proyecto | Completada |
| 05/08/2026  | Se creó la clase `Circuito` con atributos (nombre, kilometros, ubicacion) | Completada |
| 16/08/2026  | Se creó la clase `Vehiculo` con validación de velocidad y desgaste de neumáticos | Completada |
| 16/08/2026  | Se creó la clase `Piloto` con validaciones de habilidad y experiencia | Completada |
| 16/08/2026  | Se agregó relación de agregación entre `Vehiculo` y `Piloto` con validación | Completada |
| 16/08/2026  | Se creó el puerto de salida `ClimaServicePort` para el clima | Completada |
| 16/08/2026  | Se completó `SimulacionService` con motor matemático de vueltas | Completada |
| 16/08/2026  | Se creó el esqueleto inicial de `ClimaHttpAdapter` con retorno mock | Completada |
| 16/08/2026  | Se implementó `ClimaHttpAdapter` conectado a la API de wttr.in | Completada |
| 16/08/2026  | Se estructuró `VentanaSimulacion` e instanciaron componentes gráficos | Completada |
| 16/08/2026  | Se configuró `FlowLayout` y se añadieron componentes a `VentanaSimulacion` | Completada |
| 16/08/2026  | Se conectó y visibilizó `VentanaSimulacion` desde `Main` | Completada |
| 17/08/2026  | Se inyectaron `ClimaHttpAdapter` y `SimulacionService` en `VentanaSimulacion` desde `Main` | Completada |
| 17/08/2026  | Se conectó `SimulacionService` en el botón y se imprimió telemetría en `JTextArea` | Completada |
| 17/08/2026  | Fase 0.1: corrección de unidades km/m del circuito y truncado del tiempo de vuelta | Completada |
| 17/08/2026  | Fase 0.2: corrección del EDT de Swing, actualizaciones de UI vía `SwingUtilities.invokeLater` | Completada |
| 17/08/2026  | Fase 0.3: clima consultado una sola vez por carrera (`consultarClima` + `simularVuelta` con clima) | Completada |
| 17/08/2026  | Fase 0.4: barra de progreso con progreso real y etiquetas ("Vuelta X de Y", "Carrera finalizada") | Completada |
| 17/08/2026  | Fase 1: creación de puertos de salida de repositorios (Circuito, Piloto, Vehiculo) | Completada |
| 17/08/2026  | Fase 1: implementación de repositorios en memoria (HashMap) | Completada |
| 17/08/2026  | Fase 1: puertos de entrada y servicios CRUD con inyección de repositorios | Completada |
| 17/08/2026  | Fase 1: cableado de repositorios y servicios en `Main` con datos de prueba | Completada |
| 17/08/2026  | Fase 2: `VentanaPrincipal` de navegación conectada en `Main` | Completada |
| 17/08/2026  | Fase 2: ventanas CRUD de Circuitos, Pilotos y Vehículos | Completada |
| 17/08/2026  | Fase 2: `VentanaSimulacion` integrada con circuitos y vehículos registrados | Completada |
| 18/08/2026  | Fase 4: motor de carrera en vivo multi-auto (`CarreraEnVivo`) con desgaste, paradas en boxes, abandonos (DNF) y vuelta rápida | Completada |
| 18/08/2026  | Fase 4: `VentanaSimulacion` con carrera en vivo: ranking en vivo, eventos con colores F1 y resultado final | Completada |
| 18/08/2026  | Fase 0.4b: clima automático por API según la zona del circuito (se quita el selector manual, se muestra el clima real al elegir el circuito y se añade thunder/storm a la detección) | Completada |
| 18/08/2026  | Fase 0.4c: etiqueta de clima simplificada (muestra solo `Clima: Lluvia`/`Seco` con "Consultando..." mientras carga) | Completada |
| 18/08/2026  | Fase 2: menú principal moderno con tarjetas F1, acentos de color por módulo y hover | Completada |
| 18/08/2026  | Fase 0.5: flag de interrupción restaurado; pendiente limpiar marcadores IA de comentarios | Completada |
| 19/08/2026  | Sesión A: `simularClasificacion` ya no incrementa el desgaste (usa `proyectarVuelta`) + assert en test | Completada |
| 19/08/2026  | Sesión A: validación de inyección por constructor con `Objects.requireNonNull` (10 clases) | Completada |
| 19/08/2026  | Sesión A: eliminados repositorios en memoria sin uso; javadoc de puertos apunta a los JSON | Completada |
| 19/08/2026  | Sesión A: `CarreraEnVivoTest` con 13 pruebas (invariantes + semilla fija `Random(42)`) | Completada |
| 19/08/2026  | Sesión A: `ObjectMapper` único reutilizable en `UtilJson` | Completada |
| 19/08/2026  | Paradas estratégicas: cada auto entra al pit-lane (deja de recorrer metros 28 s), sale con neumáticos nuevos y la UI lo muestra como "En pits" | Completada |

---

## 4. Tareas del Proyecto

### 4.1 Inventario de tareas

| Prioridad | Tarea | Estado | Dependencias |
|-----------|-------|--------|--------------|
| Alta      | Definir estructura de clases (Circuito, Piloto, Vehiculo) | Completada | - |
| Alta      | Simulación base: motor de vueltas, clima vía API y UI Swing | Completada | Estructura de clases |
| Alta      | Fase 0: correcciones de unidades, EDT, clima por carrera y barra de progreso | Completada | Simulación base |
| Media     | Fase 1: repositorios en memoria (HashMap) y puertos de salida | Completada | Simulación base |
| Media     | Fase 1: puertos de entrada y servicios CRUD (Circuito, Piloto, Vehiculo) | Completada | Repositorios |
| Baja      | Fase 2: interfaz de administración con navegación y CRUD | Completada | Servicios CRUD |
| Baja      | Fase 2: simulación con entidades seleccionables (no hardcodeadas) | Completada | Interfaz admin |
| Baja      | Fase 3: pruebas unitarias (JUnit) | En progreso | Servicios CRUD |
| Baja      | Fase 3: pruebas unitarias de `CarreraEnVivo` (ranking, DNF, paradas, resultado) | Completada | Carrera en vivo |
| Baja      | Fase 4: carrera en vivo con toda la parrilla (ranking, eventos y resultado) | Completada | Simulación + clasificación |
| Baja      | Fase 4: historial de carreras / persistencia de resultados | Pendiente | Carrera en vivo |
| Baja      | Fase 0.5: limpieza de marcadores IA en comentarios | Completada | - |
| Media     | Sesión A: deuda técnica (clasificación sin desgaste, `requireNonNull`, limpieza) | Completada | - |
| Media     | Paradas estratégicas: plan por auto en el ecuador de la carrera + parada de emergencia por neumáticos, con tiempo real en pit-lane | Completada | Carrera en vivo |
| Alta      | Mantener la sección de Punto de Control actualizada al cerrar cada sesión | Completada | - |

---

## 5. Punto de Control (Checkpoint)

> **Regla de trabajo:** al finalizar cada sesión (o cuando se agoten los tokens), se actualiza
> esta sección con el estado exacto del proyecto para poder retomarlo en la próxima sesión
> sin perder el hilo. Se registran: rama actual, trabajo hecho, pendientes y próximos pasos.

### Estado al cierre de la sesión 19/08/2026

| Campo | Valor |
|-------|-------|
| Rama actual | `main` (merges de `fix/clasificacion-sin-desgaste`, `refactor/constructor-require-non-null`, `refactor/eliminar-repositorios-en-memoria`, `test/carrera-en-vivo`, `refactor/util-json-objectmapper`, `feature/paradas-en-boxes`) |
| Último commit | `d728255` (merge `feature/paradas-en-boxes`); rama limpia, sin cambios sin commitear |
| Estado general | Compila con Maven (JDK 21) y 29 tests unitarios en verde |
| Ramas locales sin mergear | ninguna nueva; las de sesiones anteriores quedaron como histórico |

**Hecho en esta sesión:**
- Sesión A · deuda técnica:
  - `simularClasificacion` ya **no modifica el desgaste** de los vehículos (usa `proyectarVuelta`); test que lo garantiza.
  - Inyección por constructor migrada a `Objects.requireNonNull` (10 clases).
  - Eliminados los 3 repositorios en memoria (dead code); javadoc de puertos actualizado a los JSON.
  - `CarreraEnVivoTest`: pruebas sobre invariantes con semilla fija `Random(42)`.
  - `UtilJson` reutiliza una única instancia de `ObjectMapper`.
- Paradas en boxes («más simulación»):
  - Cada auto planifica 1 parada estratégica (2 si son 30+ vueltas) en una ventana alrededor del ecuador (0.4-0.7 de la carrera), con la misma semilla → reproducible.
  - Durante la parada (~28 s) el auto **deja de recorrer metros** en el pit-lane y sale con neumáticos nuevos (desgaste a 0). En la UI el estado muestra "En pits".
  - Si el neumático se destruye (>85% de desgaste) entra igualmente a boxes como parada de emergencia.
  - Eventos de entrada y salida de boxes en el área de eventos, coloreados por escudería.

**Pendiente / próximo paso sugerido (Sesión B):**
- Edición real en los CRUD (seleccionar fila → cargar formulario → actualizar) + métodos `actualizar(...)` en los puertos.
- Validaciones de negocio: piloto único por vehículo, sin duplicados de nombre/escudería.
- Confirmación al eliminar y búsqueda incremental en las tablas.

---

### ▶ Cómo retomar esta sesión (6 pasos)

1. **Preparar el entorno** (el `mvn` del sistema no está en PATH):
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
   export PATH="$JAVA_HOME/bin:$PATH"
   MVN=/usr/share/idea/plugins/maven/lib/maven3/bin/mvn
   ```
2. **Verificar que todo compila y pasa** (29 tests):
   ```bash
   cd /home/Papi_Leo/VSCODE/JAVA/proyecto_f1 && $MVN test
   ```
3. **Correr la app:** ejecutar `Main` (`com.proyectof1.Main`) desde el IDE; la persistencia vive en `data/*.json`.
4. **Convención git del repo:** cada tarea en su propia rama (`fix/…`, `refactor/…`, `test/…`, `feature/…`), un commit en estilo conventional con emoji, `$MVN test` en verde antes de mergear, y merge a `main` con `--no-ff`.
5. **Antes de tocar código:** `git switch main && git status` para confirmar rama limpia en `main`.
6. **Al cerrar cada sesión:** actualizar este SDD (historial → inventario de tareas → checkpoint con rama, último commit y siguientes pasos).
