package com.proyectof1.infraestructura.adaptadores.salida;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad interna (infraestructura) para leer y escribir archivos JSON
 * con Jackson. Centraliza el manejo de errores: si algo falla, el programa
 * continúa con datos vacíos en lugar de romperse.
 */
final class UtilJson {

    private UtilJson() {
        // Clase estática, no se instancia.
    }

    // Una única instancia reutilizable: Jackson es costoso de crear y es thread-safe.
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Lee una lista de objetos JSON desde un archivo.
     * Si el archivo no existe o la lectura falla, devuelve una lista vacía.
     */
    static <T> List<T> leer(Path ruta, Class<T> tipo) {

        try {

            if (!Files.exists(ruta)) {
                return new ArrayList<>();
            }

            CollectionType tipoLista = MAPPER.getTypeFactory().constructCollectionType(List.class, tipo);
            return MAPPER.readValue(ruta.toFile(), tipoLista);

        } catch (IOException e) {

            System.out.println("No se pudo leer la persistencia: " + ruta + " - " + e.getMessage());
            return new ArrayList<>();

        }
    }

    /**
     * Escribe un objeto (normalmente una lista) como JSON con formato legible.
     * Crea la carpeta padre si es necesario y no detiene el programa si falla.
     */
    static void escribir(Path ruta, Object contenido) {

        try {

            Path padre = ruta.getParent();
            if (padre != null) {
                Files.createDirectories(padre);
            }

            MAPPER.writerWithDefaultPrettyPrinter().writeValue(ruta.toFile(), contenido);

        } catch (IOException e) {

            System.out.println("No se pudo guardar la persistencia: " + ruta + " - " + e.getMessage());

        }
    }
}