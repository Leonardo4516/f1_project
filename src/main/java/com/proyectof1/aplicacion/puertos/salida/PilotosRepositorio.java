package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Piloto;

/**
 * Puerto de salida que define cómo se guardan y consultan los pilotos.
 * La capa de aplicación depende de esta interfaz; la persistencia real
 * la implementa {@code PilotosRepositorioJDBC}.
 */
public interface PilotosRepositorio {

    /** Guarda (o actualiza) un piloto. */
    void guardar(Piloto piloto);

    /** Devuelve todos los pilotos almacenados. */
    List<Piloto> listarTodos();

    /** Busca un piloto por su nombre. Devuelve null si no existe. */
    Piloto buscarPorNombre(String nombre);

    /** Elimina un piloto por nombre. Devuelve true si se eliminó. */
    boolean eliminarPorNombre(String nombre);

}