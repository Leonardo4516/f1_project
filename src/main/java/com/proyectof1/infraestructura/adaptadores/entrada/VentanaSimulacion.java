package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

        areaTexto = new JTextArea(10, 30);

        add(txtUbicacion);

        add(btnIniciar);

        add(barProgreso);

        add(areaTexto);

        btnIniciar.addActionListener(e -> {

            new Thread(() -> {

                try {

                    String ubicacion = txtUbicacion.getText();

                    Piloto piloto1 = new Piloto("Leonardo", 90, 90);

                    Vehiculo vehiculo1 = new Vehiculo("Williams", 320, 0.0, piloto1);

                    Circuito circuito1= new Circuito("Gran Premio Especial", 5.793, ubicacion);

                    areaTexto.setText("");

                    areaTexto.append("=== INICIANDO CARRERA EN " + ubicacion + " ===\n");

                    for (int vuelta = 1; vuelta <= 5; vuelta++) {


                        double tiempoVuelta = simulacionService.simularVuelta(vehiculo1, circuito1);

                        double desgasteActual = vehiculo1.getDesgasteNeumaticos();

                        areaTexto.append("Vuelta " + vuelta + " - Tiempo: " + ((int)tiempoVuelta) + " s | Desgaste: " + desgasteActual + "%\n");

                        barProgreso.setValue(((int)desgasteActual));

                        Thread.sleep(1000);

                    }

                    
                } catch (InterruptedException ex) {

                }

            }).start();

        });

    }

}
