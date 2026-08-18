package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

/**
 * Adaptador de salida (infraestructura) que implementa PilotosRepositorio
 * con persistencia en un archivo JSON. Los pilotos se cargan al construirse
 * y se guardan en disco cada vez que se modifican.
 */
public class PilotosRepositorioJson implements PilotosRepositorio {

    // Ubicación por defecto de los datos.
    private static final Path ARCHIVO_DEFAULT = Path.of("data", "pilotos.json");

    // Archivo concreto donde se persiste la información.
    private final Path archivo;

    // Estructura en memoria: nombre del piloto como clave, Piloto como valor.
    private final Map<String, Piloto> pilotos = new HashMap<>();

    /** Usa la ubicación por defecto (carpeta data/ del proyecto). */
    public PilotosRepositorioJson() {
        this(ARCHIVO_DEFAULT);
    }

    /**
     * Permite indicar un archivo distinto (útil en pruebas unitarias).
     * Carga en memoria todo lo que ya existía persistido.
     */
    PilotosRepositorioJson(Path archivo) {

        this.archivo = archivo;

        for (PilotoDto dto : UtilJson.leer(archivo, PilotoDto.class)) {

            pilotos.put(dto.nombre(), dto.aDominio());

        }
    }

    /** Inserta o actualiza un piloto y guarda el cambio en disco. */
    @Override
    public void guardar(Piloto piloto) {

        pilotos.put(piloto.getNombre(), piloto);
        persistir();

    }

    /** Devuelve una copia de la lista de todos los pilotos. */
    @Override
    public List<Piloto> listarTodos() {

        return new ArrayList<>(pilotos.values());

    }

    /** Obtiene un piloto por nombre o null si no existe. */
    @Override
    public Piloto buscarPorNombre(String nombre) {

        return pilotos.get(nombre);

    }

    /** Elimina un piloto por nombre. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorNombre(String nombre) {

        boolean eliminado = pilotos.remove(nombre) != null;

        if (eliminado) {

            persistir();

        }

        return eliminado;
    }

    /** Vuelca el estado actual de la memoria al archivo JSON. */
    private void persistir() {

        List<PilotoDto> dtos = new ArrayList<>();

        for (Piloto piloto : pilotos.values()) {

            dtos.add(PilotoDto.desdeDominio(piloto));

        }

        UtilJson.escribir(archivo, dtos);
    }

    /**
     * Representación plana de Piloto para guardar en JSON.
     * Mantiene el dominio libre de anotaciones de serialización.
     */
    private record PilotoDto(String nombre, int experiencia, int habilidadLluvia) {

        Piloto aDominio() {
            return new Piloto(nombre, experiencia, habilidadLluvia);
        }

        static PilotoDto desdeDominio(Piloto piloto) {
            return new PilotoDto(piloto.getNombre(), piloto.getExperiencia(), piloto.getHabilidadLluvia());
        }
    }
}