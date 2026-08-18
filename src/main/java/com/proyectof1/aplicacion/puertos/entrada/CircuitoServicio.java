package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Circuito;

/**
 * Puerto de entrada para la gestión de circuitos.
 * Define las operaciones que la interfaz de usuario puede invocar.
 * La implementación concreta la proporciona CircuitoServicioImpl.
 */
public interface CircuitoServicio {

    /** Registra un nuevo circuito. */
    void registrar(String nombre, double kilometros, String ubicacion);

    /** Devuelve la lista de todos los circuitos registrados. */
    List<Circuito> listarCircuitos();

    /** Busca un circuito por su nombre. Devuelve null si no existe. */
    Circuito buscarPorNombre(String nombre);

    /** Busca todos los circuitos de una ubicación concreta. */
    List<Circuito> buscarPorUbicacion(String ubicacion);

    /** Elimina un circuito por nombre. Devuelve true si se eliminó. */
    boolean eliminar(String nombre);

}