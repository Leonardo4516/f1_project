package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Circuito;

public interface CircuitosRepositorio {

    void guardar(Circuito circuito);

    List<Circuito> listarTodos(String nombre);

    Circuito buscarPorNombre(String nombre);

    List<Circuito> buscarPorUbicacion(String ubicacion);

    boolean eliminarPorNombre(String nombre);
    
}
