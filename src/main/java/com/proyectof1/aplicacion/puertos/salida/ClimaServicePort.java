package com.proyectof1.aplicacion.puertos.salida;

/**
 * Puerto de salida para la consulta del clima.
 * Permite a la capa de aplicación obtener el clima sin conocer
 * los detalles de cómo se implementa (API externa, simulación, etc.).
 * La implementación concreta la proporciona ClimaHttpAdapter.
 */
public interface ClimaServicePort {

    /**
     * Obtiene el estado del clima para una ubicación determinada.
     *
     * @param ubicacion Lugar del que se consulta el clima (ej. "Monza").
     * @return Descripción del clima ("Lluvia" o "Seco" en esta aplicación).
     */
    String obtenerClima(String ubicacion);

}