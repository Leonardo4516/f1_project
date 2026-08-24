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
 * Consulta el clima real de una ubicación usando Open-Meteo (gratuito, sin API key).
 * Primero geocodifica la ubicación y luego consulta el código meteorológico actual.
 */
public class ClimaHttpAdapter implements ClimaServicePort {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @Override
    public String obtenerClima(String ubicacion) {
        try {
            double[] coordenadas = geocodificar(ubicacion);
            if (coordenadas == null) {
                return "Seco";
            }
            return consultarClima(coordenadas[0], coordenadas[1]);
        } catch (Exception e) {
            System.out.println("Alerta: No se pudo consultar el clima para " + ubicacion);
            System.out.println("Detalle: " + e.getMessage());
            return "Seco";
        }
    }

    private double[] geocodificar(String ubicacion) throws Exception {
        String codificada = URLEncoder.encode(ubicacion, StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + codificada + "&count=1&language=es";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(response.body());

        JsonNode results = root.path("results");
        if (results.isMissingNode() || results.isEmpty()) {
            return null;
        }

        JsonNode first = results.get(0);
        double lat = first.path("latitude").asDouble();
        double lon = first.path("longitude").asDouble();
        return new double[]{lat, lon};
    }

    private String consultarClima(double lat, double lon) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                + "&longitude=" + lon + "&current=weather_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(response.body());

        int weatherCode = root.path("current").path("weather_code").asInt();

        // Códigos WMO: https://open-meteo.com/en/docs
        // 50-67: lluvia, 80-82: chubascos, 95-99: tormenta
        if ((weatherCode >= 50 && weatherCode <= 67) ||
            (weatherCode >= 80 && weatherCode <= 82) ||
            (weatherCode >= 95 && weatherCode <= 99)) {
            return "Lluvia";
        }
        return "Seco";
    }
}
