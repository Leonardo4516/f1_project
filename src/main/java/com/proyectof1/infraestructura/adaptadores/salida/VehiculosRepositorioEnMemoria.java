package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Vehiculo;

public class VehiculosRepositorioEnMemoria implements VehiculosRepositorio {

    private final Map<String, Vehiculo> vehiculos = new HashMap<>();

    @Override
    public void guardar(Vehiculo vehiculo) {

        vehiculos.put(vehiculo.getMarcaEscuderia(), vehiculo);

    }

    @Override
    public List<Vehiculo> listarTodos() {

        return new ArrayList<>(vehiculos.values());

    }

    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        return vehiculos.get(marcaEscuderia);

    }

    @Override
    public boolean eliminarPorEscuderia(String marcaEscuderia) {

        return vehiculos.remove(marcaEscuderia) != null;

    }

}