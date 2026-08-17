package com.proyectof1.aplicacion.servicios;

import java.util.List;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

public class CircuitoServicioImpl implements CircuitoServicio {

    private final CircuitosRepositorio circuitosRepositorio;

    public CircuitoServicioImpl(CircuitosRepositorio circuitosRepositorio) {

        if (circuitosRepositorio != null) {

            this.circuitosRepositorio = circuitosRepositorio;

        } else {

            throw new IllegalArgumentException("El repositorio de circuitos no puede ser nulo.");

        }
    }

    @Override
    public void registrar(String nombre, double kilometros, String ubicacion) {

        Circuito circuito = new Circuito(nombre, kilometros, ubicacion);

        circuitosRepositorio.guardar(circuito);

    }

    @Override
    public List<Circuito> listarCircuitos() {

        return circuitosRepositorio.listarTodos();

    }

    @Override
    public Circuito buscarPorNombre(String nombre) {

        return circuitosRepositorio.buscarPorNombre(nombre);

    }

    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        return circuitosRepositorio.buscarPorUbicacion(ubicacion);

    }

    @Override
    public boolean eliminar(String nombre) {

        return circuitosRepositorio.eliminarPorNombre(nombre);

    }

}