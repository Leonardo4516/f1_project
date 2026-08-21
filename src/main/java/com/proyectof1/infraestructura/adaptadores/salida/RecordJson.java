package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;

/**
 * Adaptador de salida (infraestructura) que persiste el récord del juego
 * arcade en un archivo JSON dentro de {@code data/}. Sigue el mismo patrón
 * que el resto de la persistencia: usa {@link UtilJson} y un DTO record plano,
 * y no detiene el programa si el archivo no existe o falla la lectura.
 */
public class RecordJson {

    // Ubicación por defecto del archivo de récord.
    private static final Path ARCHIVO_DEFAULT = Path.of("data", "record.json");

    // Archivo concreto donde se persiste el récord.
    private final Path archivo;

    /**
     * Constructor por defecto que usa la ubicación estándar.
     */
    public RecordJson() {
        this(ARCHIVO_DEFAULT);
    }

    /**
     * Permite indicar un archivo distinto (útil en pruebas unitarias).
     *
     * @param archivo Ruta del archivo de récord.
     */
    RecordJson(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Lee el récord guardado. Si no existe o falla, devuelve 0.
     *
     * @return El récord más alto alcanzado hasta ahora (0 si no hay ninguno).
     */
    public int leer() {

        var dtos = UtilJson.leer(archivo, RecordDto.class);

        if (dtos.isEmpty()) {
            return 0;
        }

        return Math.max(0, dtos.get(0).record());
    }

    /**
     * Guarda el récord si supera al actual. Devuelve true si se actualizó.
     *
     * @param puntuacion Nueva puntuación alcanzada.
     * @return true si la puntuación superó el récord previo y se persistió.
     */
    public boolean guardar(int puntuacion) {

        if (puntuacion <= leer()) {
            return false;
        }

        UtilJson.escribir(archivo, java.util.List.of(new RecordDto(puntuacion)));
        return true;

    }

    /**
     * Representación plana del récord para guardar en JSON.
     */
    private record RecordDto(int record) {
    }

}