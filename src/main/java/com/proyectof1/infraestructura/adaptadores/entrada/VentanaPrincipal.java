package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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

    /**
     * Constructor de la ventana principal. Recibe los servicios ya construidos.
     * Se valida que ninguno sea nulo.
     */
    public VentanaPrincipal(CircuitoServicio circuitoServicio, PilotoServicio pilotoServicio,
            VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        if (circuitoServicio == null || pilotoServicio == null
                || vehiculoServicio == null || simulacionService == null) {

            throw new IllegalArgumentException("Los servicios no pueden ser nulos.");

        }

        this.circuitoServicio = circuitoServicio;
        this.pilotoServicio = pilotoServicio;
        this.vehiculoServicio = vehiculoServicio;
        this.simulacionService = simulacionService;

        // Configuración básica de la ventana.
        setTitle("Simulación de Fórmula 1");
        setSize(430, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación entera.
        setLocationRelativeTo(null);                     // Centra la ventana.
        setLayout(new BorderLayout());

        // ----- Cabecera con el título del programa. -----
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(TemaF1.margenes(24, 4, 24, 24));
        cabecera.add(TemaF1.titulo("Formulemon"));
        cabecera.add(Box.createVerticalStrut(4));
        cabecera.add(TemaF1.subtitulo("Gestiona tu equipo y compite en carreras reales"));
        add(cabecera, BorderLayout.NORTH);

        // ----- Menú central con los botones de navegación. -----
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(TemaF1.margenes(8, 24, 40, 40));

        JButton btnCircuitos = nuevoBotonMenu("Circuitos", "Administra pistas y trazados");
        JButton btnPilotos = nuevoBotonMenu("Pilotos", "Gestiona tu alineación");
        JButton btnVehiculos = nuevoBotonMenu("Vehículos", "Configura tus escuderías");
        JButton btnSimulacion = nuevoBotonMenu("Simulación", "Vive la carrera en tiempo real");

        menu.add(btnCircuitos);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnPilotos);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnVehiculos);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnSimulacion);
        add(menu, BorderLayout.CENTER);

        // ----- Pie con una nota estilizada. -----
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        pie.add(TemaF1.etiqueta("Proyecto F1 · Arquitectura hexagonal"));
        add(pie, BorderLayout.SOUTH);

        // Cada botón abre su ventana correspondiente.
        btnCircuitos.addActionListener(e -> new VentanaCircuitos(circuitoServicio).setVisible(true));
        btnPilotos.addActionListener(e -> new VentanaPilotos(pilotoServicio).setVisible(true));
        btnVehiculos.addActionListener(e -> new VentanaVehiculos(vehiculoServicio, pilotoServicio).setVisible(true));
        btnSimulacion.addActionListener(e -> new VentanaSimulacion(circuitoServicio, vehiculoServicio, simulacionService).setVisible(true));

    }

    /**
     * Crea un botón de menú grande, con texto principal y descripción,
     * alineado al estilo de una tarjeta de navegación.
     */
    private JButton nuevoBotonMenu(String nombre, String descripcion) {

        JButton boton = new JButton("<html><b>" + nombre.toUpperCase() + "</b>"
                + "<br><font size='2'>" + descripcion + "</font></html>");
        boton.setAlignmentX(CENTER_ALIGNMENT);
        boton.setMinimumSize(new Dimension(300, 56));
        boton.setMaximumSize(new Dimension(360, 58));
        boton.setPreferredSize(new Dimension(340, 56));

        return boton;
    }

}