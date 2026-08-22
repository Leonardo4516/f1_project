package com.proyectof1.dominio;

import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una entrada en la tabla de clasificación
 * del juego arcade. Contiene el nombre del jugador, su puntuación, la
 * dificultad en la que jugó y la fecha del intento.
 */
public record EntradaRanking(
        String jugador,
        int puntuacion,
        String dificultad,
        LocalDateTime fecha
) {

    /**
     * Crea una entrada sin fecha (se asigna la actual).
     */
    public EntradaRanking(String jugador, int puntuacion, String dificultad) {
        this(jugador, puntuacion, dificultad, LocalDateTime.now());
    }
}
