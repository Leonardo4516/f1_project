package com.proyectof1.aplicacion.servicios;

import java.util.List;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

/**
 * Implementación del puerto de entrada PilotoServicio.
 * Orquesta la lógica de negocio de los pilotos y delega la persistencia
 * en el repositorio (puerto de salida), aplicando la arquitectura hexagonal.
 */
public class PilotoServicioImpl implements PilotoServicio {

    // Dependencia de persistencia (puerto de salida).
    private final PilotosRepositorio pilotosRepositorio;

    /**
     * Constructor que inyecta el repositorio de pilotos.
     * Se valida que no sea nulo para evitar fallos posteriores.
     */
    public PilotoServicioImpl(PilotosRepositorio pilotosRepositorio) {

        if (pilotosRepositorio != null) {

            this.pilotosRepositorio = pilotosRepositorio;

        } else {

            throw new IllegalArgumentException("El repositorio de pilotos no puede ser nulo.");

        }
    }

    /** Crea la entidad Piloto y la guarda en el repositorio. */
    @Override
    public void registrar(String nombre, int experiencia, int habilidadLluvia) {

        Piloto piloto = new Piloto(nombre, experiencia, habilidadLluvia);

        pilotosRepositorio.guardar(piloto);

    }

    /** Delega en el repositorio la obtención de todos los pilotos. */
    @Override
    public List<Piloto> listarPilotos() {

        return pilotosRepositorio.listarTodos();

    }

    /** Delega en el repositorio la búsqueda por nombre. */
    @Override
    public Piloto buscarPorNombre(String nombre) {

        return pilotosRepositorio.buscarPorNombre(nombre);

    }

    /** Delega en el repositorio la eliminación por nombre. */
    @Override
    public boolean eliminar(String nombre) {

        return pilotosRepositorio.eliminarPorNombre(nombre);

    }

}