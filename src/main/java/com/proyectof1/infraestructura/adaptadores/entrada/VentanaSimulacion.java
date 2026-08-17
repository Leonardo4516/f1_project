package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public class VentanaSimulacion extends JFrame {

    private JTextField txtUbicacion;

    private JButton btnIniciar;

    private JProgressBar barProgreso;

    private JTextArea areaTexto;

    private SimulacionService simulacionService;

    public VentanaSimulacion (SimulacionService simulacionService){

        this.simulacionService = simulacionService;

        setTitle("Simulador de Fórmula 1");

        setSize(500, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new FlowLayout());

        txtUbicacion = new JTextField(15);

        btnIniciar = new JButton("Iniciar carrera");

        barProgreso = new JProgressBar(0, 100);

        barProgreso.setStringPainted(true);

        barProgreso.setString("Presiona \"Iniciar carrera\" para comenzar");

        areaTexto = new JTextArea(10, 30);

        add(txtUbicacion);

        add(btnIniciar);

        add(barProgreso);

        add(areaTexto);

        btnIniciar.addActionListener(e -> {

            String ubicacion = txtUbicacion.getText();

            new Thread(() -> {

                try {

                    Piloto piloto1 = new Piloto("Leonardo", 90, 90);

                    Vehiculo vehiculo1 = new Vehiculo("Williams", 320, 0.0, piloto1);

                    Circuito circuito1= new Circuito("Gran Premio Especial", 5.793, ubicacion);

                    SwingUtilities.invokeLater(() -> {

                        areaTexto.setText("");

                        areaTexto.append("=== INICIANDO CARRERA EN " + ubicacion + " ===\n");

                        barProgreso.setValue(0);

                        barProgreso.setString("Carrera en curso...");

                    });
                    
                    String climaConsulta = simulacionService.consultarClima(circuito1);

                    int vueltasTotales = 5;

                    for (int vuelta = 1; vuelta <= vueltasTotales; vuelta++) {

                        int container = vuelta;

                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo1, circuito1, climaConsulta);

                        double desgasteActual = vehiculo1.getDesgasteNeumaticos();

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
