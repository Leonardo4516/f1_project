package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Piloto;

public interface PilotosRepositorio {

    void guardar(Piloto piloto);

    List<Piloto> listarTodos();

    Piloto buscarPorNombre(String nombre);

    boolean eliminarPorNombre(String nombre);

}
