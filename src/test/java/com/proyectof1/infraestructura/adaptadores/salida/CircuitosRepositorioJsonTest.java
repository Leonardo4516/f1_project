package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.proyectof1.dominio.Circuito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el repositorio de circuitos con persistencia en JSON,
 * en especial que los datos sobrevivan a la recreación del repositorio.
 */
class CircuitosRepositorioJsonTest {

    @TempDir
    Path tempDir;

    @Test
    void guardarYRecargarDesdeDisco() {

        Path archivo = tempDir.resolve("circuitos.json");
        CircuitosRepositorioJson repo = new CircuitosRepositorioJson(archivo);

        repo.guardar(new Circuito("Monza", 5.793, "Italia"));
        repo.guardar(new Circuito("Spa", 7.004, "Bélgica"));

        // Un nuevo repositorio sobre el mismo archivo debe recuperar los datos.
        CircuitosRepositorioJson recargado = new CircuitosRepositorioJson(archivo);

        assertEquals(2, recargado.listarTodos().size());
        assertEquals("Italia", recargado.buscarPorNombre("Monza").getUbicacion());

    }

    @Test
    void buscarPorUbicacionIgnoreCase() {

        CircuitosRepositorioJson repo = new CircuitosRepositorioJson(tempDir.resolve("ubicacion.json"));
        repo.guardar(new Circuito("Monza", 5.793, "Italia"));

        assertEquals(1, repo.buscarPorUbicacion("italia").size());

    }

    @Test
    void eliminarBorraYPersiste() {

        Path archivo = tempDir.resolve("borrar.json");
        CircuitosRepositorioJson repo = new CircuitosRepositorioJson(archivo);
        repo.guardar(new Circuito("Monza", 5.793, "Italia"));

        assertTrue(repo.eliminarPorNombre("Monza"));
        assertFalse(repo.eliminarPorNombre("Inexistente"));
        assertNull(repo.buscarPorNombre("Monza"));

        assertEquals(0, new CircuitosRepositorioJson(archivo).listarTodos().size());

    }
}