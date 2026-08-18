package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

/**
 * Adaptador de salida (infraestructura) que implementa PilotosRepositorio.
 * Almacena los pilotos en memoria usando un HashMap, donde la clave es el nombre.
 */
public class PilotosRepositorioEnMemoria implements PilotosRepositorio {

    // Estructura en memoria: nombre del piloto como clave, Piloto como valor.
    private final Map<String, Piloto> pilotos = new HashMap<>();

    /** Inserta o actualiza un piloto usando su nombre como clave. */
    @Override
    public void guardar(Piloto piloto) {

        pilotos.put(piloto.getNombre(), piloto);

    }

    /** Devuelve una copia de la lista de todos los pilotos. */
    @Override
    public List<Piloto> listarTodos() {

        return new ArrayList<>(pilotos.values());

    }

    /** Obtiene un piloto por nombre o null si no existe. */
    @Override
    public Piloto buscarPorNombre(String nombre) {

        return pilotos.get(nombre);

    }

    /** Elimina un piloto por nombre. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorNombre(String nombre) {

        return pilotos.remove(nombre) != null;

    }

}