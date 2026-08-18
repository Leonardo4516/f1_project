package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.ConfiguracionCarrera;
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana de simulación de carreras (adaptador de entrada en Swing).
 * Permite configurar la carrera (circuito, vehículo, clima, compuesto de
 * neumáticos y vueltas), ver la parrilla de salida tras una clasificación
 * y correr una carrera de un vehículo con telemetría en tiempo real.
 */
public class VentanaSimulacion extends JFrame {

    // Servicios inyectados.
    private final CircuitoServicio circuitoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;

    // Desplegables para elegir circuito, vehículo y condiciones.
    private JComboBox<Circuito> comboCircuitos;
    private JComboBox<Vehiculo> comboVehiculos;
    private JComboBox<String> comboClima;
    private JComboBox<CompuestoNeumatico> comboCompuesto;
    private JComboBox<Integer> comboVueltas;

    // Botones de acción.
    private JButton btnIniciar;
    private JButton btnClasificacion;

    // Barra de progreso que muestra el avance de la carrera.
    private JProgressBar barProgreso;

    // Área de texto donde se muestran los resultados de cada vuelta.
    private JTextArea areaTexto;

    // Parrilla de salida calculada en la clasificación (para futuras carreras).
    private List<Vehiculo> parrillaActual;

    /**
     * Constructor de la ventana. Recibe los servicios y valida que no sean nulos.
     */
    public VentanaSimulacion(CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        if (circuitoServicio == null || vehiculoServicio == null || simulacionService == null) {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        this.circuitoServicio = circuitoServicio;
        this.vehiculoServicio = vehiculoServicio;
        this.simulacionService = simulacionService;

        // Configuración básica de la ventana.
        setTitle("Simulador de Fórmula 1");
        setSize(700, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera con el título. -----
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        cabecera.add(TemaF1.titulo("Simulador de Fórmula 1"));
        add(cabecera, BorderLayout.NORTH);

        // ----- Zona central: progreso arriba + telemetría debajo. -----
        JPanel telemetria = new JPanel(new BorderLayout());

        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Configura tu carrera y presiona Iniciar");
        telemetria.add(barProgreso, BorderLayout.NORTH);

        areaTexto = new JTextArea(14, 46);
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        TemaF1.conBorde(areaTexto);
        telemetria.add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        add(telemetria, BorderLayout.CENTER);

        // ----- Zona sur: configuración y botones. -----
        JPanel configuracion = new JPanel();
        configuracion.setLayout(new BoxLayout(configuracion, BoxLayout.Y_AXIS));
        configuracion.setBorder(TemaF1.margenes(4, 12, 16, 16));
        configuracion.add(construirFilaSeleccion());
        configuracion.add(Box.createVerticalStrut(8));
        configuracion.add(construirFilaEstrategia());
        add(configuracion, BorderLayout.SOUTH);

        // ---- Acciones. ----
        conectarAcciones();

        // Mensaje de bienvenida y carga inicial de la tabla de selección.
        areaTexto.append("Bienvenido al simulador.\n");
        areaTexto.append("1. Elige circuito y vehículo.\n");
        areaTexto.append("2. Define clima, compuesto y vueltas.\n");
        areaTexto.append("3. Ejecuta la clasificación para ver la parrilla.\n");
        areaTexto.append("4. Presiona Iniciar carrera.\n\n");

    }

    /** Construye la primera fila: selección de circuito y vehículo. */
    private JPanel construirFilaSeleccion() {

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        comboCircuitos = new JComboBox<>();
        for (Circuito circuito : circuitoServicio.listarCircuitos()) {
            comboCircuitos.addItem(circuito);
        }

        comboVehiculos = new JComboBox<>();
        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {
            comboVehiculos.addItem(vehiculo);
        }

        btnIniciar = new JButton("Iniciar carrera");
        TemaF1.estilizarBoton(btnIniciar);

        fila.add(TemaF1.etiqueta("Circuito:"));
        fila.add(comboCircuitos);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(TemaF1.etiqueta("Vehículo:"));
        fila.add(comboVehiculos);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(btnIniciar);

        return fila;
    }

    /** Construye la segunda fila: condiciones de la carrera y clasificación. */
    private JPanel construirFilaEstrategia() {

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        comboClima = new JComboBox<>(new String[]{ConfiguracionCarrera.CLIMA_AUTO, ConfiguracionCarrera.CLIMA_SECO, ConfiguracionCarrera.CLIMA_LLUVIA});
        comboClima.setSelectedItem(ConfiguracionCarrera.CLIMA_AUTO);

        comboCompuesto = new JComboBox<>(CompuestoNeumatico.values());
        comboCompuesto.setSelectedItem(CompuestoNeumatico.MEDIO);

        comboVueltas = new JComboBox<>(new Integer[]{3, 5, 10, 20, 40});
        comboVueltas.setSelectedItem(10);

        btnClasificacion = new JButton("Clasificación (parrilla)");
        TemaF1.estilizarBoton(btnClasificacion);

        fila.add(TemaF1.etiqueta("Clima:"));
        fila.add(comboClima);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(TemaF1.etiqueta("Compuesto:"));
        fila.add(comboCompuesto);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(TemaF1.etiqueta("Vueltas:"));
        fila.add(comboVueltas);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(btnClasificacion);

        return fila;
    }

    /** Conecta las acciones de los botones de la ventana. */
    private void conectarAcciones() {

        // Clasificación: un vuelta lanzada por cada vehículo y parrilla ordenada.
        btnClasificacion.addActionListener(e -> {

            Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
            List<Vehiculo> vehiculos = vehiculoServicio.listarVehiculos();

            if (circuito == null || vehiculos.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Registra circuitos y vehículos antes de clasificar.");
                return;

            }

            try {

                String clima = simulacionService.resolverClima(circuito, (String) comboClima.getSelectedItem());
                parrillaActual = simulacionService.simularClasificacion(vehiculos, circuito, clima);

                areaTexto.append("=== CLASIFICACIÓN (" + circuito.getNombre() + ") - Clima: " + clima + " ===\n");

                for (int i = 0; i < parrillaActual.size(); i++) {

                    Vehiculo v = parrillaActual.get(i);
                    areaTexto.append(String.format("%2d. %-16s (%s)\n", i + 1, v.getPiloto().getNombre(), v.getMarcaEscuderia()));

                }

                areaTexto.append("\n");

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error en la clasificación: " + ex.getMessage(),
                        "Clasificación", JOptionPane.ERROR_MESSAGE);

            }
        });

        // Iniciar carrera: simula las vueltas configuradas con el compuesto elegido.
        btnIniciar.addActionListener(e -> {

            Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
            Vehiculo vehiculo = (Vehiculo) comboVehiculos.getSelectedItem();

            if (circuito == null || vehiculo == null) {

                JOptionPane.showMessageDialog(this, "No hay circuitos o vehículos registrados.");
                return;

            }

            // Construye la configuración desde los desplegables.
            ConfiguracionCarrera config = new ConfiguracionCarrera(
                    (Integer) comboVueltas.getSelectedItem(),
                    (String) comboClima.getSelectedItem(),
                    (CompuestoNeumatico) comboCompuesto.getSelectedItem());

            // La simulación corre en otro hilo para no congelar la interfaz.
            new Thread(() -> {

                try {

                    // Consulta el clima: automático usa la API, si no el forzado.
                    String clima = simulacionService.resolverClima(circuito, config.getClima());
                    CompuestoNeumatico compuesto = config.getCompuesto();

                    SwingUtilities.invokeLater(() -> {

                        areaTexto.append("=== CARRERA: " + circuito.getNombre() + " (" + circuito.getUbicacion() + ") ===\n");
                        areaTexto.append("Clima: " + clima + " | Compuesto: " + compuesto.getEtiqueta() + " | Vueltas: " + config.getVueltas() + "\n\n");
                        barProgreso.setValue(0);
                        barProgreso.setString("Carrera en curso...");

                    });

                    int vueltasTotales = config.getVueltas();

                    for (int vuelta = 1; vuelta <= vueltasTotales; vuelta++) {

                        int container = vuelta;

                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo, circuito, clima, compuesto);

                        double desgasteActual = vehiculo.getDesgasteNeumaticos();

                        SwingUtilities.invokeLater(() -> {

                            areaTexto.append(String.format("Vuelta %2d - Tiempo: %5.1f s | Desgaste: %4.1f%%\n",
                                    container, tiempoVuelta, desgasteActual));

                            barProgreso.setValue(container * 100 / vueltasTotales);

                            if (container == vueltasTotales) {

                                barProgreso.setString("Carrera finalizada");

                            } else {

                                barProgreso.setString("Vuelta " + container + " de " + vueltasTotales);

                            }
                        });

                        Thread.sleep(1000);

                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                }

            }).start();

        });

    }

}