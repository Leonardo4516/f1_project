package com.proyectof1.aplicacion.servicios;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.Vehiculo;

public class SimulacionService {

    private final ClimaServicePort climaService;

    public SimulacionService(ClimaServicePort climaService) {
        
        if (climaService != null) {

            this.climaService = climaService;
            
        } else {

            throw new IllegalArgumentException("El servicio de clima no puede ser nulo.");

        }
    }

    public double simularVuelta(Vehiculo vehiculo, Circuito circuito){

        String clima = climaService.obtenerClima(circuito.getUbicacion());

        double tiempoBase = (circuito.getKilometros() / vehiculo.getVelocidadMaxima()) * 3600.0;

        double factorExperiencia = 1.0 - (vehiculo.getPiloto().getExperiencia() / 200.0);

        double tiempoCalculado = tiempoBase * factorExperiencia;

        if (clima != null && clima.equalsIgnoreCase("Lluvia")) {
            
            double penalizacionLluvia = 20.0 * (1.0 - (vehiculo.getPiloto().getHabilidadLluvia() / 100.0));
            tiempoCalculado += penalizacionLluvia;

        }

        double nuevoDesgaste = Math.min(vehiculo.getDesgasteNeumaticos() + 1.5, 100.0);
        vehiculo.setDesgasteNeumaticos(nuevoDesgaste);

        return tiempoCalculado;
    }
}
