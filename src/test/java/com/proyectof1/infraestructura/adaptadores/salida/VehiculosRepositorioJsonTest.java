package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el repositorio de vehículos con persistencia en JSON.
 * Verifica que la referencia al piloto se guarde por nombre y se
 * reconstruya correctamente al volver a cargar desde el disco.
 */
class VehiculosRepositorioJsonTest {

    @TempDir
    Path tempDir;

    @Test
    void guardarYRecargarResuelveAlPiloto() {

        Path archivoPilotos = tempDir.resolve("pilotos.json");
        Path archivoVehiculos = tempDir.resolve("vehiculos.json");

        PilotosRepositorioJson pilotosRepo = new PilotosRepositorioJson(archivoPilotos);
        Piloto verstappen = new Piloto("Max Verstappen", 97, 92);
        pilotosRepo.guardar(verstappen);

        VehiculosRepositorioJson repo = new VehiculosRepositorioJson(archivoVehiculos, pilotosRepo);
        repo.guardar(new Vehiculo("Red Bull", 340, 0.0, verstappen));

        // Al recargar, el piloto debe recuperarse por nombre.
        VehiculosRepositorioJson recargado = new VehiculosRepositorioJson(archivoVehiculos, pilotosRepo);

        Vehiculo cargado = recargado.buscarPorEscuderia("Red Bull");
        assertEquals("Max Verstappen", cargado.getPiloto().getNombre());
        assertEquals(340, cargado.getVelocidadMaxima());

    }

    @Test
    void vehiculoSinPilotoRegistradoSeOmite() {

        PilotosRepositorioJson pilotosVacios = new PilotosRepositorioJson(tempDir.resolve("sin_pilotos.json"));
        Path archivoVehiculos = tempDir.resolve("vehiculos.json");

        // Primero guardamos un vehículo existiendo el piloto.
        Piloto piloto = new Piloto("Lando Norris", 91, 84);
        pilotosVacios.guardar(piloto);

        VehiculosRepositorioJson repo = new VehiculosRepositorioJson(archivoVehiculos, pilotosVacios);
        repo.guardar(new Vehiculo("McLaren", 339, 0.0, piloto));

        // Luego al recargar su eliminamos al piloto, el vehículo se ignora.
        pilotosVacios.eliminarPorNombre("Lando Norris");

        VehiculosRepositorioJson recargado = new VehiculosRepositorioJson(archivoVehiculos, pilotosVacios);

        assertTrue(recargado.listarTodos().isEmpty());

    }
}