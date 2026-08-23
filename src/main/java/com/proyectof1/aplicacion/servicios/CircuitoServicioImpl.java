package com.proyectof1.aplicacion.servicios;

import java.util.List;
import java.util.Objects;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

/**
 * Implementación del puerto de entrada CircuitoServicio.
 * Orquesta la lógica de negocio de los circuitos y delega la persistencia
 * en el repositorio (puerto de salida), aplicando la arquitectura hexagonal.
 */
public class CircuitoServicioImpl implements CircuitoServicio {

    // Dependencia de persistencia (puerto de salida).
    private final CircuitosRepositorio circuitosRepositorio;

    /**
     * Constructor que inyecta el repositorio de circuitos.
     * Se valida que no sea nulo para evitar fallos posteriores.
     */
    public CircuitoServicioImpl(CircuitosRepositorio circuitosRepositorio) {

        this.circuitosRepositorio = Objects.requireNonNull(circuitosRepositorio,
                "El repositorio de circuitos no puede ser nulo.");

    }

    /** Crea la entidad Circuito y la guarda en el repositorio. */
    @Override
    public void registrar(String nombre, double kilometros, String ubicacion,
            int numCurvas, String tipoCircuito, int vueltasTipicas, String recordVuelta) {

        Circuito circuito = new Circuito(nombre, kilometros, ubicacion,
                numCurvas, tipoCircuito, vueltasTipicas, recordVuelta);

        circuitosRepositorio.guardar(circuito);

    }

    /** Delega en el repositorio la obtención de todos los circuitos. */
    @Override
    public List<Circuito> listarCircuitos() {

        return circuitosRepositorio.listarTodos();

    }

    /** Delega en el repositorio la búsqueda por nombre. */
    @Override
    public Circuito buscarPorNombre(String nombre) {

        return circuitosRepositorio.buscarPorNombre(nombre);

    }

    /** Delega en el repositorio la búsqueda por ubicación. */
    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        return circuitosRepositorio.buscarPorUbicacion(ubicacion);

    }

    /** Delega en el repositorio la eliminación por nombre. */
    @Override
    public boolean eliminar(String nombre) {

        return circuitosRepositorio.eliminarPorNombre(nombre);

    }

}