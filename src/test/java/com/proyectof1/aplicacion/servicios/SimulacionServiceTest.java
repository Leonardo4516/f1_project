package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba la lógica de simulación con compuestos, clima y clasificación.
 */
class SimulacionServiceTest {

    // Adaptador de clima de prueba: siempre devuelve el clima indicado.
    private static ClimaServicePort climaFijo(String clima) {
        return ubicacion -> clima;
    }

    private Vehiculo vehiculo(Piloto piloto, String escuderia, int velocidad) {
        return new Vehiculo(escuderia, velocidad, 0.0, piloto);
    }

    private Circuito circuito() {
        return new Circuito("Monza", 5.793, "Italia");
    }

    @Test
    void compuestoBlandoEsMasRapidoQueElDuro() {

        SimulacionService servicio = new SimulacionService(climaFijo("Seco"));
        Circuito circuito = circuito();
        Vehiculo auto = vehiculo(new Piloto("Max Verstappen", 97, 92), "Red Bull", 340);

        double tiempoBlando = servicio.simularVuelta(auto, circuito, "Seco", CompuestoNeumatico.BLANDO);
        double tiempoDuro = servicio.simularVuelta(auto, circuito, "Seco", CompuestoNeumatico.DURO);

        assertTrue(tiempoBlando < tiempoDuro, "Con blando debe cerrarse más rápido que con duro");

    }

    @Test
    void lluviaPenalizaAMenosHabilidad() {

        SimulacionService servicio = new SimulacionService(climaFijo("Lluvia"));
        Circuito circuito = circuito();

        Vehiculo habilidoso = vehiculo(new Piloto("Max Verstappen", 90, 95), "Red Bull", 340);
        Vehiculo torpe = vehiculo(new Piloto("Piloto Novato", 90, 30), "Williams", 340);

        double tiempoHabilidad = servicio.simularVuelta(habilidoso, circuito, "Lluvia", CompuestoNeumatico.MEDIO);
        double tiempoTorpe = servicio.simularVuelta(torpe, circuito, "Lluvia", CompuestoNeumatico.MEDIO);

        assertTrue(tiempoTorpe > tiempoHabilidad, "Bajo lluvia el piloto con menos habilidad debe ser más lento");

    }

    @Test
    void desgasteAumentaSegunElCompuesto() {

        Vehiculo auto = vehiculo(new Piloto("Charles Leclerc", 93, 92), "Ferrari", 338);
        SimulacionService servicio = new SimulacionService(climaFijo("Seco"));

        servicio.simularVuelta(auto, circuito(), "Seco", CompuestoNeumatico.BLANDO);

        assertEquals(CompuestoNeumatico.BLANDO.getDesgastePorVuelta(), auto.getDesgasteNeumaticos(), 0.001);

    }

    @Test
    void clasificacionOrdenaDeMejorAPeor() {

        SimulacionService servicio = new SimulacionService(climaFijo("Seco"));
        Circuito circuito = circuito();

        List<Vehiculo> autos = new ArrayList<>(Arrays.asList(
                vehiculo(new Piloto("Lento", 60, 60), "Williams", 300),
                vehiculo(new Piloto("Rápido", 99, 95), "Red Bull", 360),
                vehiculo(new Piloto("Medio", 85, 85), "McLaren", 340)));

        List<Vehiculo> parrilla = servicio.simularClasificacion(autos, circuito, "Seco");

        assertEquals(3, parrilla.size());
        assertEquals("Rápido", parrilla.get(0).getPiloto().getNombre());

        // La lista original no se modifica.
        assertEquals("Lento", autos.get(0).getPiloto().getNombre());

    }

    @Test
    void climaAutomaticoUsaELPuerto() {

        SimulacionService servicio = new SimulacionService(climaFijo("Lluvia"));
        assertEquals("Lluvia", servicio.resolverClima(circuito(), "Auto"));
        assertEquals("Seco", servicio.resolverClima(circuito(), "Seco"));
        assertEquals("Lluvia", servicio.resolverClima(circuito(), "Lluvia"));

    }

}