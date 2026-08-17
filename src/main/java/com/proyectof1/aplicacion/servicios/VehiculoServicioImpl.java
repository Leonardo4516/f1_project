package com.proyectof1.aplicacion.servicios;

import java.util.List;

import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public class VehiculoServicioImpl implements VehiculoServicio {

    private final VehiculosRepositorio vehiculosRepositorio;

    public VehiculoServicioImpl(VehiculosRepositorio vehiculosRepositorio) {

        if (vehiculosRepositorio != null) {

            this.vehiculosRepositorio = vehiculosRepositorio;

        } else {

            throw new IllegalArgumentException("El repositorio de vehículos no puede ser nulo.");

        }
    }

    @Override
    public void registrar(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, Piloto piloto) {

        Vehiculo vehiculo = new Vehiculo(marcaEscuderia, velocidadMaxima, desgasteNeumaticos, piloto);

        vehiculosRepositorio.guardar(vehiculo);

    }

    @Override
    public List<Vehiculo> listarVehiculos() {

        return vehiculosRepositorio.listarTodos();

    }

    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        return vehiculosRepositorio.buscarPorEscuderia(marcaEscuderia);

    }

    @Override
    public boolean eliminar(String marcaEscuderia) {

        return vehiculosRepositorio.eliminarPorEscuderia(marcaEscuderia);

    }

}