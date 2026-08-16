package com.proyectof1.infraestructura.adaptadores.salida;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;

public class ClimaHttpAdapter implements ClimaServicePort {

    @Override public String obtenerClima(String ubicacion){

        return "Seco";

    }

}
