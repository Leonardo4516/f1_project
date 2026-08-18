package com.proyectof1.dominio;

/**
 * Configuración que define cómo se corre una carrera:
 * número de vueltas, condiciones climáticas (automáticas o forzadas)
 * y el compuesto de neumáticos elegido por la escudería.
 */
public class ConfiguracionCarrera {

    // Modos de clima soportados por la simulación.
    public static final String CLIMA_AUTO = "Auto";
    public static final String CLIMA_SECO = "Seco";
    public static final String CLIMA_LLUVIA = "Lluvia";

    private int vueltas;
    private String clima;
    private CompuestoNeumatico compuesto;

    /**
     * Constructor de ConfiguracionCarrera. Valida y asigna todos los atributos.
     *
     * @param vueltas   Número de vueltas de la carrera (1-200).
     * @param clima     "Auto", "Seco" o "Lluvia".
     * @param compuesto Compuesto de neumáticos elegido.
     */
    public ConfiguracionCarrera(int vueltas, String clima, CompuestoNeumatico compuesto) {

        setVueltas(vueltas);
        setClima(clima);
        setCompuesto(compuesto);

    }

    public int getVueltas() {
        return vueltas;
    }

    /**
     * Establece las vueltas solo si están en el rango 1-200.
     */
    public final void setVueltas(int vueltas) {

        if (vueltas >= 1 && vueltas <= 200) {

            this.vueltas = vueltas;

        } else {

            throw new IllegalArgumentException("El número de vueltas debe estar entre 1 y 200.");

        }
    }

    public String getClima() {
        return clima;
    }

    /**
     * Establece el clima solo si es uno de los tres modos válidos.
     */
    public final void setClima(String clima) {

        if (CLIMA_AUTO.equals(clima) || CLIMA_SECO.equals(clima) || CLIMA_LLUVIA.equals(clima)) {

            this.clima = clima;

        } else {

            throw new IllegalArgumentException("El modo de clima no es válido.");

        }
    }

    public CompuestoNeumatico getCompuesto() {
        return compuesto;
    }

    /**
     * Establece el compuesto de neumáticos solo si no es nulo.
     */
    public final void setCompuesto(CompuestoNeumatico compuesto) {

        if (compuesto != null) {

            this.compuesto = compuesto;

        } else {

            throw new IllegalArgumentException("El compuesto de neumáticos no puede ser nulo.");

        }
    }

    /**
     * Indica si el clima debe resolverse automáticamente según la API o
     * si, por el contrario, ya está forzado (seco o lluvia).
     */
    public boolean esClimaAutomatico() {

        return CLIMA_AUTO.equals(clima);

    }

}