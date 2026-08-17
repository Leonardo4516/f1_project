package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public interface VehiculoServicio {

    void registrar(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, Piloto piloto);

    List<Vehiculo> listarVehiculos();

    Vehiculo buscarPorEscuderia(String marcaEscuderia);

    boolean eliminar(String marcaEscuderia);

}