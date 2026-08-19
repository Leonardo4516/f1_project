package com.proyectof1.infraestructura.adaptadores.salida;

import com.proyectof1.aplicacion.puertos.salida.ClimaServicePort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Adaptador de salida (infraestructura) que implementa ClimaServicePort.
 * Consulta el clima real de una ubicación usando el servicio web gratuito wttr.in
 * y traduce la respuesta a los valores que entiende el simulador ("Lluvia"/"Seco").
 * Utiliza las APIs HttpClient y Jackson de Java.
 */
public class ClimaHttpAdapter implements ClimaServicePort {

    /** Consulta el clima actual de la ubicación dada. */
    @Override
    public String obtenerClima(String ubicacion) {
        // 1. Construir la URL del servicio climático wttr.in (formato JSON 'j1').
        //    Se codifica la ubicación para soportar espacios y acentos de forma segura.
        String ubicacionCodificada = URLEncoder.encode(ubicacion, StandardCharsets.UTF_8).replace("+", "%20");
        String url = "https://wttr.in/" + ubicacionCodificada + "?format=j1";

        // 2. Crear el cliente y la petición HTTP de Java
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // 3. Dado que internet puede fallar, usamos un bloque try-catch para tolerancia a fallos [1]
        try {
            // Realizar la petición web y obtener la respuesta en texto
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            // 4. Analizar el JSON de respuesta con la librería Jackson [247.4]
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            // Extraemos la descripción del clima actual: current_condition.weatherDesc.value
            String descripcionClima = rootNode.path("current_condition")
                    .get(0)
                    .path("weatherDesc")
                    .get(0)
                    .path("value")
                    .asText();

            // 5. Traducir la respuesta al lenguaje de nuestro simulador ("Lluvia" o "Seco") [381.1]
            if (descripcionClima.toLowerCase().contains("rain") ||
                descripcionClima.toLowerCase().contains("shower") ||
                descripcionClima.toLowerCase().contains("drizzle") ||
                descripcionClima.toLowerCase().contains("thunder") ||
                descripcionClima.toLowerCase().contains("storm")) {
                return "Lluvia";
            } else {
                return "Seco";
            }

        } catch (Exception e) {
            // En caso de caída de internet o error, la simulación continúa con un clima por defecto seguro [1]
            System.out.println("⚠️ Alerta de Infraestructura: No se pudo conectar a la API del clima.");
            System.out.println("Detalle del error: " + e.getMessage());
            return "Seco";
        }
    }
}