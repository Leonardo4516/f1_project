package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

public class PilotosRepositorioEnMemoria implements PilotosRepositorio {

    private final Map<String, Piloto> pilotos = new HashMap<>();

    @Override
    public void guardar(Piloto piloto) {

        pilotos.put(piloto.getNombre(), piloto);

    }

    @Override
    public List<Piloto> listarTodos() {

        return new ArrayList<>(pilotos.values());

    }

    @Override
    public Piloto buscarPorNombre(String nombre) {

        return pilotos.get(nombre);

    }

    @Override
    public boolean eliminarPorNombre(String nombre) {

        return pilotos.remove(nombre) != null;

    }

}