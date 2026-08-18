package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana de simulación de carreras (adaptador de entrada en Swing).
 * Permite elegir un circuito y un vehículo, y simula una carrera de 5 vueltas
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

        if (circuitoServicio != null && vehiculoServicio != null && simulacionService != null) {

            this.circuitoServicio = circuitoServicio;
            this.vehiculoServicio = vehiculoServicio;
            this.simulacionService = simulacionService;

        } else {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        // Configuración básica de la ventana.
        setTitle("Simulador de Fórmula 1");
        setSize(520, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        // Se llenan los desplegables con los circuitos y vehículos registrados.
        comboCircuitos = new JComboBox<>();
        for (Circuito circuito : circuitoServicio.listarCircuitos()) {

            comboCircuitos.addItem(circuito);

        }

        comboVehiculos = new JComboBox<>();
        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {

            comboVehiculos.addItem(vehiculo);

        }

        btnIniciar = new JButton("Iniciar carrera");

        // Barra de progreso de 0 a 100 con el texto inicial.
        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Elige circuito y vehículo, luego presiona Iniciar");

        areaTexto = new JTextArea(12, 40);

        add(new JLabel("Circuito:"));
        add(comboCircuitos);
        add(new JLabel("Vehículo:"));
        add(comboVehiculos);
        add(btnIniciar);
        add(barProgreso);
        add(areaTexto);

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

                    int vueltasTotales = 5;

                    // Simula cada una de las 5 vueltas.
                    for (int vuelta = 1; vuelta <= vueltasTotales; vuelta++) {

                        // La variable vuelta debe ser efectivamente final para usarla en la lambda.
                        int container = vuelta;

                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo, circuito, climaConsulta);

                        double desgasteActual = vehiculo.getDesgasteNeumaticos();

                        // Actualiza el área de texto y la barra de progreso en el hilo de Swing.
                        SwingUtilities.invokeLater(() -> {

                            areaTexto.append("Vuelta " + container + " - Tiempo: " + ((int)tiempoVuelta) + " s | Desgaste: " + desgasteActual + "%\n");

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