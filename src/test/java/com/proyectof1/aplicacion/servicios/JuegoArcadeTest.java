package com.proyectof1.aplicacion.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.proyectof1.aplicacion.servicios.JuegoArcade.Dificultad;
import com.proyectof1.aplicacion.servicios.JuegoArcade.Obstaculo;

/**
 * Pruebas del núcleo del juego arcade. Se usa una semilla fija de Random para
 * que los escenarios sean reproducibles, igual que en el resto de la capa de
 * aplicación. Se cubren: geometría de carriles, hitbox, vidas, dificultad
 * progresiva y la garantía de un carril libre.
 */
class JuegoArcadeTest {

    private JuegoArcade juegoConSemilla(long semilla) {
        return new JuegoArcade(new Random(semilla), 0, Dificultad.NORMAL);
    }

    @Test
    void arrancaEnElCarrilCentralConVidasLlenasYSinFinDePartida() {

        JuegoArcade juego = juegoConSemilla(1L);

        assertEquals(JuegoArcade.CANTIDAD_CARRILES / 2, juego.getCarrilCoche());
        assertEquals(JuegoArcade.VIDAS_INICIALES, juego.getVidas());
        assertEquals(0, juego.getPuntuacion());
        assertFalse(juego.isGameOver());

    }

    @Test
    void cambiarCarrilIzquierdaNoSabeSalirsePorLaIzquierda() {

        JuegoArcade juego = juegoConSemilla(1L);

        for (int i = 0; i < 10; i++) {
            juego.cambiarCarrilIzquierda();
        }

        assertEquals(0, juego.getCarrilCoche());

    }

    @Test
    void cambiarCarrilDerechaNoSabeSalirsePorLaDerecha() {

        JuegoArcade juego = juegoConSemilla(1L);

        for (int i = 0; i < 10; i++) {
            juego.cambiarCarrilDerecha();
        }

        assertEquals(JuegoArcade.CANTIDAD_CARRILES - 1, juego.getCarrilCoche());

    }

    @Test
    void avanzarAumentaLaPuntuacionYLosObstaculosCaenHaciaElJugador() {

        JuegoArcade juego = juegoConSemilla(7L);

        juego.avanzar();

        assertEquals(1, juego.getPuntuacion());

        // La velocidad es positiva: los obstáculos bajan (y crece) con el tiempo.
        assertTrue(juego.getVelocidad() > 0);
    }

    @Test
    void laDificultadProgresivaAceleraLaVelocidad() {

        JuegoArcade juego = juegoConSemilla(11L);

        double velocidadInicial = juego.getVelocidad();
        int nivelInicial = juego.getNivel();

        // Se avanza hasta subir al menos un nivel de dificultad.
        int pasos = 0;
        while (juego.getNivel() == nivelInicial && !juego.isGameOver() && pasos < 2000) {
            juego.avanzar();
            pasos++;
        }

        assertTrue(juego.getVelocidad() > velocidadInicial,
                "A más puntos, mayor velocidad de caída de los obstáculos.");
    }

    @Test
    void elChoqueRestaUnaVidaYNoTerminaAlPrimerGolpe() {

        // Semilla elegida para producir al menos un choque pronto; como hay varias
        // vidas e inmunidad, un solo golpe no debe acabar la partida.
        JuegoArcade juego = juegoConSemilla(5L);

        int vidasIniciales = juego.getVidas();
        boolean golpeado = false;

        for (int i = 0; i < 400 && !juego.isGameOver(); i++) {

            int vidasAntes = juego.getVidas();
            juego.avanzar();

            if (juego.getVidas() < vidasAntes) {
                golpeado = true;
                break;
            }
        }

        assertTrue(golpeado, "En algún momento el coche choca y pierde una vida.");
        assertFalse(juego.isGameOver(), "Con vidas restantes, el juego no termina en el primer golpe.");
        assertTrue(juego.getVidas() < vidasIniciales);
    }

    @Test
    void laPartidaTerminaAlAgotarLasVidas() {

        JuegoArcade juego = juegoConSemilla(13L);

        int pasos = 0;
        while (!juego.isGameOver() && pasos < 5000) {
            juego.avanzar();
            pasos++;
        }

        assertTrue(juego.isGameOver(), "Al agotar las vidas el juego termina.");
        assertEquals(0, juego.getVidas());
    }

    @Test
    void nuncaSeLlenanTodosLosCarrilesEnLaFranjaDeAparicion() {

        // Garantía de jugabilidad: siempre debe quedar al menos un carril libre
        // entre los obstáculos recién aparecidos.
        JuegoArcade juego = juegoConSemilla(17L);

        for (int paso = 0; paso < 300 && !juego.isGameOver(); paso++) {

            juego.avanzar();

            boolean[] carrilOcupado = new boolean[JuegoArcade.CANTIDAD_CARRILES];
            int ocupados = 0;

            for (Obstaculo o : juego.getObstaculos()) {
                if (o.getY() < 90.0 && !carrilOcupado[o.getCarril()]) {
                    carrilOcupado[o.getCarril()] = true;
                    ocupados++;
                }
            }

            assertTrue(ocupados <= JuegoArcade.CANTIDAD_CARRILES - 1,
                    "No debe haber un muro que bloquee todos los carriles.");
        }
    }

    @Test
    void reiniciarLimpiaElEstadoYCOnservaElRecord() {

        JuegoArcade juego = juegoConSemilla(3L);
        juego.avanzar();
        juego.avanzar();
        juego.avanzar();

        int recordPrev = juego.getRecord();
        juego.reiniciar();

        assertEquals(0, juego.getPuntuacion());
        assertEquals(JuegoArcade.CANTIDAD_CARRILES / 2, juego.getCarrilCoche());
        assertEquals(JuegoArcade.VIDAS_INICIALES, juego.getVidas());
        assertFalse(juego.isGameOver());
        assertEquals(recordPrev, juego.getRecord());

    }

    @Test
    void elRecordNuncaBaja() {

        JuegoArcade juego = new JuegoArcade(new Random(8L), 10, Dificultad.NORMAL);

        for (int i = 0; i < 50 && !juego.isGameOver(); i++) {
            juego.avanzar();
        }

        assertTrue(juego.getRecord() >= 10);

    }

    @Test
    void laSeparacionMinimaPorCarrilSeCumple() {

        JuegoArcade juego = juegoConSemilla(23L);

        for (int paso = 0; paso < 500 && !juego.isGameOver(); paso++) {

            juego.avanzar();

            for (int c = 0; c < JuegoArcade.CANTIDAD_CARRILES; c++) {

                final int carril = c;

                double[] posiciones = juego.getObstaculos().stream()
                        .filter(o -> o.getCarril() == carril)
                        .mapToDouble(Obstaculo::getY)
                        .sorted()
                        .toArray();

                for (int i = 1; i < posiciones.length; i++) {
                    double distancia = posiciones[i] - (posiciones[i - 1] + JuegoArcade.ALTO_OBSTACULO);
                    assertTrue(distancia >= 100.0,
                            "En carril " + carril + ", obstáculos demasiado cerca: "
                                    + distancia + " unidades (mínimo 100).");
                }
            }
        }
    }

    @Test
    void noHayObstaculosAdyacentesDemasiadoCercos() {

        JuegoArcade juego = juegoConSemilla(31L);

        for (int paso = 0; paso < 500 && !juego.isGameOver(); paso++) {

            juego.avanzar();

            List<Obstaculo> lista = juego.getObstaculos();

            for (int i = 0; i < lista.size(); i++) {
                for (int j = i + 1; j < lista.size(); j++) {

                    Obstaculo a = lista.get(i);
                    Obstaculo b = lista.get(j);

                    if (Math.abs(a.getCarril() - b.getCarril()) == 1) {

                        double distanciaVertical = Math.abs(a.getY() - b.getY());

                        assertTrue(distanciaVertical >= 40.0,
                                "Obstaculos en carriles " + a.getCarril() + " y " + b.getCarril()
                                        + " demasiado cerca verticalmente: "
                                        + distanciaVertical + " unidades (minimo 40).");
                    }
                }
            }
        }
    }
}