package com.proyectof1.infraestructura.adaptadores.entrada;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class VentanaSimulacion extends JFrame {

    private JTextField txtUbicacion;

    private JButton btnIniciar;

    private JProgressBar barProgreso;

    private JTextArea areaTexto;

    public VentanaSimulacion(){

        setTitle("Simulador de Fórmula 1");

        setSize(500, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        txtUbicacion = new JTextField(15);

        btnIniciar = new JButton("Iniciar carrera");

        barProgreso = new JProgressBar(0, 100);

        areaTexto = new JTextArea(10, 30);

    }

}
