package com.proyectof1.dominio;

import java.util.List;

/**
 * Resultado completo de una carrera: clasificación final ordenada,
 * circuito, clima y total de vueltas.
 */
public record ResultadoCarrera(

        // Clasificación final de todos los participantes (posición 1 = ganador).
        List<ResultadoParticipante> participantes,

        // Circuito donde se corrió.
        Circuito circuito,

        // Clima efectivo durante la carrera ("Lluvia" o "Seco").
        String clima,

        // Total de vueltas de la carrera.
        int totalVueltas) {

    /** Devuelve el ganador de la carrera (primer participante). */
    public ResultadoParticipante ganador() {

        return participantes.isEmpty() ? null : participantes.get(0);

    }

    /** Devuelve al autor de la vuelta rápida, o null si nadie la marcó. */
    public ResultadoParticipante autorVueltaRapida() {

        for (ResultadoParticipante participante : participantes) {

            if (participante.vueltaRapida()) {

                return participante;

            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "Resultado " + circuito.getNombre() + " -> " + ganador();
    }

}