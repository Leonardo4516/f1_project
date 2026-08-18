package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.SimulacionService;

/**
 * Ventana principal del programa (adaptador de entrada en Swing).
 * Actúa como menú: ofrece botones para abrir las ventanas de gestión
 * de circuitos, pilotos, vehículos y la simulación de carreras.
 */
public class VentanaPrincipal extends JFrame {

    // Servicios inyectados desde Main para compartirlos con las demás ventanas.
    private final CircuitoServicio circuitoServicio;
    private final PilotoServicio pilotoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;

    // Botones del menú.
    private JButton btnCircuitos;
    private JButton btnPilotos;
    private JButton btnVehiculos;
    private JButton btnSimulacion;

    /**
     * Constructor de la ventana principal. Recibe los servicios ya construidos.
     * Se valida que ninguno sea nulo.
     */
    public VentanaPrincipal(CircuitoServicio circuitoServicio, PilotoServicio pilotoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        if (circuitoServicio != null && pilotoServicio != null && vehiculoServicio != null && simulacionService != null) {

            this.circuitoServicio = circuitoServicio;
            this.pilotoServicio = pilotoServicio;
            this.vehiculoServicio = vehiculoServicio;
            this.simulacionService = simulacionService;

        } else {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        // Configuración básica de la ventana.
        setTitle("Administración de Fórmula 1");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación entera.
        setLocationRelativeTo(null);                     // Centra la ventana.
        setLayout(new FlowLayout());                     // Los botones se colocan en línea.

        // Creación de los botones del menú.
        btnCircuitos = new JButton("Circuitos");
        btnPilotos = new JButton("Pilotos");
        btnVehiculos = new JButton("Vehículos");
        btnSimulacion = new JButton("Simulación");

        add(btnCircuitos);
        add(btnPilotos);
        add(btnVehiculos);
        add(btnSimulacion);

        // Cada botón abre su ventana correspondiente.
        btnCircuitos.addActionListener(e -> new VentanaCircuitos(circuitoServicio).setVisible(true));
        btnPilotos.addActionListener(e -> new VentanaPilotos(pilotoServicio).setVisible(true));
        btnVehiculos.addActionListener(e -> new VentanaVehiculos(vehiculoServicio, pilotoServicio).setVisible(true));
        btnSimulacion.addActionListener(e -> new VentanaSimulacion(circuitoServicio, vehiculoServicio, simulacionService).setVisible(true));

    }

}