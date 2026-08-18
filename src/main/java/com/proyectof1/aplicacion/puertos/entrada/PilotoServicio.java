package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Piloto;

/**
 * Puerto de entrada para la gestión de pilotos.
 * Define las operaciones que la interfaz de usuario puede invocar.
 * La implementación concreta la proporciona PilotoServicioImpl.
 */
public interface PilotoServicio {

    /** Registra un nuevo piloto con sus datos de habilidad. */
    void registrar(String nombre, int experiencia, int habilidadLluvia);

    /** Devuelve la lista de todos los pilotos registrados. */
    List<Piloto> listarPilotos();

    /** Busca un piloto por su nombre. Devuelve null si no existe. */
    Piloto buscarPorNombre(String nombre);

    /** Elimina un piloto por nombre. Devuelve true si se eliminó. */
    boolean eliminar(String nombre);

}