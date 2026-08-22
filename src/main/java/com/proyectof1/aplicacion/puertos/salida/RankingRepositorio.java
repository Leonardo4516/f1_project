package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.EntradaRanking;

/**
 * Puerto de salida que define cómo se consultan y guardan las puntuaciones
 * de la clasificación del juego arcade. La capa de aplicación depende de
 * esta interfaz; la persistencia real la implementa {@code RankingRepositorioJDBC}.
 */
public interface RankingRepositorio {

    /** Guarda una entrada de puntuación con el nombre del jugador y la dificultad. */
    void guardar(String jugador, int puntuacion, String dificultad);

    /** Devuelve las 5 mejores puntuaciones globales, ordenadas de mayor a menor. */
    List<EntradaRanking> top5();

    /** Devuelve las 5 mejores puntuaciones de una dificultad concreta. */
    List<EntradaRanking> top5PorDificultad(String dificultad);
}
