package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Núcleo lógico del juego arcade de Fórmula 1 (capa de aplicación).
 *
 * <p>Representa un mini-juego jugable en el que el jugador conduce un coche
 * entre tres carriles esquivando obstáculos que caen hacia él. Mantiene todo
 * el estado del juego (carril del jugador, obstáculos, puntuación, dificultad
 * y fin de partida) y no depende de Swing, de modo que puede probarse con
 * JUnit como el resto de la capa de aplicación.</p>
 *
 * <p>La dificultad es progresiva: a medida que aumenta la puntuación, los
 * obstáculos descienden más rápido y aparecen con más frecuencia.</p>
 */
public class JuegoArcade {

    /** Cantidad de carriles de la pista (el coche se mueve entre ellos). */
    public static final int CANTIDAD_CARRILES = 3;

    /** Distancia recorrida por cada tick de avance del juego (paso base). */
    public static final int PASO_BASE = 12;

    /** Puntos que suman los obstáculos esquivados o la distancia recorrida. */
    private static final int PUNTOS_POR_PASO = 1;

    /** Cada cuántos puntos sube un nivel de dificultad. */
    private static final int PUNTOS_POR_NIVEL = 50;

    /** Velocidad máxima de caída de los obstáculos (en unidades de paso). */
    private static final int VELOCIDAD_MAXIMA = 40;

    /** Distancia a la que se genera un nuevo obstáculo (parte alta de la pista). */
    static final int ORIGEN_OBSTACULO = 800;

    // Generador de aleatoriedad: inyectable para reproducir los tests con semilla.
    private final Random aleatorio;

    // Carril actual del coche del jugador (0 = izquierda, 2 = derecha).
    private int carrilCoche;

    // Obstáculos presentes: cada uno es un par [carril, distancia].
    private final List<int[]> obstaculos;

    // Puntuación y record histórico (en memoria) del jugador.
    private int puntuacion;
    private int record;

    // Marca de fin de partida (choque con un obstáculo).
    private boolean gameOver;

    /**
     * Crea el juego con un generador aleatorio propio y el récord indicado
     * (normalmente cargado desde la persistencia).
     *
     * @param record Récord previo del jugador; se usa para resaltar si se supera.
     */
    public JuegoArcade(int record) {
        this(new Random(), record);
    }

    /**
     * Crea el juego con un generador y un récord concretos (usado en pruebas).
     *
     * @param aleatorio Fuente de aleatoriedad (permite semillas fijas en tests).
     * @param record    Récord previo cargado desde la persistencia.
     */
    public JuegoArcade(Random aleatorio, int record) {

        this.aleatorio = Objects.requireNonNull(aleatorio, "El generador no puede ser nulo.");
        this.record = Math.max(0, record);
        this.obstaculos = new ArrayList<>();
        this.carrilCoche = 1;
        this.puntuacion = 0;
        this.gameOver = false;

    }

    /**
     * Mueve el coche un carril a la izquierda, sin salirse de la pista.
     */
    public void cambiarCarrilIzquierda() {

        if (carrilCoche > 0) {
            carrilCoche--;
        }

    }

    /**
     * Mueve el coche un carril a la derecha, sin salirse de la pista.
     */
    public void cambiarCarrilDerecha() {

        if (carrilCoche < CANTIDAD_CARRILES - 1) {
            carrilCoche++;
        }

    }

    /**
     * Avanza un paso del juego: desplaza los obstáculos hacia el coche,
     * genera otros nuevos según la dificultad, suma puntos por distancia y
     * detecta colisiones. El paso real (velocidad de caída) depende del nivel
     * de dificultad actual.
     */
    public void avanzar() {

        if (gameOver) {
            return;
        }

        int velocidad = velocidadDeCaida();
        avanzarObstaculos(velocidad);
        generarObstaculos();

        puntuacion += PUNTOS_POR_PASO;
        if (puntuacion > record) {
            record = puntuacion;
        }

        detectarColision();

    }

    /** Desplaza cada obstáculo hacia abajo y elimina los que pasaron el coche. */
    private void avanzarObstaculos(int velocidad) {

        for (int i = obstaculos.size() - 1; i >= 0; i--) {

            int[] obstaculo = obstaculos.get(i);
            obstaculo[1] -= velocidad;

            // Si se salió por la parte baja (ya pasó el coche), se retira.
            if (obstaculo[1] <= 0) {
                obstaculos.remove(i);
            }

        }
    }

    /** Crea obstáculos nuevos en la parte alta con probabilidad según la dificultad. */
    private void generarObstaculos() {

        // Con más dificultad, más probable que aparezca un obstáculo por tick.
        int probabilidad = Math.min(70, 25 + nivelDeDificultad() * 5);

        if (aleatorio.nextInt(100) < probabilidad) {

            int carril = aleatorio.nextInt(CANTIDAD_CARRILES);
            obstaculos.add(new int[]{carril, ORIGEN_OBSTACULO});

        }
    }

    /** Marca la partida como perdida si el coche comparte carril con un obstáculo. */
    private void detectarColision() {

        for (int[] obstaculo : obstaculos) {

            if (obstaculo[0] == carrilCoche && obstaculo[1] <= 24) {
                gameOver = true;
                return;
            }

        }
    }

    /** Velocidad de caída (paso por tick) según la dificultad progresiva. */
    private int velocidadDeCaida() {

        return Math.min(PASO_BASE + nivelDeDificultad() * 2, VELOCIDAD_MAXIMA);

    }

    /** Nivel de dificultad actual: sube con la puntuación acumulada. */
    private int nivelDeDificultad() {

        return puntuacion / PUNTOS_POR_NIVEL;

    }

    /**
     * Reinicia la partida: limpia obstáculos, vuelve al carril central,
     * resetea la puntuación y desmarca el fin de juego. El récord se conserva.
     */
    public void reiniciar() {

        obstaculos.clear();
        carrilCoche = 1;
        puntuacion = 0;
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

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Devuelve una copia de los obstáculos activos como pares [carril, distancia].
     * Se devuelve una copia para que la vista no pueda mutar el estado interno.
     */
    public List<int[]> getObstaculos() {
        return new ArrayList<>(obstaculos);
    }

}