package com.proyectof1.dominio;

public class Circuito {

    private String nombre;
    private double kilometros;
    private String ubicacion;

    public Circuito(String nombre, double kilometros, String ubicacion) {
        setNombre(nombre);
        setKilometros(kilometros);
        setUbicacion(ubicacion);
    }

    public String getNombre() {
        return nombre;
    }

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

    public final void setUbicacion(String ubicacion) {
        if (!(ubicacion == null) && !ubicacion.isEmpty()) {

            this.ubicacion = ubicacion;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

}
