package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Circuito;

public interface CircuitoServicio {

    void registrar(String nombre, double kilometros, String ubicacion);

    List<Circuito> listarCircuitos();

    Circuito buscarPorNombre(String nombre);

    List<Circuito> buscarPorUbicacion(String ubicacion);

    boolean eliminar(String nombre);

}