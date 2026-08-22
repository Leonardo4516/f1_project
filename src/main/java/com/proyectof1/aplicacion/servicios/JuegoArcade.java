package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Núcleo lógico del juego arcade de Fórmula 1 (capa de aplicación).
 *
 * <p>Representa un mini-juego jugable de conducción entre carriles: el jugador
 * mueve su coche a izquierda y derecha para esquivar obstáculos que caen desde
 * arriba de la pantalla. La pista se modela en unidades lógicas verticales que
 * crecen hacia abajo: los obstáculos nacen arriba ({@code y} pequeña) y caen
 * hacia el jugador ({@code y} creciente) hasta llegar a su altura.</p>
 *
 * <p>La dificultad es progresiva (los obstáculos caen más rápido y aparecen con
 * más frecuencia según los puntos) pero siempre se garantiza al menos un carril
 * libre para poder esquivar. El jugador dispone de varias vidas; pierde una al
 * chocar y la partida termina al agotarlas.</p>
 *
 * <p>No depende de Swing y usa un {@link Random} inyectable, de modo que puede
 * probarse con JUnit de forma determinista (igual que el resto de la capa de
 * aplicación).</p>
 */
public class JuegoArcade {

    /** Número de carriles por los que se mueve el coche. */
    public static final int CANTIDAD_CARRILES = 4;

    /** Alto lógico del circuito en unidades verticales (0 = arriba). */
    public static final double LARGO_PISTA = 700.0;

    /** Borde superior del coche del jugador en unidades lógicas. */
    public static final double PARTE_SUPERIOR_COCHE = 620.0;

    /** Alto del coche del jugador en unidades lógicas. */
    public static final double ALTO_COCHE = 60.0;

    /** Alto de cada obstáculo en unidades lógicas. */
    public static final double ALTO_OBSTACULO = 46.0;

    /** Vidas con las que arranca cada partida. */
    public static final int VIDAS_INICIALES = 3;

    // --- Dificultad configurable (afecta a la velocidad base y la frecuencia) ---
    public enum Dificultad {
        FACIL("Fácil", 5.0, 0.6),
        NORMAL("Normal", 7.0, 1.0),
        DIFICIL("Difícil", 9.0, 1.35),
        CLASIFICACION("Clasificación", 8.0, 1.15);

        private final String etiqueta;
        private final double velocidadBase;
        private final double multiplicadorSpawn;

        Dificultad(String etiqueta, double velocidadBase, double multiplicadorSpawn) {
            this.etiqueta = etiqueta;
            this.velocidadBase = velocidadBase;
            this.multiplicadorSpawn = multiplicadorSpawn;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        double velocidadBase() {
            return velocidadBase;
        }

        double multiplicadorSpawn() {
            return multiplicadorSpawn;
        }
    }

    // Parámetros de la dificultad progresiva.
    private static final double INCREMENTO_VELOCIDAD = 0.5;
    private static final double VELOCIDAD_MAXIMA = 20.0;
    private static final int PUNTOS_POR_NIVEL = 150;

    // Franja (en unidades) donde un obstáculo se considera "recién aparecido".
    private static final double FRANJA_APARICION = 90.0;

    // Distancia vertical mínima entre obstáculos en el mismo carril.
    // Se mide desde la parte superior del nuevo (que nace entre -80 y 0)
    // hasta la parte superior del anterior. Con un valor de 160, el peor
    // caso (nuevo en y=0) da una brecha de 160−46=114 unidades.
    private static final double SEPARACION_MINIMA_OBSTACULOS = 160.0;

    // Margen vertical para evitar que obstáculos en carriles adyacentes
    // aparezcan demasiado cerca entre sí y formen un muro imposible de esquivar.
    private static final double MARGEN_ADYACENCIA = 50.0;

    // Ticks de inmunidad tras perder una vida (evita perder varias seguidas).
    private static final int TICKS_INMUNIDAD = 8;

    // Generador aleatorio inyectable (semilla fija en pruebas).
    private final Random aleatorio;

    // Carril actual del coche (0 = izquierda, CANTIDAD_CARRILES-1 = derecha).
    private int carrilCoche;

    // Obstáculos presentes en pista.
    private final List<Obstaculo> obstaculos;

    // Dificultad elegida por el jugador.
    private final Dificultad dificultad;

    // Puntuación y récord (en memoria) del jugador.
    private int puntuacion;
    private int record;

    // Vidas restantes.
    private int vidas;

    // Ticks restantes de inmunidad tras un impacto.
    private int ticksInmunidad;

    // Marca de fin de partida (vidas agotadas).
    private boolean gameOver;

    /**
     * Crea el juego con dificultad normal, un generador propio y el récord dado.
     *
     * @param record Récord previo cargado desde la persistencia.
     */
    public JuegoArcade(int record) {
        this(new Random(), record, Dificultad.NORMAL);
    }

    /**
     * Crea el juego con generador, récord y dificultad concretos.
     *
     * @param aleatorio   Fuente de aleatoriedad (permite semillas fijas en tests).
     * @param record      Récord previo cargado desde la persistencia.
     * @param dificultad  Nivel de dificultad elegido.
     */
    public JuegoArcade(Random aleatorio, int record, Dificultad dificultad) {

        this.aleatorio = Objects.requireNonNull(aleatorio, "El generador no puede ser nulo.");
        this.record = Math.max(0, record);
        this.dificultad = Objects.requireNonNull(dificultad, "La dificultad no puede ser nula.");
        this.obstaculos = new ArrayList<>();
        this.carrilCoche = CANTIDAD_CARRILES / 2;
        this.puntuacion = 0;
        this.vidas = VIDAS_INICIALES;
        this.ticksInmunidad = 0;
        this.gameOver = false;

    }

    /** Mueve el coche un carril a la izquierda, sin salirse de la pista. */
    public void cambiarCarrilIzquierda() {
        if (carrilCoche > 0) {
            carrilCoche--;
        }
    }

    /** Mueve el coche un carril a la derecha, sin salirse de la pista. */
    public void cambiarCarrilDerecha() {
        if (carrilCoche < CANTIDAD_CARRILES - 1) {
            carrilCoche++;
        }
    }

    /**
     * Avanza un paso del juego: baja los obstáculos, crea otros nuevos según la
     * dificultad, suma puntos y comprueba colisiones. Si recibe un golpe, resta
     * una vida y otorga una breve inmunidad.
     */
    public void avanzar() {

        if (gameOver) {
            return;
        }

        if (ticksInmunidad > 0) {
            ticksInmunidad--;
        }

        double velocidad = velocidadActual();

        for (int i = obstaculos.size() - 1; i >= 0; i--) {

            Obstaculo obstaculo = obstaculos.get(i);
            obstaculo.y += velocidad;

            // Si se salió por abajo (ya pasó al jugador) se retira.
            if (obstaculo.y > LARGO_PISTA + ALTO_OBSTACULO) {
                obstaculos.remove(i);
            }
        }

        generarObstaculos();

        puntuacion++;
        if (puntuacion > record) {
            record = puntuacion;
        }

        detectarColisiones();
    }

    /** Crea obstáculos nuevos en la parte alta, sin llenar todos los carriles. */
    private void generarObstaculos() {

        // Nunca se llenan todos los carriles a la vez: siempre queda uno libre.
        if (ocupadosEnFranja() >= CANTIDAD_CARRILES - 1) {
            return;
        }

        int probabilidad = (int) Math.min(45, 15 + nivelDeDificultad() * 4 * dificultad.multiplicadorSpawn());

        if (aleatorio.nextInt(100) < probabilidad) {

            // Se recopilan los carriles que cumplen ambas condiciones:
            // sin obstáculo propio demasiado cerca y sin adyacente conflictivo.
            List<Integer> carrilesSeguros = new ArrayList<>();

            for (int i = 0; i < CANTIDAD_CARRILES; i++) {

                boolean ocupado = false;
                for (Obstaculo o : obstaculos) {
                    if (o.carril == i && o.y < FRANJA_APARICION) {
                        ocupado = true;
                        break;
                    }
                }

                if (!ocupado && !existeObstaculoCercano(i)
                        && !hayAdyacenteDemasiadoCercano(i)) {
                    carrilesSeguros.add(i);
                }
            }

            if (carrilesSeguros.isEmpty()) {
                return;
            }

            int carril = carrilesSeguros.get(aleatorio.nextInt(carrilesSeguros.size()));
            double y = -aleatorio.nextDouble() * 80.0;
            obstaculos.add(new Obstaculo(carril, y));
        }
    }

    /** Cuenta cuántos carriles tienen ya un obstáculo en la franja de aparición. */
    private int ocupadosEnFranja() {

        int ocupados = 0;
        boolean[] carrilOcupado = new boolean[CANTIDAD_CARRILES];

        for (Obstaculo obstaculo : obstaculos) {
            if (obstaculo.y < FRANJA_APARICION && !carrilOcupado[obstaculo.carril]) {
                carrilOcupado[obstaculo.carril] = true;
                ocupados++;
            }
        }

        return ocupados;
    }

    /** Elige un carril sin obstáculos en la franja de aparición, si existe. */
    private int elegirCarrilLibre() {

        List<Integer> libres = new ArrayList<>();

        for (int i = 0; i < CANTIDAD_CARRILES; i++) {

            boolean ocupado = false;
            for (Obstaculo obstaculo : obstaculos) {
                if (obstaculo.carril == i && obstaculo.y < FRANJA_APARICION) {
                    ocupado = true;
                    break;
                }
            }

            if (!ocupado) {
                libres.add(i);
            }
        }

        if (libres.isEmpty()) {
            return aleatorio.nextInt(CANTIDAD_CARRILES);
        }

        return libres.get(aleatorio.nextInt(libres.size()));
    }

    /**
     * Comprueba si en el carril dado hay algún obstáculo cuya parte superior
     * todavía no descendió lo suficiente (está por encima del umbral de
     * separación). Si es así, no se debe generar otro hasta que baje más.
     */
    private boolean existeObstaculoCercano(int carril) {

        for (Obstaculo obstaculo : obstaculos) {
            if (obstaculo.carril == carril
                    && obstaculo.y < SEPARACION_MINIMA_OBSTACULOS) {
                return true;
            }
        }

        return false;
    }

    /**
     * Comprueba si en los carriles adyacentes (izq y der) hay algún obstáculo
     * que esté demasiado cerca de la zona de aparición (y entre -80 y 0).
     * Si es así, el nuevo obstáculo formaría un muro horizontal imposible
     * de esquivar.
     */
    private boolean hayAdyacenteDemasiadoCercano(int carril) {

        double yMinNuevo = -80.0;
        double yMaxNuevo = ALTO_OBSTACULO;

        for (Obstaculo obstaculo : obstaculos) {

            if (obstaculo.carril == carril - 1 || obstaculo.carril == carril + 1) {

                double parteInferior = obstaculo.y + ALTO_OBSTACULO;

                if (parteInferior > yMinNuevo - MARGEN_ADYACENCIA
                        && obstaculo.y < yMaxNuevo + MARGEN_ADYACENCIA) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Comprueba si el coche choca con algún obstáculo y aplica el golpe. */
    private void detectarColisiones() {

        if (ticksInmunidad > 0) {
            return;
        }

        for (int i = obstaculos.size() - 1; i >= 0; i--) {

            Obstaculo obstaculo = obstaculos.get(i);

            if (chocaConCoche(obstaculo)) {

                obstaculos.remove(i);
                recibirGolpe();
                return;

            }
        }
    }

    /** ¿El obstáculo solapa la caja del coche (mismo carril y franja vertical)? */
    private boolean chocaConCoche(Obstaculo obstaculo) {

        if (obstaculo.carril != carrilCoche) {
            return false;
        }

        double parteSuperiorObstaculo = obstaculo.y;
        double parteInferiorObstaculo = obstaculo.y + ALTO_OBSTACULO;

        return parteInferiorObstaculo > PARTE_SUPERIOR_COCHE
                && parteSuperiorObstaculo < PARTE_SUPERIOR_COCHE + ALTO_COCHE;
    }

    /** Resta una vida; si se acaban, marca el fin de la partida. */
    private void recibirGolpe() {

        vidas--;
        ticksInmunidad = TICKS_INMUNIDAD;

        if (vidas <= 0) {
            gameOver = true;
        }
    }

    /** Velocidad de caída (unidades por tick) según dificultad y puntos. */
    double velocidadActual() {
        double velocidad = dificultad.velocidadBase() + nivelDeDificultad() * INCREMENTO_VELOCIDAD;
        return Math.min(velocidad, VELOCIDAD_MAXIMA);
    }

    /** Nivel de dificultad progresiva según los puntos acumulados. */
    private int nivelDeDificultad() {
        return puntuacion / PUNTOS_POR_NIVEL;
    }

    /**
     * Reinicia la partida: limpia obstáculos, vuelve al carril central, restablece
     * las vidas y los puntos. El récord se conserva.
     */
    public void reiniciar() {

        obstaculos.clear();
        carrilCoche = CANTIDAD_CARRILES / 2;
        puntuacion = 0;
        vidas = VIDAS_INICIALES;
        ticksInmunidad = 0;
        gameOver = false;

    }

    public int getCarrilCoche() {
        return carrilCoche;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getRecord() {
        return record;
    }

    public int getVidas() {
        return vidas;
    }

    public int getNivel() {
        return nivelDeDificultad();
    }

    public double getVelocidad() {
        return velocidadActual();
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean estaInmune() {
        return ticksInmunidad > 0;
    }

    /** Devuelve una copia de los obstáculos activos para dibujarlos. */
    public List<Obstaculo> getObstaculos() {
        return new ArrayList<>(obstaculos);
    }

    /**
     * Obstáculo en pista: carril donde está y su posición vertical lógica.
     * Es inmutable desde fuera; la vista solo lo lee para dibujarlo.
     */
    public static class Obstaculo {

        final int carril;
        double y;

        Obstaculo(int carril, double y) {
            this.carril = carril;
            this.y = y;
        }

        public int getCarril() {
            return carril;
        }

        public double getY() {
            return y;
        }
    }

}