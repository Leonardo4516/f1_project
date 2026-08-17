package com.proyectof1.infraestructura.adaptadores.salida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

public class CircuitosRepositorioEnMemoria implements CircuitosRepositorio {

    private final Map<String, Circuito> circuitos = new HashMap<>();

    @Override
    public void guardar(Circuito circuito) {

        circuitos.put(circuito.getNombre(), circuito);

    }

    @Override
    public List<Circuito> listarTodos() {

        return new ArrayList<>(circuitos.values());

    }

    @Override
    public Circuito buscarPorNombre(String nombre) {

        return circuitos.get(nombre);

    }

    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        List<Circuito> resultados = new ArrayList<>();

        for (Circuito circuito : circuitos.values()) {

            if (circuito.getUbicacion().equalsIgnoreCase(ubicacion)) {

                resultados.add(circuito);

            }
        }

        return resultados;
    }

    @Override
    public boolean eliminarPorNombre(String nombre) {

        return circuitos.remove(nombre) != null;

    }

}