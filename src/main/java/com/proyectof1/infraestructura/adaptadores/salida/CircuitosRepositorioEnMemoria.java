package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

/**
 * Adaptador de salida (infraestructura) que implementa CircuitosRepositorio.
 * Almacena los circuitos en memoria usando un HashMap, donde la clave es el nombre.
 */
public class CircuitosRepositorioEnMemoria implements CircuitosRepositorio {

    // Estructura en memoria: nombre del circuito como clave, Circuito como valor.
    private final Map<String, Circuito> circuitos = new HashMap<>();

    /** Inserta o actualiza un circuito usando su nombre como clave. */
    @Override
    public void guardar(Circuito circuito) {

        circuitos.put(circuito.getNombre(), circuito);

    }

    /** Devuelve una copia de la lista de todos los circuitos. */
    @Override
    public List<Circuito> listarTodos() {

        return new ArrayList<>(circuitos.values());

    }

    /** Obtiene un circuito por nombre o null si no existe. */
    @Override
    public Circuito buscarPorNombre(String nombre) {

        return circuitos.get(nombre);

    }

    /** Devuelve los circuitos cuya ubicación coincide (ignorando mayúsculas). */
    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        List<Circuito> resultados = new ArrayList<>();

        // Recorre todos los circuitos comprobando si la ubicación coincide.
        for (Circuito circuito : circuitos.values()) {

            if (circuito.getUbicacion().equalsIgnoreCase(ubicacion)) {

                resultados.add(circuito);

            }
        }

        return resultados;
    }

    /** Elimina un circuito por nombre. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorNombre(String nombre) {

        return circuitos.remove(nombre) != null;

    }

}