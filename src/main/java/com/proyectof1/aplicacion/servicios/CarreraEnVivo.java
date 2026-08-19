package com.proyectof1.aplicacion.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.ResultadoCarrera;
import com.proyectof1.dominio.ResultadoParticipante;
import com.proyectof1.dominio.Vehiculo;

/**
 * Motor de simulación de una carrera con varios vehículos a la vez.
 *
 * <p>La carrera avanza en pasos de tiempo simulados (método
 * {@link #avanzar(double)}). Cada vehículo tiene su propia distancia
 * recorrida, desgaste y ritmo; los adelantamientos surgen de forma natural
 * porque quien va más rápido acumula más metros. Cada vuelta completada
 * consume neumático; en la parada estratégica planificada (o si el neumático
 * se destruye) el auto entra al pit-lane: deja de recorrer metros durante la
 * parada y sale con neumáticos nuevos. Cada vuelta conlleva además una
 * pequeña probabilidad de abandono (DNF).</p>
 *
 * <p>Es lógica pura: no crea hilos. La interfaz decide cada cuánto avanzar
 * y en qué hilo.</p>
 */
public class CarreraEnVivo {

    // Estados posibles de un participante.
    public static final String ACTIVO = "En pista";
    public static final String DNF = "DNF";
    public static final String FINALIZADO = "Finalizado";

    // Duración de una parada en boxes (incluye el paso por el pit-lane) en segundos.
    private static final double DURACION_PARADA = 28.0;

    // Velocidad de seguridad del pit-lane (km/h) mientras el auto está parado.
    private static final double VELOCIDAD_CARRIL_PITS = 80.0;

    // Un compuesto por defecto cuando no se especifica uno por vehículo.
    private final SimulacionService simulacion;
    private final Circuito circuito;
    private final String clima;
    private final int vueltasTotales;

    // Estado interno de cada vehículo durante la carrera.
    private final List<AutoEnCarrera> autos;

    // Registro de sucesos sueltos (paradas, abandonos, vueltas rápidas).
    private final List<String> eventos;

    // Suerte para los abandonos (aleatorio por vehículo, reproducible).
    private final Random azar;

    // Tiempo simulador ya transcurrido.
    private double tiempoCarrera;

    // Vuelta rápida global de la carrera.
    private double mejorVueltaCarrera = Double.MAX_VALUE;
    private Vehiculo autorMejorVuelta;

    private boolean finalizada;

    /**
     * Crea una carrera nueva.
     *
     * @param parrilla   Vehículos en orden de salida (P1 primero).
     * @param circuito   Circuito donde se corre.
     * @param clima      Clima efectivo ("Lluvia" o "Seco").
     * @param compuestos Mapa vehículo -> compuesto montado. Si llega vacío,
     *                   todos usan el compuesto medio.
     * @param vueltas    Total de vueltas de la carrera.
     * @throws IllegalArgumentException si la parrilla o el circuito son inválidos.
     */
    public CarreraEnVivo(List<Vehiculo> parrilla, Circuito circuito, String clima,
            Map<Vehiculo, CompuestoNeumatico> compuestos, int vueltas) {

        if (parrilla == null || parrilla.isEmpty()) {

            throw new IllegalArgumentException("La parrilla de salida no puede estar vacía.");

        }

        if (circuito == null) {

            throw new IllegalArgumentException("El circuito no puede ser nulo.");

        }

        if (vueltas < 1) {

            throw new IllegalArgumentException("La carrera debe tener al menos una vuelta.");

        }

        this.simulacion = new SimulacionService(ubicacion -> clima == null ? "Seco" : clima);
        this.circuito = circuito;
        this.clima = clima == null ? "Seco" : clima;
        this.vueltasTotales = vueltas;
        this.azar = new Random(42L); // semilla fija para simular siempre igual en la demo
        this.eventos = new ArrayList<>();
        this.autos = new ArrayList<>();

        for (Vehiculo vehiculo : parrilla) {

            CompuestoNeumatico compuesto = compuestos == null ? null : compuestos.get(vehiculo);

            if (compuesto == null) {

                compuesto = CompuestoNeumatico.MEDIO;

            }

            autos.add(new AutoEnCarrera(vehiculo, compuesto));

        }

        // Cada auto planifica sus paradas estratégicas (misma semilla -> reproducible).
        for (AutoEnCarrera auto : autos) {

            auto.planificarParadas(vueltas, azar);

        }
    }

    /** Avanza la carrera la cantidad de segundos simulados indicada. */
    public void avanzar(double segundos) {

        if (segundos <= 0) {

            return;

        }

        if (finalizada) {

            return;

        }

        double kmPorVuelta = circuito.getKilometros();

        for (AutoEnCarrera auto : autos) {

            if (auto.isDnf()) {

                continue;

            }

            // Mientras está en boxes no recorre metros: solo se descuenta el
            // tiempo de parada y, al salir, se registra el evento de salida.
            if (auto.estaEnPits()) {

                auto.avanzarEnPits(segundos);

                if (auto.estaEnPits()) {

                    continue;

                }

                eventos.add("Salida de boxes: " + auto.getVehiculo().getMarcaEscuderia()
                        + " (" + auto.getVehiculo().getPiloto().getNombre() + ")");
                continue;

            }

            // Ritmo actual con el desgaste que lleva el auto en este momento.
            double tiempoVuelta = simulacion.proyectarVuelta(
                    auto.getVehiculo(), circuito, clima, auto.getCompuesto(), auto.getDesgaste());

            // Velocidad media equivalente en km/h y metros recorridos en el paso.
            double velocidad = kmPorVuelta / (tiempoVuelta / 3600.0);
            auto.setVelocidadActual(velocidad);

            auto.acumularDistancia(velocidad * segundos / 3600.0);

            // Completar tantas vueltas como hayan sido superadas en este paso.
            while (auto.getDistanciaKm() >= (auto.getVueltasCompletadas() + 1) * kmPorVuelta) {

                cerrarVuelta(auto, tiempoVuelta);

            }
        }

        tiempoCarrera += segundos;

        // La carrera termina cuando el líder cruza la bandera a cuadros.
        AutoEnCarrera lider = ranking().get(0);

        if (lider.getVueltasCompletadas() >= vueltasTotales) {

            finalizada = true;

        }
    }

    /** Cierra una vuelta completada por un auto: tiempos, desgaste, paradas y DNF. */
    private void cerrarVuelta(AutoEnCarrera auto, double tiempoVuelta) {

        auto.completarVuelta(tiempoVuelta);

        // Se actualiza la vuelta rápida global de la carrera.
        if (tiempoVuelta < mejorVueltaCarrera) {

            mejorVueltaCarrera = tiempoVuelta;
            autorMejorVuelta = auto.getVehiculo();

            eventos.add("Vuelta rápida:  *" + auto.getVehiculo().getPiloto().getNombre()
                    + "*  (" + String.format("%.2f s)", tiempoVuelta));

        }

        // El compuesto consume desgaste en cada vuelta completada.
        auto.aplicarDesgaste(auto.getCompuesto().getDesgastePorVuelta());

        // Entrada a boxes por estrategia planificada o por neumático destrozado.
        // La parada es un proceso real: el auto deja de recorrer metros unos
        // segundos y sale con neumáticos nuevos.
        boolean paradaEstrategica = auto.debePararAhora();

        if (paradaEstrategica || auto.getDesgaste() > 85.0) {

            auto.iniciarParada(DURACION_PARADA, paradaEstrategica);
            eventos.add("Parada en boxes: " + auto.getVehiculo().getMarcaEscuderia()
                    + " (" + auto.getVehiculo().getPiloto().getNombre() + ")");

        }

        // Probabilidad de abandono: baja en condiciones normales y alta con
        // los neumáticos destrozados por el uso prolongado.
        double probabilidad = auto.getDesgaste() > 90.0 ? 0.12 : 0.004;

        if (azar.nextDouble() < probabilidad) {

            auto.marcarDnf();
            eventos.add("ABANDONO: " + auto.getVehiculo().getMarcaEscuderia()
                    + " (" + auto.getVehiculo().getPiloto().getNombre() + ")");

        }
    }

    /**
     * Devuelve la clasificación en vivo, ordenada de más a menos distancia
     * recorrida (a más metros, mejor posición).
     */
    public List<AutoEnCarrera> ranking() {

        List<AutoEnCarrera> copia = new ArrayList<>(autos);

        copia.sort(Comparator
                .comparingDouble(AutoEnCarrera::getDistanciaKm).reversed()
                .thenComparingDouble(AutoEnCarrera::getTiempoTotal));

        return copia;

    }

    /** Diferencia en segundos de un auto respecto al líder. */
    public double gapAlLider(AutoEnCarrera auto) {

        AutoEnCarrera lider = ranking().get(0);

        if (auto == lider) {

            return 0.0;

        }

        double kmDiferencia = lider.getDistanciaKm() - auto.getDistanciaKm();
        double velocidadLider = Math.max(lider.getVelocidadActual(), 60.0);

        return kmDiferencia / velocidadLider * 3600.0;

    }

    /** Devuelve una copia de los eventos registrados hasta el momento. */
    public List<String> getEventos() {

        return new ArrayList<>(eventos);

    }

    /** Porcentaje de avance del líder respecto al total de la carrera (0-100). */
    public double progresoPorcentaje() {

        double total = vueltasTotales * circuito.getKilometros();
        double lider = ranking().get(0).getDistanciaKm();

        return Math.min(100.0, lider / total * 100.0);

    }

    /** Vuelta actual del líder (1-based). */
    public int vueltaDelLider() {

        return Math.min(vueltasTotales, ranking().get(0).getVueltasCompletadas() + 1);

    }

    public boolean estaFinalizada() {
        return finalizada;
    }

    public double getTiempoCarrera() {
        return tiempoCarrera;
    }

    public int getVueltasTotales() {
        return vueltasTotales;
    }

    public Circuito getCircuito() {
        return circuito;
    }

    public String getClima() {
        return clima;
    }

    public String getNombreLider() {

        AutoEnCarrera lider = ranking().get(0);

        return lider.getVehiculo().getMarcaEscuderia() + " (" + lider.getVehiculo().getPiloto().getNombre() + ")";

    }

    /**
     * Genera el resultado definitivo de la carrera cuando ya terminó.
     * Se calcula en el momento para proyectos futuros (historial), por eso
     * se puede pedir en cualquier instante.
     */
    public ResultadoCarrera resultadoFinal() {

        List<ResultadoParticipante> participantes = new ArrayList<>();

        int posicion = 1;

        for (AutoEnCarrera auto : ranking()) {

            boolean esVueltaRapida = auto.getVehiculo() == autorMejorVuelta;
            String estado = auto.isDnf() ? DNF : FINALIZADO;

            participantes.add(new ResultadoParticipante(
                    posicion++,
                    auto.getVehiculo(),
                    estado,
                    auto.getTiempoTotal(),
                    auto.getMejorVuelta() == Double.MAX_VALUE ? 0.0 : auto.getMejorVuelta(),
                    auto.getVueltasCompletadas(),
                    auto.getParadas(),
                    esVueltaRapida));

        }

        return new ResultadoCarrera(participantes, circuito, clima, vueltasTotales);

    }

    /** Estado de un vehículo durante la carrera. */
    public static class AutoEnCarrera {

        private final Vehiculo vehiculo;
        private final CompuestoNeumatico compuesto;

        // Metros reales sobre el trazado (acumulado en kilómetros).
        private double distanciaKm;
        private int vueltasCompletadas;
        private double desgaste;

        private double tiempoTotal;
        private double velocidadActualKmh;
        private double horaUltimaVuelta;
        private double mejorVuelta = Double.MAX_VALUE;
        private int paradas;
        private boolean dnf;

        // Paradas estratégicas planificadas al inicio (vuelta 1-based en la que paran).
        private final List<Integer> paradasPlanificadas = new ArrayList<>();
        private int indiceParadaPlanificada;

        // Tiempo restante dentro del pit-lane (0 = en pista).
        private double segundosEnPits;

        AutoEnCarrera(Vehiculo vehiculo, CompuestoNeumatico compuesto) {

            this.vehiculo = vehiculo;
            this.compuesto = compuesto;
            this.desgaste = vehiculo.getDesgasteNeumaticos();

        }

        public Vehiculo getVehiculo() {
            return vehiculo;
        }

        public CompuestoNeumatico getCompuesto() {
            return compuesto;
        }

        public double getDistanciaKm() {
            return distanciaKm;
        }

        public int getVueltasCompletadas() {
            return vueltasCompletadas;
        }

        public double getDesgaste() {
            return desgaste;
        }

        public double getTiempoTotal() {
            return tiempoTotal;
        }

        public double getVelocidadActual() {
            return velocidadActualKmh;
        }

        public double getHoraUltimaVuelta() {
            return horaUltimaVuelta;
        }

        public double getMejorVuelta() {
            return mejorVuelta;
        }

        public int getParadas() {
            return paradas;
        }

        public boolean isDnf() {
            return dnf;
        }

        public String getEstado() {
            return dnf ? DNF : FINALIZADO;
        }

        void acumularDistancia(double kilometros) {
            distanciaKm += kilometros;
        }

        void setVelocidadActual(double velocidadActualKmh) {
            this.velocidadActualKmh = velocidadActualKmh;
        }

        void completarVuelta(double tiempoVuelta) {

            vueltasCompletadas++;
            tiempoTotal += tiempoVuelta;
            horaUltimaVuelta = tiempoVuelta;

            if (tiempoVuelta < mejorVuelta) {

                mejorVuelta = tiempoVuelta;

            }
        }

        void aplicarDesgaste(double cantidad) {

            desgaste = Math.min(100.0, desgaste + cantidad);

        }

        /** Planifica las paradas estratégicas: en carreras largas se hacen dos. */
        void planificarParadas(int vueltasTotales, Random azar) {

            int numeroParadas = vueltasTotales >= 30 ? 2 : 1;

            for (int i = 0; i < numeroParadas; i++) {

                // Ventana de parada alrededor del ecuador de la carrera (0.4-0.7).
                double fraccion = 0.4 + 0.3 * azar.nextDouble();
                paradasPlanificadas.add(Math.max(1, (int) Math.round(vueltasTotales * fraccion)));

            }

            Collections.sort(paradasPlanificadas);

        }

        /** Indica si le toca entrar a boxes por su próxima parada estratégica. */
        boolean debePararAhora() {

            return indiceParadaPlanificada < paradasPlanificadas.size()
                    && vueltasCompletadas >= paradasPlanificadas.get(indiceParadaPlanificada);

        }

        /** El auto entra al pit-lane: deja de recorrer metros hasta terminar la parada. */
        void iniciarParada(double duracion, boolean estrategica) {

            if (estrategica) {

                indiceParadaPlanificada++;

            }

            paradas++;
            segundosEnPits = duracion;
            velocidadActualKmh = VELOCIDAD_CARRIL_PITS;

        }

        /** Descuenta el tiempo transcurrido en el pit-lane; al llegar a 0, sale. */
        void avanzarEnPits(double segundos) {

            segundosEnPits -= segundos;

            if (segundosEnPits <= 0) {

                segundosEnPits = 0.0;
                desgaste = 0.0;

            }
        }

        /** true mientras el auto esté dentro del pit-lane. */
        public boolean estaEnPits() {
            return segundosEnPits > 0;
        }

        void marcarDnf() {
            dnf = true;
        }
    }

}