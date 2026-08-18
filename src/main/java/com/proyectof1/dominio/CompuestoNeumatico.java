package com.proyectof1.dominio;

/**
 * Compuesto de neumáticos disponible en la simulación.
 * Cada compuesto tiene un perfil distinto:
 *  - Blando: máximo agarre (menor penalización de velocidad) pero mucho desgaste.
 *  - Medio: equilibrio entre rendimiento y durabilidad.
 *  - Duro: el más lento en términos de agarre pero con el desgaste más bajo.
 */
public enum CompuestoNeumatico {

    BLANDO("Blando (Soft)", 0.0, 4.0),
    MEDIO("Medio (Medium)", 1.2, 2.5),
    DURO("Duro (Hard)", 2.6, 1.5);

    // Nombre que se muestra en la interfaz.
    private final String etiqueta;

    // Penalización de velocidad en km/h frente al compuesto más rápido.
    private final double perdidaVelocidad;

    // Desgaste (en %) que se acumula al completar cada vuelta.
    private final double desgastePorVuelta;

    CompuestoNeumatico(String etiqueta, double perdidaVelocidad, double desgastePorVuelta) {

        this.etiqueta = etiqueta;
        this.perdidaVelocidad = perdidaVelocidad;
        this.desgastePorVuelta = desgastePorVuelta;

    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getPerdidaVelocidad() {
        return perdidaVelocidad;
    }

    public double getDesgastePorVuelta() {
        return desgastePorVuelta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

}