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
| 21/08/2026  | Juego arcade jugable: mini-juego de carriles con control por teclado, dificultad progresiva, escudería elegible y récord en JSON | Completada |
| 21/08/2026  | Fix jugabilidad del arcade: geometría vertical corregida, hitbox real, teclado con bindings, vidas, pausa, dificultad seleccionable y carril libre garantizado | Completada |
| 22/08/2026  | Mejoras arcade: centrado de pista, separación mínima por carril (160 u), filtro de adyacencia (50 u), coche reducido (44×60 px) | Completada |
| 22/08/2026  | Migración de persistencia JSON a PostgreSQL (JDBC driver 42.7.4) | Completada |
| 22/08/2026  | Repositorios JDBC: Circuitos, Pilotos, Vehículos y Ranking contra PostgreSQL | Completada |
| 22/08/2026  | Nuevo modo Clasificación: velocidad=8.0, spawn=1.15, prompt de nombre, top 5 visible | Completada |
| 22/08/2026  | Modelo de dominio EntradaRanking + interfaz RankingRepositorio + RankingRepositorioJDBC | Completada |

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
| Alta      | Juego arcade jugable: núcleo `JuegoArcade` testable, `VentanaArcade` con control por teclado, dificultad progresiva, escudería elegible y récord en `data/record.json` | Completada | - |
| Alta      | Fix jugabilidad del arcade: geometría, hitbox, teclado (bindings), vidas, pausa, dificultad seleccionable y carril libre garantizado | Completada | Juego arcade |
| Alta      | Mejoras arcade: centrado, separación por carril, adyacencia, coche más pequeño | Completada | Fix jugabilidad |
| Alta      | Migración JSON → PostgreSQL (JDBC, esquema, repositorios) | Completada | - |
| Alta      | Modo Clasificación: prompt nombre, top 5, panel lateral en UI | Completada | PostgreSQL |
| Alta      | Mantener la sección de Punto de Control actualizada al cerrar cada sesión | Completada | - |
| Alta      | Varianza en vueltas (±0.3s) para inconsistencia humana | Completada | Carrera en vivo |
| Alta      | Paradas en boxes con duración variable (normal μ=25σ=2 + 5% error) | Completada | Varianza |
| Alta      | Cambio de neumático en pits según vueltas restantes (>15=Duro, >8=Medio, sino=Blando) | Completada | Paradas |
| Alta      | Safety Car: 40% probabilidad tras DNF, reduce velocidad 80 km/h, 3-5 vueltas | Completada | Carrera en vivo |
| Media     | Control de velocidad (1x/2x/4x) y pausa/reanudar en UI de simulación | Completada | UI simulación |
| Media     | Telemetría por auto: velocidad, desgaste, compuesto, paradas y última vuelta | Completada | UI simulación |

---

## 5. Punto de Control (Checkpoint)

> **Regla de trabajo:** al finalizar cada sesión (o cuando se agoten los tokens), se actualiza
> esta sección con el estado exacto del proyecto para poder retomarlo en la próxima sesión
> sin perder el hilo. Se registran: rama actual, trabajo hecho, pendientes y próximos pasos.

### Estado al cierre de la sesión 22/08/2026

| Campo | Valor |
|-------|-------|
| Rama actual | `main` (merge de 6 ramas feature/sim-* sobre el punto de control anterior) |
| Último commit | `ff9976b` (merge telemetría); rama limpia |
| Estado general | Compila con Maven (JDK 21) y 39 tests unitarios en verde |
| Ramas locales sin mergear | ninguna nueva |

**Hecho en esta sesión (22/08/2026) — Mejoras a la simulación:**

*1. Varianza en vueltas (feature/sim-varianza):*
- Añadido `VARIANZA_VUELTA = 0.3` (±0.3 segundos) en `CarreraEnVivo.java`.
- Cada auto recibe una perturbación aleatoria en su tiempo de vuelta simulando inconsistencia humana.
- Test: `autosIdenticosTienenDistintasMejoresVueltas`.

*2. Paradas aleatorias (feature/sim-pits-random):*
- Duración de parada con distribución normal (μ=25s, σ=2s) + 5% de probabilidad de error (35-45s).
- Eventos de parada ahora muestran la duración: `· 24.0 s · Medio (Medium)`.
- Fix: `String.format` con `Locale.US` para evitar separadores regionales.
- Tests: `paradasEnBoxesTienenDuracionesVariables`, `duracionParadaDentroDeRangoRazonable`.

*3. Cambio de neumático en pits (feature/sim-cambio-compuesto):*
- Campo `compuesto` ahora es mutable en `AutoEnCarrera`.
- Método `elegirCompuestoOptimo(vueltasRestantes)`: >15=Duro, >8=Medio, ≤8=Blando.
- Se muestra el compuesto al que cambia en el log de paradas.
- Test: `compuestoCambiaSegunVueltasRestantes`.

*4. Safety Car (feature/sim-safety-car):*
- 40% de probabilidad de activar SC tras un DNF (si no hay uno activo).
- SC dura 3-5 vueltas y limita la velocidad a 80 km/h.
- El pelotón se agrupa: gaps se reducen significativamente.
- Eventos: `⚠️ SAFETY CAR desplegado` y `✅ Safety car retirado`.
- Tests: `safetyCarSeActivaTrasAbandono`, `safetyCarReduceLaVelocidadDelPeloton`.

*5. Control de velocidad y pausa (feature/sim-controles):*
- Panel de controles debajo de la barra de progreso: ⏸ Pausa/▶ Reanudar + botones 1x/2x/4x.
- La velocidad multiplicada afecta el paso simulado: `PASO_SIMULADO * multiplicadorVelocidad`.
- Los controles se habilitan durante la carrera y se desactivan al finalizar.

*6. Telemetría por auto (feature/sim-telemetria):*
- Panel inferior en la tabla de clasificación que muestra datos del auto seleccionado.
- Velocidad actual (km/h), desgaste de neumáticos (%), compuesto montado, número de paradas, última vuelta (s).
- Se actualiza en cada tick de la carrera.

**Pendiente / próximo paso sugerido (Sesión siguiente):**
- Tests JDBC (requieren DB de prueba o mock).
- Retomar los pendientes previos: edición real en los CRUD, validaciones de negocio, confirmación al eliminar y búsqueda incremental.
- Posibles mejoras: climas dinámicos (lluvia a mitad de carrera), neumáticos intermedios/de lluvia.

---

### ▶ Cómo retomar esta sesión (6 pasos)

1. **Preparar el entorno** (el `mvn` del sistema no está en PATH):
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
   export PATH="$JAVA_HOME/bin:$PATH"
   MVN=/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn
   ```
2. **Verificar PostgreSQL** (debe estar corriendo):
   ```bash
   systemctl status postgresql
   # Si no está corriendo:
   sudo systemctl start postgresql
   ```
3. **Verificar que todo compila y pasa** (33 tests):
   ```bash
   cd /home/Papi_Leo/VSCODE/JAVA/proyecto_f1 && $MVN test
   ```
4. **Correr la app:** ejecutar `Main` (`com.proyectof1.Main`) desde el IDE; la persistencia vive en PostgreSQL (`proyecto_f1`).
5. **Convención git del repo:** cada tarea en su propia rama (`fix/…`, `refactor/…`, `test/…`, `feature/…`), un commit en estilo conventional con emoji, `$MVN test` en verde antes de mergear, y merge a `main` con `--no-ff`.
6. **Al cerrar cada sesión:** actualizar este SDD (historial → inventario de tareas → checkpoint con rama, último commit y siguientes pasos).
