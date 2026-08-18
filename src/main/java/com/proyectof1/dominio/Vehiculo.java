package com.proyectof1.dominio;

/**
 * Entidad de dominio que representa un vehículo de Fórmula 1 (escudería).
 * Está asociado a un piloto y tiene características físicas y de desgaste.
 */
public class Vehiculo {

    private String marcaEscuderia;
    private int velocidadMaxima;
    private double desgasteNeumaticos;
    private Piloto piloto;

    /**
     * Constructor de Vehiculo. Valida y asigna todos sus atributos.
     *
     * @param marcaEscuderia     Nombre de la escudería (ej. Ferrari).
     * @param velocidadMaxima    Velocidad máxima en km/h.
     * @param desgasteNeumaticos Desgaste actual de los neumáticos (0-100%).
     * @param piloto             Piloto asignado al vehículo.
     */
    public Vehiculo(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, Piloto piloto) {

        setMarcaEscuderia(marcaEscuderia);
        setVelocidadMaxima(velocidadMaxima);
        setDesgasteNeumaticos(desgasteNeumaticos);
        setPiloto(piloto);

    }

    public String getMarcaEscuderia() {
        return marcaEscuderia;
    }

    /**
     * Establece la escudería solo si no es nula ni vacía.
     */
    public final void setMarcaEscuderia(String marcaEscuderia) {

        if (!(marcaEscuderia == null) && !marcaEscuderia.isEmpty()) {

            this.marcaEscuderia = marcaEscuderia;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }

    /**
     * Establece la velocidad máxima solo si es mayor que 0.
     */
    public final void setVelocidadMaxima(int velocidadMaxima) {
        if (velocidadMaxima > 0) {

            this.velocidadMaxima = velocidadMaxima;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public double getDesgasteNeumaticos() {
        return desgasteNeumaticos;
    }

    /**
     * Establece el desgaste de neumáticos solo si está entre 0% y 100%.
     */
    public final void setDesgasteNeumaticos(double desgasteNeumaticos) {
        if (desgasteNeumaticos >= 0.0 && desgasteNeumaticos <= 100.0) {

            this.desgasteNeumaticos = desgasteNeumaticos;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public Piloto getPiloto() {
        return piloto;
    }

    /**
     * Asigna un piloto al vehículo. No se permite un piloto nulo.
     */
    public final void setPiloto(Piloto piloto) {
        if (piloto != null) {

            this.piloto = piloto;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    @Override
    public String toString() {

        // Representación textual del vehículo para mostrarlo en las listas.
        return marcaEscuderia + " | " + velocidadMaxima + " km/h | Piloto: " + piloto.getNombre();

    }

}
