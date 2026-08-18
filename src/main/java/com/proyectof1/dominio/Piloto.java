package com.proyectof1.dominio;

/**
 * Entidad de dominio que representa a un piloto de Fórmula 1.
 * Contiene sus datos personales y de habilidad.
 */
public class Piloto {

    private String nombre;
    private int experiencia;
    private int habilidadLluvia;

    /**
     * Constructor de Piloto. Valida y asigna los atributos.
     *
     * @param nombre          Nombre del piloto.
     * @param experiencia     Nivel de experiencia (1-100).
     * @param habilidadLluvia Habilidad para conducir bajo la lluvia (1-100).
     */
    public Piloto(String nombre, int experiencia, int habilidadLluvia) {
        setNombre(nombre);
        setExperiencia(experiencia);
        setHabilidadLluvia(habilidadLluvia);
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre solo si es válido (no nulo ni vacío).
     * En caso contrario lanza una excepción.
     */
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

    /**
     * Establece la experiencia solo si está en el rango 1-100.
     */
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

    /**
     * Establece la habilidad bajo la lluvia solo si está en el rango 1-100.
     */
    public final void setHabilidadLluvia(int habilidadLluvia) {
        if (habilidadLluvia >= 1 && habilidadLluvia <= 100) {

            this.habilidadLluvia = habilidadLluvia;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }
}
