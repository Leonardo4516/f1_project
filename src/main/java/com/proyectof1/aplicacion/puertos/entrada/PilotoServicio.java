package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Piloto;

public interface PilotoServicio {

    void registrar(String nombre, int experiencia, int habilidadLluvia);

    List<Piloto> listarPilotos();

    Piloto buscarPorNombre(String nombre);

    boolean eliminar(String nombre);

}