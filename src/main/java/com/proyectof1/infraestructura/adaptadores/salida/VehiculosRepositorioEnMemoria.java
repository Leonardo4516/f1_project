package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Vehiculo;

/**
 * Adaptador de salida (infraestructura) que implementa VehiculosRepositorio.
 * Almacena los vehículos en memoria usando un HashMap, donde la clave es la escudería.
 */
public class VehiculosRepositorioEnMemoria implements VehiculosRepositorio {

    // Estructura en memoria: nombre de escudería como clave, Vehiculo como valor.
    private final Map<String, Vehiculo> vehiculos = new HashMap<>();

    /** Inserta o actualiza un vehículo usando su escudería como clave. */
    @Override
    public void guardar(Vehiculo vehiculo) {

        vehiculos.put(vehiculo.getMarcaEscuderia(), vehiculo);

    }

    /** Devuelve una copia de la lista de todos los vehículos. */
    @Override
    public List<Vehiculo> listarTodos() {

        return new ArrayList<>(vehiculos.values());

    }

    /** Obtiene un vehículo por escudería o null si no existe. */
    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        return vehiculos.get(marcaEscuderia);

    }

    /** Elimina un vehículo por escudería. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorEscuderia(String marcaEscuderia) {

        return vehiculos.remove(marcaEscuderia) != null;

    }

}