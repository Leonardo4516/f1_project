package com.proyectof1.dominio;

/**
 * Resultado individual de un participante al finalizar una carrera.
 * Para vehículos que abandonan (DNF) el tiempo total queda en 0 y el
 * estado lo indica claramente para que la interfaz lo represente bien.
 */
public record ResultadoParticipante(

        // Posición final (1 = ganador).
        int posicion,

        // Vehículo que participó.
        Vehiculo vehiculo,

        // "Finalizado" o "DNF".
        String estado,

        // Tiempo total acumulado (0 para abandonos).
        double tiempoTotal,

        // Mejor vuelta en segundos del participante.
        double mejorVuelta,

        // Vueltas completadas.
        int vueltas,

        // Paradas en boxes realizadas.
        int paradas,

        // true si este participante marcó la vuelta rápida de la carrera.
        boolean vueltaRapida) {

    @Override
    public String toString() {
        return posicion + ". " + vehiculo.getMarcaEscuderia() + " - " + vehiculo.getPiloto().getNombre()
                + " (" + estado + ")";
    }

}