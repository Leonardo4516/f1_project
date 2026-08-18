package com.proyectof1.aplicacion.servicios;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.Vehiculo;

/**
 * Servicio de aplicación que gestiona la lógica de la simulación de carreras.
 * Consulta el clima real a través del puerto de salida y calcula
 * los tiempos de vuelta de cada vehículo según sus características.
 */
public class SimulacionService {

    // Dependencia (puerto de salida) para consultar el clima.
    private final ClimaServicePort climaService;

    /**
     * Constructor que inyecta el servicio de clima.
     * Se valida que no sea nulo para evitar fallos posteriores.
     */
    public SimulacionService(ClimaServicePort climaService) {

        if (climaService != null) {

            this.climaService = climaService;

        } else {

            throw new IllegalArgumentException("El servicio de clima no puede ser nulo.");

        }
    }

    /**
     * Consulta el clima de la ubicación del circuito a través del puerto de salida.
     *
     * @param circuito Circuito del que se quiere conocer el clima.
     * @return Estado del clima ("Lluvia" o "Seco").
     */
    public String consultarClima(Circuito circuito) {

        String clima;

        return clima = climaService.obtenerClima(circuito.getUbicacion());

    }

    /**
     * Calcula el tiempo (en segundos) de una vuelta de un vehículo en un circuito.
     * La fórmula tiene en cuenta:
     *  - La longitud del circuito y la velocidad máxima del vehículo.
     *  - La experiencia del piloto (a más experiencia, menor tiempo).
     *  - En caso de lluvia, una penalización inversamente proporcional a la
     *    habilidad del piloto bajo lluvia.
     * Además, incrementa el desgaste de los neumáticos del vehículo en 1.5%.
     *
     * @param vehiculo Vehículo que participa.
     * @param circuito Circuito donde se corre.
     * @param clima    Estado del clima.
     * @return Tiempo estimado de la vuelta en segundos.
     */
    public double simularVuelta(Vehiculo vehiculo, Circuito circuito, String clima) {

        // Tiempo base: convertir (km / km/h) a horas y luego a segundos (x 3600).
        double tiempoBase = (circuito.getKilometros() / vehiculo.getVelocidadMaxima()) * 3600.0;

        // La experiencia (1-100) reduce el tiempo: a 100 de experiencia, factor 0.5 -> la mitad del tiempo.
        double factorExperiencia = 1.0 - (vehiculo.getPiloto().getExperiencia() / 200.0);

        double tiempoCalculado = tiempoBase * factorExperiencia;

        // Si llueve, se penaliza el tiempo según la falta de habilidad para lluvia del piloto.
        if (clima != null && clima.equalsIgnoreCase("Lluvia")) {

            double penalizacionLluvia = 20.0 * (1.0 - (vehiculo.getPiloto().getHabilidadLluvia() / 100.0));
            tiempoCalculado += penalizacionLluvia;

        }

        // Cada vuelta aumenta el desgaste en un 1.5%, sin superar el 100%.
        double nuevoDesgaste = Math.min(vehiculo.getDesgasteNeumaticos() + 1.5, 100.0);
        vehiculo.setDesgasteNeumaticos(nuevoDesgaste);

        return tiempoCalculado;
    }
}