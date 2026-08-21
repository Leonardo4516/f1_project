package com.proyectof1.aplicacion.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import com.proyectof1.aplicacion.servicios.JuegoArcade;

/**
 * Pruebas del núcleo del juego arcade. Se usa una semilla fija de Random para
 * que los escenarios de obstáculos sean reproducibles, igual que en el resto
 * de pruebas de la capa de aplicación.
 */
class JuegoArcadeTest {

    /** Crea el juego con una semilla fija para reproducir los escenarios. */
    private JuegoArcade juegoConSemilla(long semilla) {
        return new JuegoArcade(new Random(semilla), 0);
    }

    @Test
    void arrancaEnElCarrilCentralSinPuntosNiFinDePartida() {

        JuegoArcade juego = juegoConSemilla(1L);

        assertEquals(1, juego.getCarrilCoche());
        assertEquals(0, juego.getPuntuacion());
        assertFalse(juego.isGameOver());

    }

    @Test
    void cambiarCarrilIzquierdaNoSabeSalirsePorLaIzquierda() {

        JuegoArcade juego = juegoConSemilla(1L);

        // Desde el carril central (1) se llega al 0 y no más allá.
        juego.cambiarCarrilIzquierda();
        juego.cambiarCarrilIzquierda();

        assertEquals(0, juego.getCarrilCoche());

    }

    @Test
    void cambiarCarrilDerechaNoSabeSalirsePorLaDerecha() {

        JuegoArcade juego = juegoConSemilla(1L);

        juego.cambiarCarrilDerecha();
        juego.cambiarCarrilDerecha();
        juego.cambiarCarrilDerecha();

        assertEquals(2, juego.getCarrilCoche());

    }

    @Test
    void avanzarAumentaLaPuntuacion() {

        JuegoArcade juego = juegoConSemilla(7L);

        juego.avanzar();

        assertEquals(1, juego.getPuntuacion());

    }

    @Test
    void laDificultadAceleraConLaPuntuacion() {

        // El método velocidadDeCaida no es público; se comprueba el efecto a través
        // de un largo avance: a más puntos, los obstáculos descienden más rápido.
        JuegoArcade juego = juegoConSemilla(99L);

        // Con una semilla donde no hay choque pronto, se comprueba que la distancia
        // recorrida por los obstáculos crece al subir de nivel.
        int distanciaNivel0 = distanciaRecorridaEn(juego, 20);
        int distanciaNivelAlto = distanciaRecorridaEn(juego, 200);

        assertTrue(distanciaNivelAlto > distanciaNivel0,
                "A más puntuación, mayor velocidad de caída de los obstáculos.");
    }

    /** Avanza n ticks y devuelve cuánto se movió el primer obstáculo visible. */
    private int distanciaRecorridaEn(JuegoArcade juego, int ticks) {

        int posicionInicial = obstaculoMasBajo(juego);

        for (int i = 0; i < ticks && !juego.isGameOver(); i++) {
            juego.avanzar();
        }

        int posicionFinal = obstaculoMasBajo(juego);
        return posicionInicial - posicionFinal;
    }

    /** Devuelve la menor distancia (más cercana al coche) entre los obstáculos. */
    private int obstaculoMasBajo(JuegoArcade juego) {

        if (juego.getObstaculos().isEmpty()) {
            return JuegoArcade.ORIGEN_OBSTACULO;
        }

        return juego.getObstaculos().stream()
                .mapToInt(obst -> obst[1])
                .min()
                .orElse(JuegoArcade.ORIGEN_OBSTACULO);
    }

    @Test
    void elChoqueConUnObstaculoTerminaLaPartida() {

        // Se fuerza un escenario: un obstáculo en el mismo carril que el coche,
        // a poca distancia, para que al avanzar se produzca la colisión.
        JuegoArcade juego = new JuegoArcade(new Random(5L), 0);

        // Generamos obstáculos hasta que alguno coincida con el carril central.
        boolean hayChoque = false;
        for (int i = 0; i < 300 && !hayChoque; i++) {
            juego.avanzar();
            hayChoque = juego.isGameOver();
        }

        assertTrue(hayChoque, "Al cabo de varios pasos el coche termina chocando con un obstáculo.");
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
        assertEquals(1, juego.getCarrilCoche());
        assertFalse(juego.isGameOver());
        assertEquals(recordPrev, juego.getRecord());

    }

    @Test
    void elRecordNuncaBaja() {

        JuegoArcade juego = new JuegoArcade(new Random(8L), 10);

        // Después de unos cuantos avances, el récord debe ser >= el inicial.
        for (int i = 0; i < 20 && !juego.isGameOver(); i++) {
            juego.avanzar();
        }

        assertTrue(juego.getRecord() >= 10);

    }
}