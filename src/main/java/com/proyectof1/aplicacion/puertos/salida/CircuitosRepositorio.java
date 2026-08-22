package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Circuito;

/**
 * Puerto de salida que define cómo se guardan y consultan los circuitos.
 * La capa de aplicación depende de esta interfaz; la persistencia real
 * la implementa {@code CircuitosRepositorioJDBC}.
 */
public interface CircuitosRepositorio {

    /** Guarda (o actualiza) un circuito. */
    void guardar(Circuito circuito);

    /** Devuelve todos los circuitos almacenados. */
    List<Circuito> listarTodos();

    /** Busca un circuito por su nombre. Devuelve null si no existe. */
    Circuito buscarPorNombre(String nombre);

    /** Busca todos los circuitos de una ubicación concreta. */
    List<Circuito> buscarPorUbicacion(String ubicacion);

    /** Elimina un circuito por nombre. Devuelve true si se eliminó. */
    boolean eliminarPorNombre(String nombre);

}