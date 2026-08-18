package com.proyectof1.dominio;

/**
 * Entidad de dominio que representa un circuito de Fórmula 1.
 * Guarda el nombre, la longitud en kilómetros y su ubicación (país/ciudad).
 */
public class Circuito {

    private String nombre;
    private double kilometros;
    private String ubicacion;

    /**
     * Constructor de Circuito. Valida y asigna todos sus atributos.
     *
     * @param nombre     Nombre del circuito.
     * @param kilometros Longitud de la vuelta en kilómetros (mayor que 0).
     * @param ubicacion  Ubicación geográfica del circuito.
     */
    public Circuito(String nombre, double kilometros, String ubicacion) {
        setNombre(nombre);
        setKilometros(kilometros);
        setUbicacion(ubicacion);
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

    @Override
    public String toString() {

        // Representación textual del circuito para mostrarlo en las listas.
        return nombre + " | " + kilometros + " km | " + ubicacion;

    }

}
