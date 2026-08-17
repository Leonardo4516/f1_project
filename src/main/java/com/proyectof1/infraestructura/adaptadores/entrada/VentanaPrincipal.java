package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.SimulacionService;

public class VentanaPrincipal extends JFrame {

    private final CircuitoServicio circuitoServicio;

    private final PilotoServicio pilotoServicio;

    private final VehiculoServicio vehiculoServicio;

    private final SimulacionService simulacionService;

    private JButton btnCircuitos;

    private JButton btnPilotos;

    private JButton btnVehiculos;

    private JButton btnSimulacion;

    public VentanaPrincipal(CircuitoServicio circuitoServicio, PilotoServicio pilotoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        if (circuitoServicio != null && pilotoServicio != null && vehiculoServicio != null && simulacionService != null) {

            this.circuitoServicio = circuitoServicio;

            this.pilotoServicio = pilotoServicio;

            this.vehiculoServicio = vehiculoServicio;

            this.simulacionService = simulacionService;

        } else {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        setTitle("Administración de Fórmula 1");

        setSize(350, 220);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new FlowLayout());

        btnCircuitos = new JButton("Circuitos");

        btnPilotos = new JButton("Pilotos");

        btnVehiculos = new JButton("Vehículos");

        btnSimulacion = new JButton("Simulación");

        add(btnCircuitos);

        add(btnPilotos);

        add(btnVehiculos);

        add(btnSimulacion);

    }

}