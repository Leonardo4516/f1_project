package com.proyectof1.dominio;

public class Piloto {
    private String nombre;
    private int experiencia;
    private int habilidadLluvia;


    public Piloto(String nombre, int experiencia, int habilidadLluvia) {
        setNombre(nombre);
        setExperiencia(experiencia);
        setHabilidadLluvia(habilidadLluvia);
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


    public int getExperiencia() {
        return experiencia;
    }


    public final void setExperiencia(int experiencia) {
        if (experiencia >= 1 && experiencia <= 100) {

            this.experiencia = experiencia;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }


    public int getHabilidadLluvia() {
        return habilidadLluvia;
    }


    public final void setHabilidadLluvia(int habilidadLluvia) {
        if (habilidadLluvia >= 1 && habilidadLluvia <= 100) {

            this.habilidadLluvia = habilidadLluvia;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }    
}
