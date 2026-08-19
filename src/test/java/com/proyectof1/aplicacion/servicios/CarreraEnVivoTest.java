package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.proyectof1.aplicacion.servicios.CarreraEnVivo.AutoEnCarrera;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.ResultadoCarrera;
import com.proyectof1.dominio.ResultadoParticipante;
import com.proyectof1.dominio.Vehiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del motor de carrera en vivo {@link CarreraEnVivo}.
 * El motor usa una semilla fija (Random 42), por lo que cada carrera con los
 * mismos datos siempre produce el mismo resultado y los tests son estables.
 * Las aserciones se basan en invariantes para no depender de valores mágicos.
 */
class CarreraEnVivoTest {

    private static Circuito circuito() {
        return new Circuito("Monza", 5.793, "Italia");
    }

    private static Vehiculo vehiculo(String nombrePiloto, String escuderia, int velocidad) {
        return new Vehiculo(escuderia, velocidad, 0.0, new Piloto(nombrePiloto, 90, 80));
    }

    private static List<Vehiculo> parrillaDeTres() {
        return new ArrayList<>(Arrays.asList(
                vehiculo("Piloto A", "Red Bull", 340),
                vehiculo("Piloto B", "Ferrari", 338),
                vehiculo("Piloto C", "McLaren", 339)));
    }

    private static CarreraEnVivo carreraDeTres(int vueltas) {
        return new CarreraEnVivo(parrillaDeTres(), circuito(), "Seco", null, vueltas);
    }

    /** Avanza la carrera en pasos grandes hasta que termine (con salvaguarda). */
    private static void correrHastaElFinal(CarreraEnVivo carrera) {
        int salvaguarda = 0;
        while (!carrera.estaFinalizada() && salvaguarda++ < 100_000) {
            carrera.avanzar(1000.0);
        }
    }

    @Test
    void constructorRechazaDatosInvalidos() {

        List<Vehiculo> parrilla = parrillaDeTres();

        assertThrows(IllegalArgumentException.class,
                () -> new CarreraEnVivo(null, circuito(), "Seco", null, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new CarreraEnVivo(new ArrayList<>(), circuito(), "Seco", null, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new CarreraEnVivo(parrilla, null, "Seco", null, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new CarreraEnVivo(parrilla, circuito(), "Seco", null, 0));

    }

    @Test
    void avanzarProgresaLaDistanciaDeTodosLosAutos() {

        CarreraEnVivo carrera = carreraDeTres(5);
        carrera.avanzar(120.0);

        for (AutoEnCarrera auto : carrera.ranking()) {
            assertTrue(auto.getDistanciaKm() > 0, "El auto debe haber recorrido metros");
        }

        assertTrue(carrera.getTiempoCarrera() > 0);

    }

    @Test
    void avanzarConSegundosInvalidosNoAvanza() {

        CarreraEnVivo carrera = carreraDeTres(10);
        carrera.avanzar(0);
        carrera.avanzar(-5);

        for (AutoEnCarrera auto : carrera.ranking()) {
            assertEquals(0.0, auto.getDistanciaKm(), 0.0001);
        }

    }

    @Test
    void rankingOrdenaDeMasAMenosDistancia() {

        CarreraEnVivo carrera = carreraDeTres(10);
        carrera.avanzar(60.0);
        carrera.avanzar(60.0);

        List<AutoEnCarrera> ranking = carrera.ranking();

        for (int i = 1; i < ranking.size(); i++) {

            assertTrue(ranking.get(i - 1).getDistanciaKm() >= ranking.get(i).getDistanciaKm());

        }

    }

    @Test
    void sinCompuestosIndicadosElPorDefectoEsMedio() {

        CarreraEnVivo carrera = carreraDeTres(3);

        for (AutoEnCarrera auto : carrera.ranking()) {
            assertEquals(CompuestoNeumatico.MEDIO, auto.getCompuesto());
        }

    }

    @Test
    void gapAlLiderDelLiderEsCero() {

        CarreraEnVivo carrera = carreraDeTres(5);
        carrera.avanzar(60.0);

        AutoEnCarrera lider = carrera.ranking().get(0);
        assertEquals(0.0, carrera.gapAlLider(lider), 0.0001);

    }

    @Test
    void carreraTerminaYGeneraResultadoCompleto() {

        CarreraEnVivo carrera = carreraDeTres(3);
        correrHastaElFinal(carrera);

        assertTrue(carrera.estaFinalizada());

        ResultadoCarrera resultado = carrera.resultadoFinal();
        List<ResultadoParticipante> participantes = resultado.participantes();

        assertEquals(3, participantes.size());
        assertNotNull(resultado.ganador());

        // Posiciones consecutivas y únicas 1..N.
        for (int i = 0; i < participantes.size(); i++) {
            assertEquals(i + 1, participantes.get(i).posicion());
        }

        // El ganador debe haber finalizado cruzando al menos todas las vueltas
        // (con pasos grandes el motor puede cerrar vueltas de más al pasar la meta).
        ResultadoParticipante ganador = resultado.ganador();
        assertEquals("Finalizado", ganador.estado());
        assertTrue(ganador.vueltas() >= 3);
        assertTrue(ganador.tiempoTotal() > 0);

        // El clima, el circuito y las vueltas se reflejan en el resultado.
        assertEquals("Seco", resultado.clima());
        assertEquals(3, resultado.totalVueltas());
        assertEquals(circuito().getNombre(), resultado.circuito().getNombre());

    }

    @Test
    void participantesReportanEstadosValidos() {

        CarreraEnVivo carrera = carreraDeTres(20);
        correrHastaElFinal(carrera);

        ResultadoCarrera resultado = carrera.resultadoFinal();

        for (ResultadoParticipante participante : resultado.participantes()) {

            assertTrue("Finalizado".equals(participante.estado()) || "DNF".equals(participante.estado()));
            assertTrue(participante.vueltas() >= 0);

        }

    }

    @Test
    void vueltaRapidaSeReflejaEnElResultado() {

        CarreraEnVivo carrera = carreraDeTres(10);
        correrHastaElFinal(carrera);

        ResultadoParticipante autor = carrera.resultadoFinal().autorVueltaRapida();

        if (autor != null) {
            assertTrue(autor.vueltaRapida());
        }

    }

    @Test
    void progresoYVueltaDelLiderQuedanAcotados() {

        CarreraEnVivo carrera = carreraDeTres(3);

        // Antes de arrancar: sin progreso y en la vuelta 1.
        assertEquals(0.0, carrera.progresoPorcentaje(), 0.0001);
        assertEquals(1, carrera.vueltaDelLider());

        correrHastaElFinal(carrera);

        assertEquals(100.0, carrera.progresoPorcentaje(), 0.0001);
        assertEquals(3, carrera.vueltaDelLider());

    }

    @Test
    void avanzarNoHaceNadaTrasFinalizar() {

        CarreraEnVivo carrera = carreraDeTres(1);
        correrHastaElFinal(carrera);

        assertTrue(carrera.estaFinalizada());

        double tiempo = carrera.getTiempoCarrera();
        carrera.avanzar(1000.0);

        assertEquals(tiempo, carrera.getTiempoCarrera(), 0.0001);

    }

    @Test
    void getEventosDevuelveUnaCopiaDefensiva() {

        CarreraEnVivo carrera = carreraDeTres(20);
        correrHastaElFinal(carrera);

        int tamanoOriginal = carrera.getEventos().size();

        List<String> externos = carrera.getEventos();
        externos.clear();
        externos.add("evento falso");

        assertEquals(tamanoOriginal, carrera.getEventos().size());

    }

    @Test
    void conLaMismaSemillaElResultadoEsReproducible() {

        CarreraEnVivo c1 = carreraDeTres(15);
        CarreraEnVivo c2 = carreraDeTres(15);
        correrHastaElFinal(c1);
        correrHastaElFinal(c2);

        ResultadoParticipante ganador1 = c1.resultadoFinal().ganador();
        ResultadoParticipante ganador2 = c2.resultadoFinal().ganador();

        assertEquals(ganador1.vehiculo().getMarcaEscuderia(), ganador2.vehiculo().getMarcaEscuderia());
        assertEquals(ganador1.vehiculo().getPiloto().getNombre(), ganador2.vehiculo().getPiloto().getNombre());

    }

}