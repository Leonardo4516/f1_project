package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

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
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana de simulación de carreras (adaptador de entrada en Swing).
 * Permite elegir un circuito y un vehículo, y simula una carrera de 10 vueltas
 * usando SimulacionService. La simulación corre en un hilo aparte para no
 * bloquear la interfaz, y va actualizando la barra de progreso y el área de texto.
 */
public class VentanaSimulacion extends JFrame {

    // Servicios inyectados.
    private final CircuitoServicio circuitoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;

    // Desplegables para elegir circuito y vehículo.
    private JComboBox<Circuito> comboCircuitos;
    private JComboBox<Vehiculo> comboVehiculos;

    // Botón que inicia la carrera.
    private JButton btnIniciar;

    // Barra de progreso que muestra el avance de la carrera.
    private JProgressBar barProgreso;

    // Área de texto donde se muestran los resultados de cada vuelta.
    private JTextArea areaTexto;

    /**
     * Constructor de la ventana. Recibe el servicio de circuitos, de vehículos
     * y el de simulación. Se valida que ninguno sea nulo.
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
        setSize(600, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera con el título. -----
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        cabecera.add(TemaF1.titulo("Simulador de Fórmula 1"));

        JPanel cabeceraCompleta = new JPanel(new BorderLayout());
        cabeceraCompleta.add(cabecera, BorderLayout.NORTH);
        JPanel sub = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        sub.add(TemaF1.subtitulo("Selecciona circuito y vehículo, luego presiona Iniciar carrera"));
        cabeceraCompleta.add(sub, BorderLayout.CENTER);
        add(cabeceraCompleta, BorderLayout.NORTH);

        // ----- Zona de selección (circuito + vehículo). -----
        JPanel seleccion = new JPanel();
        seleccion.setLayout(new BoxLayout(seleccion, BoxLayout.X_AXIS));
        seleccion.setBorder(TemaF1.margenes(4, 12, 16, 16));

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

        seleccion.add(TemaF1.etiqueta("Circuito:"));
        seleccion.add(Box.createHorizontalStrut(6));
        seleccion.add(comboCircuitos);
        seleccion.add(Box.createHorizontalStrut(16));
        seleccion.add(TemaF1.etiqueta("Vehículo:"));
        seleccion.add(Box.createHorizontalStrut(6));
        seleccion.add(comboVehiculos);
        seleccion.add(Box.createHorizontalStrut(16));
        seleccion.add(btnIniciar);

        add(seleccion, BorderLayout.CENTER);

        // ----- Zona inferior: barra de progreso y área de telemetría. -----
        JPanel inferior = new JPanel(new BorderLayout());

        // Barra de progreso de 0 a 100 con el texto inicial.
        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Elige circuito y vehículo, luego presiona Iniciar");
        inferior.add(barProgreso, BorderLayout.NORTH);

        areaTexto = new JTextArea(14, 40);
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        TemaF1.conBorde(areaTexto);
        inferior.add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        add(inferior, BorderLayout.SOUTH);

        // Acción del botón Iniciar: lanza la simulación en un hilo separado.
        btnIniciar.addActionListener(e -> {

            Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
            Vehiculo vehiculo = (Vehiculo) comboVehiculos.getSelectedItem();

            if (circuito == null || vehiculo == null) {

                JOptionPane.showMessageDialog(this, "No hay circuitos o vehículos registrados.");
                return;

            }

            // La simulación corre en otro hilo para no congelar la interfaz.
            new Thread(() -> {

                try {

                    // Limpia los textos e inicializa la barra (en el hilo de Swing).
                    SwingUtilities.invokeLater(() -> {

                        areaTexto.setText("");
                        areaTexto.append("=== CARRERA: " + circuito.getNombre() + " (" + circuito.getUbicacion() + ") ===\n");
                        barProgreso.setValue(0);
                        barProgreso.setString("Carrera en curso...");

                    });

                    // Consulta el clima real de la ubicación del circuito.
                    String climaConsulta = simulacionService.consultarClima(circuito);

                    int vueltasTotales = 10;

                    // Simula cada una de las vueltas.
                    for (int vuelta = 1; vuelta <= vueltasTotales; vuelta++) {

                        // La variable vuelta debe ser efectivamente final para usarla en la lambda.
                        int container = vuelta;

                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo, circuito, climaConsulta);

                        double desgasteActual = vehiculo.getDesgasteNeumaticos();

                        // Actualiza el área de texto y la barra de progreso en el hilo de Swing.
                        SwingUtilities.invokeLater(() -> {

                            areaTexto.append("Vuelta " + container + " - Tiempo: " + ((int) tiempoVuelta) + " s | Desgaste: " + desgasteActual + "%\n");

                            barProgreso.setValue(container * 100 / vueltasTotales);

                            if (container == vueltasTotales) {

                                barProgreso.setString("Carrera finalizada");

                            } else {

                                barProgreso.setString("Vuelta " + container + " de " + vueltasTotales);

                            }

                        });

                        // Pausa de 1 segundo entre vuelta y vuelta para que se aprecie el avance.
                        Thread.sleep(1000);

                    }

                } catch (InterruptedException ex) {

                    // Restaura el flag de interrupción si el hilo es interrumpido.
                    Thread.currentThread().interrupt();

                }

            }).start();

        });

    }

}