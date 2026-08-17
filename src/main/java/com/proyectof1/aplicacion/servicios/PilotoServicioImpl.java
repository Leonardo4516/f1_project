package com.proyectof1.aplicacion.servicios;

import java.util.List;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

public class PilotoServicioImpl implements PilotoServicio {

    private final PilotosRepositorio pilotosRepositorio;

    public PilotoServicioImpl(PilotosRepositorio pilotosRepositorio) {

        if (pilotosRepositorio != null) {

            this.pilotosRepositorio = pilotosRepositorio;

        } else {

            throw new IllegalArgumentException("El repositorio de pilotos no puede ser nulo.");

        }
    }

    @Override
    public void registrar(String nombre, int experiencia, int habilidadLluvia) {

        Piloto piloto = new Piloto(nombre, experiencia, habilidadLluvia);

        pilotosRepositorio.guardar(piloto);

    }

    @Override
    public List<Piloto> listarPilotos() {

        return pilotosRepositorio.listarTodos();

    }

    @Override
    public Piloto buscarPorNombre(String nombre) {

        return pilotosRepositorio.buscarPorNombre(nombre);

    }

    @Override
    public boolean eliminar(String nombre) {

        return pilotosRepositorio.eliminarPorNombre(nombre);

    }

}