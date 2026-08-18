package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

/**
 * Adaptador de salida (infraestructura) que implementa CircuitosRepositorio
 * con persistencia en un archivo JSON. Los circuitos se cargan al construirse
 * y se guardan en disco cada vez que se modifican.
 */
public class CircuitosRepositorioJson implements CircuitosRepositorio {

    // Ubicación por defecto de los datos.
    private static final Path ARCHIVO_DEFAULT = Path.of("data", "circuitos.json");

    // Archivo concreto donde se persiste la información.
    private final Path archivo;

    // Estructura en memoria: nombre del circuito como clave, Circuito como valor.
    private final Map<String, Circuito> circuitos = new HashMap<>();

    /** Usa la ubicación por defecto (carpeta data/ del proyecto). */
    public CircuitosRepositorioJson() {
        this(ARCHIVO_DEFAULT);
    }

    /**
     * Permite indicar un archivo distinto (útil en pruebas unitarias).
     * Carga en memoria todo lo que ya existía persistido.
     */
    CircuitosRepositorioJson(Path archivo) {

        this.archivo = archivo;

        for (CircuitoDto dto : UtilJson.leer(archivo, CircuitoDto.class)) {

            circuitos.put(dto.nombre(), dto.aDominio());

        }
    }

    /** Inserta o actualiza un circuito y guarda el cambio en disco. */
    @Override
    public void guardar(Circuito circuito) {

        circuitos.put(circuito.getNombre(), circuito);
        persistir();

    }

    /** Devuelve una copia de la lista de todos los circuitos. */
    @Override
    public List<Circuito> listarTodos() {

        return new ArrayList<>(circuitos.values());

    }

    /** Obtiene un circuito por nombre o null si no existe. */
    @Override
    public Circuito buscarPorNombre(String nombre) {

        return circuitos.get(nombre);

    }

    /** Devuelve los circuitos cuya ubicación coincide (ignorando mayúsculas). */
    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        List<Circuito> resultados = new ArrayList<>();

        for (Circuito circuito : circuitos.values()) {

            if (circuito.getUbicacion().equalsIgnoreCase(ubicacion)) {

                resultados.add(circuito);

            }
        }

        return resultados;
    }

    /** Elimina un circuito por nombre. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorNombre(String nombre) {

        boolean eliminado = circuitos.remove(nombre) != null;

        if (eliminado) {

            persistir();

        }

        return eliminado;
    }

    /** Vuelca el estado actual de la memoria al archivo JSON. */
    private void persistir() {

        List<CircuitoDto> dtos = new ArrayList<>();

        for (Circuito circuito : circuitos.values()) {

            dtos.add(CircuitoDto.desdeDominio(circuito));

        }

        UtilJson.escribir(archivo, dtos);
    }

    /**
     * Representación plana de Circuito para guardar en JSON.
     * Mantiene el dominio libre de anotaciones de serialización.
     */
    private record CircuitoDto(String nombre, double kilometros, String ubicacion) {

        Circuito aDominio() {
            return new Circuito(nombre, kilometros, ubicacion);
        }

        static CircuitoDto desdeDominio(Circuito circuito) {
            return new CircuitoDto(circuito.getNombre(), circuito.getKilometros(), circuito.getUbicacion());
        }
    }
}