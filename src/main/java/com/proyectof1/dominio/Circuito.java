package com.proyectof1.dominio;

/**
 * Entidad de dominio que representa un circuito de Fórmula 1.
 * Guarda el nombre, la longitud en kilómetros, su ubicación y
 * características técnicas: curvas, tipo de circuito, vueltas típicas y récord.
 */
public class Circuito {

    private String nombre;
    private double kilometros;
    private String ubicacion;
    private int numCurvas;
    private String tipoCircuito;
    private int vueltasTipicas;
    private String recordVuelta;

    /**
     * Constructor completo con todas las características del circuito.
     *
     * @param nombre        Nombre del circuito.
     * @param kilometros    Longitud de la vuelta en kilómetros (mayor que 0).
     * @param ubicacion     Ubicación geográfica del circuito (país/ciudad).
     * @param numCurvas     Número de curvas del circuito.
     * @param tipoCircuito  Tipo de circuito (Permanente, Urbano, Semiacotico).
     * @param vueltasTipicas Número típico de vueltas en una carrera.
     * @param recordVuelta  Récord de vuelta rápida (ej. "1:21.046 - Rubens Barrichello, 2004").
     */
    public Circuito(String nombre, double kilometros, String ubicacion,
            int numCurvas, String tipoCircuito, int vueltasTipicas, String recordVuelta) {
        setNombre(nombre);
        setKilometros(kilometros);
        setUbicacion(ubicacion);
        this.numCurvas = numCurvas;
        this.tipoCircuito = tipoCircuito;
        this.vueltasTipicas = vueltasTipicas;
        this.recordVuelta = recordVuelta;
    }

    /**
     * Constructor de conveniencia: solo nombre, km y ubicación.
     * Útil para tests y datos legados.
     */
    public Circuito(String nombre, double kilometros, String ubicacion) {
        this(nombre, kilometros, ubicacion, 0, "", 0, "");
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre solo si no es nulo ni vacío.
     */
    public final void setNombre(String nombre) {

        if (!(nombre == null) && !nombre.isEmpty()) {

            this.nombre = nombre;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public double getKilometros() {
        return kilometros;
    }

    /**
     * Establece los kilómetros solo si son mayores que 0.
     */
    public final void setKilometros(double kilometros) {
        if (kilometros > 0) {

            this.kilometros = kilometros;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public String getUbicacion() {
        return ubicacion;
    }

    /**
     * Establece la ubicación solo si no es nula ni vacía.
     */
    public final void setUbicacion(String ubicacion) {
        if (!(ubicacion == null) && !ubicacion.isEmpty()) {

            this.ubicacion = ubicacion;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public int getNumCurvas() {
        return numCurvas;
    }

    public void setNumCurvas(int numCurvas) {
        this.numCurvas = numCurvas;
    }

    public String getTipoCircuito() {
        return tipoCircuito;
    }

    public void setTipoCircuito(String tipoCircuito) {
        this.tipoCircuito = tipoCircuito;
    }

    public int getVueltasTipicas() {
        return vueltasTipicas;
    }

    public void setVueltasTipicas(int vueltasTipicas) {
        this.vueltasTipicas = vueltasTipicas;
    }

    public String getRecordVuelta() {
        return recordVuelta;
    }

    public void setRecordVuelta(String recordVuelta) {
        this.recordVuelta = recordVuelta;
    }

    @Override
    public String toString() {

        // Representación textual del circuito para mostrarlo en las listas.
        return nombre + " | " + kilometros + " km | " + ubicacion;

    }

}
