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

public class VentanaSimulacion extends JFrame {

    private final CircuitoServicio circuitoServicio;

    private final VehiculoServicio vehiculoServicio;

    private final SimulacionService simulacionService;

    private JComboBox<Circuito> comboCircuitos;

    private JComboBox<Vehiculo> comboVehiculos;

    private JButton btnIniciar;

    private JProgressBar barProgreso;

    private JTextArea areaTexto;

    public VentanaSimulacion(CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        if (circuitoServicio != null && vehiculoServicio != null && simulacionService != null) {

            this.circuitoServicio = circuitoServicio;

            this.vehiculoServicio = vehiculoServicio;

            this.simulacionService = simulacionService;

        } else {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        setTitle("Simulador de Fórmula 1");

        setSize(520, 420);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new FlowLayout());

        comboCircuitos = new JComboBox<>();

        for (Circuito circuito : circuitoServicio.listarCircuitos()) {

            comboCircuitos.addItem(circuito);

        }

        comboVehiculos = new JComboBox<>();

        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {

            comboVehiculos.addItem(vehiculo);

        }

        btnIniciar = new JButton("Iniciar carrera");

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

        btnIniciar.addActionListener(e -> {

            Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();

            Vehiculo vehiculo = (Vehiculo) comboVehiculos.getSelectedItem();

            if (circuito == null || vehiculo == null) {

                JOptionPane.showMessageDialog(this, "No hay circuitos o vehículos registrados.");

                return;

            }

            new Thread(() -> {

                try {

                    SwingUtilities.invokeLater(() -> {

                        areaTexto.setText("");

                        areaTexto.append("=== CARRERA: " + circuito.getNombre() + " (" + circuito.getUbicacion() + ") ===\n");

                        barProgreso.setValue(0);

                        barProgreso.setString("Carrera en curso...");

                    });

                    String climaConsulta = simulacionService.consultarClima(circuito);

                    int vueltasTotales = 5;

                    for (int vuelta = 1; vuelta <= vueltasTotales; vuelta++) {

                        int container = vuelta;

                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo, circuito, climaConsulta);

                        double desgasteActual = vehiculo.getDesgasteNeumaticos();

                        SwingUtilities.invokeLater(() -> {

                            areaTexto.append("Vuelta " + container + " - Tiempo: " + ((int)tiempoVuelta) + " s | Desgaste: " + desgasteActual + "%\n");

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