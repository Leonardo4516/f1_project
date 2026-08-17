package com.proyectof1.aplicacion.puertos.salida;

import java.util.List;

import com.proyectof1.dominio.Vehiculo;

public interface VehiculosRepositorio {

    void guardar(Vehiculo vehiculo);

    List<Vehiculo> listarTodos();

    Vehiculo buscarPorEscuderia(String marcaEscuderia);

    boolean eliminarPorEscuderia(String marcaEscuderia);

}
