package com.proyectof1;

import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.infraestructura.adaptadores.entrada.VentanaSimulacion;
import com.proyectof1.infraestructura.adaptadores.salida.ClimaHttpAdapter;

public class Main {
    public static void main(String[] args) {

        ClimaHttpAdapter climaAdapter = new ClimaHttpAdapter();

        SimulacionService simulacionService = new SimulacionService(climaAdapter);

        VentanaSimulacion ventana = new VentanaSimulacion(simulacionService);

        ventana.setVisible(true);

    }
}