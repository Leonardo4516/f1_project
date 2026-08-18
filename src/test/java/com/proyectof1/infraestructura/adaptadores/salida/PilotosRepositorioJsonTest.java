package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.proyectof1.dominio.Piloto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el repositorio de pilotos con persistencia en JSON,
 * en especial que los datos sobrevivan a la recreación del repositorio.
 */
class PilotosRepositorioJsonTest {

    @TempDir
    Path tempDir;

    @Test
    void guardarYRecargarDesdeDisco() {

        Path archivo = tempDir.resolve("pilotos.json");
        PilotosRepositorioJson repo = new PilotosRepositorioJson(archivo);

        repo.guardar(new Piloto("Max Verstappen", 97, 92));
        repo.guardar(new Piloto("Charles Leclerc", 93, 92));

        PilotosRepositorioJson recargado = new PilotosRepositorioJson(archivo);

        assertEquals(2, recargado.listarTodos().size());
        assertEquals(97, recargado.buscarPorNombre("Max Verstappen").getExperiencia());

    }

    @Test
    void archivoVacioDevuelveListaVacia() {

        PilotosRepositorioJson repo = new PilotosRepositorioJson(tempDir.resolve("no_existe.json"));
        assertTrue(repo.listarTodos().isEmpty());

    }

    @Test
    void eliminarPersiste() {

        Path archivo = tempDir.resolve("borrar.json");
        PilotosRepositorioJson repo = new PilotosRepositorioJson(archivo);
        repo.guardar(new Piloto("Lewis Hamilton", 95, 88));

        assertTrue(repo.eliminarPorNombre("Lewis Hamilton"));
        assertNull(repo.buscarPorNombre("Lewis Hamilton"));

        assertEquals(0, new PilotosRepositorioJson(archivo).listarTodos().size());

    }
}