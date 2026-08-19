package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Vehiculo;

/**
 * Puerto de salida que define cómo se guardan y consultan los vehículos.
 * La capa de aplicación depende de esta interfaz; la persistencia real
 * (archivo JSON) la implementa VehiculosRepositorioJson.
 */
public interface VehiculosRepositorio {

    /** Guarda (o actualiza) un vehículo. */
    void guardar(Vehiculo vehiculo);

    /** Devuelve todos los vehículos almacenados. */
    List<Vehiculo> listarTodos();

    /** Busca un vehículo por escudería. Devuelve null si no existe. */
    Vehiculo buscarPorEscuderia(String marcaEscuderia);

    /** Elimina un vehículo por escudería. Devuelve true si se eliminó. */
    boolean eliminarPorEscuderia(String marcaEscuderia);

}