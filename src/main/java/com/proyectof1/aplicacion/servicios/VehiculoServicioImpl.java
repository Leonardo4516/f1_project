package com.proyectof1.aplicacion.servicios;

import java.util.List;
import java.util.Objects;

import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

/**
 * Implementación del puerto de entrada VehiculoServicio.
 * Orquesta la lógica de negocio de los vehículos y delega la persistencia
 * en el repositorio (puerto de salida), aplicando la arquitectura hexagonal.
 */
public class VehiculoServicioImpl implements VehiculoServicio {

    // Dependencia de persistencia (puerto de salida).
    private final VehiculosRepositorio vehiculosRepositorio;

    /**
     * Constructor que inyecta el repositorio de vehículos.
     * Se valida que no sea nulo para evitar fallos posteriores.
     */
    public VehiculoServicioImpl(VehiculosRepositorio vehiculosRepositorio) {

        this.vehiculosRepositorio = Objects.requireNonNull(vehiculosRepositorio,
                "El repositorio de vehículos no puede ser nulo.");

    }

    /** Crea la entidad Vehiculo y la guarda en el repositorio. */
    @Override
    public void registrar(String marcaEscuderia, int velocidadMaxima, int aceleracion,
            int frenado, int agarre, Piloto piloto) {

        Vehiculo vehiculo = new Vehiculo(marcaEscuderia, velocidadMaxima, aceleracion, frenado, agarre, piloto);

        vehiculosRepositorio.guardar(vehiculo);

    }

    /** Delega en el repositorio la obtención de todos los vehículos. */
    @Override
    public List<Vehiculo> listarVehiculos() {

        return vehiculosRepositorio.listarTodos();

    }

    /** Delega en el repositorio la búsqueda por escudería. */
    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        return vehiculosRepositorio.buscarPorEscuderia(marcaEscuderia);

    }

    /** Delega en el repositorio la eliminación por escudería. */
    @Override
    public boolean eliminar(String marcaEscuderia) {

        return vehiculosRepositorio.eliminarPorEscuderia(marcaEscuderia);

    }

}