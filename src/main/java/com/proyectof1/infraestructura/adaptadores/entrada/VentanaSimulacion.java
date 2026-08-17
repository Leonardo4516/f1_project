package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.proyectof1.aplicacion.servicios.SimulacionService;

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
            areaTexto.append("Gonorrea, se agregó esto en el area de texto.\n");
        });

    }

}
