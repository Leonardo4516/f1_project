package com.proyectof1.dominio;

/**
 * Entidad de dominio que representa un vehículo de Fórmula 1 (escudería).
 * Está asociado a un piloto y tiene características físicas que marcan su
 * perfil de rendimiento: aceleración (salida de curvas), frenado (zonas de
 * frenada) y agarre (curvas rápidas y condiciones de lluvia).
 */
public class Vehiculo {

    private String marcaEscuderia;
    private int velocidadMaxima;
    private int aceleracion;
    private int frenado;
    private int agarre;
    private Piloto piloto;

    /**
     * Constructor de Vehiculo con todos sus atributos físicos.
     *
     * @param marcaEscuderia Nombre de la escudería (ej. Ferrari).
     * @param velocidadMaxima Velocidad máxima en km/h.
     * @param aceleracion   Capacidad de aceleración (1-100).
     * @param frenado       Capacidad de frenada (1-100).
     * @param agarre        Agarre aerodinámico (1-100).
     * @param piloto        Piloto asignado al vehículo.
     */
    public Vehiculo(String marcaEscuderia, int velocidadMaxima, int aceleracion,
            int frenado, int agarre, Piloto piloto) {

        setMarcaEscuderia(marcaEscuderia);
        setVelocidadMaxima(velocidadMaxima);
        setAceleracion(aceleracion);
        setFrenado(frenado);
        setAgarre(agarre);
        setPiloto(piloto);

    }

    /**
     * Constructor conveniencia: un vehículo con perfil estándar (50/50/50).
     * Útil para tests y casos donde no interesa diferenciar los atributos.
     */
    public Vehiculo(String marcaEscuderia, int velocidadMaxima, Piloto piloto) {
        this(marcaEscuderia, velocidadMaxima, 50, 50, 50, piloto);
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

    public int getAceleracion() {
        return aceleracion;
    }

    /**
     * Establece la aceleración (1-100).
     */
    public final void setAceleracion(int aceleracion) {
        if (aceleracion >= 1 && aceleracion <= 100) {
            this.aceleracion = aceleracion;
        } else {
            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");
        }
    }

    public int getFrenado() {
        return frenado;
    }

    /**
     * Establece el frenado (1-100).
     */
    public final void setFrenado(int frenado) {
        if (frenado >= 1 && frenado <= 100) {
            this.frenado = frenado;
        } else {
            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");
        }
    }

    public int getAgarre() {
        return agarre;
    }

    /**
     * Establece el agarre (1-100).
     */
    public final void setAgarre(int agarre) {
        if (agarre >= 1 && agarre <= 100) {
            this.agarre = agarre;
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
        return marcaEscuderia + " | " + velocidadMaxima + " km/h"
                + " | Acel: " + aceleracion + " | Fren: " + frenado + " | Agarre: " + agarre
                + " | Piloto: " + piloto.getNombre();
    }

}