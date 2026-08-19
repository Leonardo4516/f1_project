package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.Vehiculo;

/**
 * Servicio de aplicación que gestiona la lógica de la simulación de carreras.
 * Consulta el clima real a través del puerto de salida y calcula los tiempos
 * de vuelta de cada vehículo según sus características, el compuesto de
 * neumáticos elegido y el desgaste acumulado.
 */
public class SimulacionService {

    // Dependencia (puerto de salida) para consultar el clima.
    private final ClimaServicePort climaService;

    /**
     * Constructor que inyecta el servicio de clima.
     * Se valida que no sea nulo para evitar fallos posteriores.
     */
    public SimulacionService(ClimaServicePort climaService) {

        this.climaService = Objects.requireNonNull(climaService,
                "El servicio de clima no puede ser nulo.");

    }

    /**
     * Consulta el clima real de la ubicación del circuito a través del puerto de salida.
     *
     * @param circuito Circuito del que se quiere conocer el clima.
     * @return Estado del clima ("Lluvia" o "Seco").
     */
    public String consultarClima(Circuito circuito) {

        return climaService.obtenerClima(circuito.getUbicacion());

    }

    /**
     * Calcula el tiempo de una vuelta usando el compuesto por defecto (blando).
     * Se conserva para mantener compatibilidad con las llamadas anteriores.
     */
    public double simularVuelta(Vehiculo vehiculo, Circuito circuito, String clima) {

        return simularVuelta(vehiculo, circuito, clima, CompuestoNeumatico.BLANDO);

    }

    /**
     * Calcula el tiempo (en segundos) de una vuelta de un vehículo en un circuito.
     * La fórmula tiene en cuenta:
     *  - La longitud del circuito y la velocidad máxima del vehículo.
     *  - El compuesto de neumáticos (frena por agarre y se frena aún más con el desgaste).
     *  - La experiencia del piloto (a más experiencia, menor tiempo).
     *  - En caso de lluvia, una penalización inversamente proporcional a la
     *    habilidad del piloto bajo lluvia.
     * Además, incrementa el desgaste de los neumáticos del vehículo según el compuesto.
     *
     * @param vehiculo Vehículo que participa.
     * @param circuito Circuito donde se corre.
     * @param clima    Estado del clima.
     * @param compuesto Compuesto de neumáticos montado en el vehículo.
     * @return Tiempo estimado de la vuelta en segundos.
     */
    public double simularVuelta(Vehiculo vehiculo, Circuito circuito, String clima, CompuestoNeumatico compuesto) {

        // Se proyecta el tiempo con el desgaste actual del vehículo.
        double tiempoCalculado = proyectarVuelta(vehiculo, circuito, clima, compuesto, vehiculo.getDesgasteNeumaticos());

        // Cada vuelta aumenta el desgaste según el compuesto, sin superar el 100%.
        double nuevoDesgaste = Math.min(vehiculo.getDesgasteNeumaticos() + compuesto.getDesgastePorVuelta(), 100.0);
        vehiculo.setDesgasteNeumaticos(nuevoDesgaste);

        return tiempoCalculado;
    }

    /**
     * Proyecta el tiempo (en segundos) de una vuelta sin modificar el estado
     * del vehículo. El desgaste se pasa como parámetro, lo que permite al
     * motor de carrera simular escenarios (paradas, repostajes) sin tocar la
     * entidad original. La fórmula es la misma que en simularVuelta.
     *
     * @param vehiculo  Vehículo que participa.
     * @param circuito  Circuito donde se corre.
     * @param clima     Estado del clima.
     * @param compuesto Compuesto de neumáticos montado en el vehículo.
     * @param desgaste  Desgaste de neumáticos con el que proyectar la vuelta (0-100).
     * @return Tiempo estimado de la vuelta en segundos.
     */
    public double proyectarVuelta(Vehiculo vehiculo, Circuito circuito, String clima, CompuestoNeumatico compuesto, double desgaste) {

        // Cada 100% de desgaste resta 5 km/h a la velocidad máxima.
        double penalizacionDesgaste = desgaste * 0.05;

        // Velocidad efectiva: máxima - pérdida del compuesto - desgaste.
        double velocidadEfectiva = vehiculo.getVelocidadMaxima() - compuesto.getPerdidaVelocidad() - penalizacionDesgaste;

        // Guarda contra un valor imposible que dividiría entre cero.
        if (velocidadEfectiva <= 0) {

            velocidadEfectiva = 1.0;

        }

        // Tiempo base: convertir (km / km/h) a horas y luego a segundos (x 3600).
        double tiempoBase = (circuito.getKilometros() / velocidadEfectiva) * 3600.0;

        // La experiencia (1-100) reduce el tiempo: a 100 de experiencia, factor 0.5 -> la mitad del tiempo.
        double factorExperiencia = 1.0 - (vehiculo.getPiloto().getExperiencia() / 200.0);

        double tiempoCalculado = tiempoBase * factorExperiencia;

        // Si llueve, se penaliza el tiempo según la falta de habilidad para lluvia del piloto.
        if (clima != null && clima.equalsIgnoreCase("Lluvia")) {

            double penalizacionLluvia = 20.0 * (1.0 - (vehiculo.getPiloto().getHabilidadLluvia() / 100.0));
            tiempoCalculado += penalizacionLluvia;

        }

        return tiempoCalculado;
    }

    /**
     * Simula una sesión de clasificación de una vuelta lanzada por cada vehículo.
     *
     * @param vehiculos Vehículos que participan en la clasificación.
     * @param circuito  Circuito donde se corre.
     * @param clima     Estado del clima.
     * @return Una lista (NUEVA, sin modificar la original) con los vehículos
     *         ordenados de mejor a peor tiempo: la parrilla de salida.
     */
    public List<Vehiculo> simularClasificacion(List<Vehiculo> vehiculos, Circuito circuito, String clima) {

        // Se calcula un tiempo por vehículo antes de ordenar para no repetir
        // la vuelta lanzada durante la comparación del ordenamiento. Se proyecta
        // con el desgaste actual para no modificar el estado de los vehículos.
        Map<Vehiculo, Double> tiempos = new HashMap<>();

        for (Vehiculo vehiculo : vehiculos) {

            double tiempo = proyectarVuelta(vehiculo, circuito, clima, CompuestoNeumatico.BLANDO, vehiculo.getDesgasteNeumaticos());
            tiempos.put(vehiculo, tiempo);

        }

        List<Vehiculo> parrilla = new ArrayList<>(vehiculos);

        parrilla.sort(Comparator.comparingDouble(tiempos::get));

        return parrilla;
    }

    /**
     * Resuelve el clima efectivo de la carrera según la configuración:
     * si es automático se consulta la API; si no, se usa el clima forzado.
     *
     * @param circuito     Circuito de la carrera.
     * @param climaElegido "Auto", "Seco" o "Lluvia".
     * @return El clima concreto con el que se simulará: "Lluvia" o "Seco".
     */
    public String resolverClima(Circuito circuito, String climaElegido) {

        if (climaElegido != null && climaElegido.equalsIgnoreCase("Auto")) {

            return consultarClima(circuito);

        }

        return "Lluvia".equals(climaElegido) ? "Lluvia" : "Seco";

    }

}