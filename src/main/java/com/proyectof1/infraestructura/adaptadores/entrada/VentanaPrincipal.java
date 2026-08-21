package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.infraestructura.adaptadores.salida.RecordJson;

/**
 * Ventana principal del programa (adaptador de entrada en Swing).
 * Actúa como menú de navegación con estética moderna estilo Fórmula 1:
 * cabecera de marca con acento rojo y tarjetas de menú con barra de color
 * por módulo, hover y cursor de mano. Ofrece botones para abrir las ventanas
 * de gestión de circuitos, pilotos, vehículos y la simulación de carreras.
 */
public class VentanaPrincipal extends JFrame {

    // Colores de acento por módulo (se reutiliza la paleta del tema F1).
    private static final Color ACENTO_CIRCUITOS = new Color(0x3671C6);
    private static final Color ACENTO_PILOTOS = new Color(0x52E252);
    private static final Color ACENTO_VEHICULOS = new Color(0xFF8000);

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

        this.circuitoServicio = Objects.requireNonNull(circuitoServicio, "Los servicios no pueden ser nulos.");
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio, "Los servicios no pueden ser nulos.");
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio, "Los servicios no pueden ser nulos.");
        this.simulacionService = Objects.requireNonNull(simulacionService, "Los servicios no pueden ser nulos.");

        // Configuración básica de la ventana (dashboard fijo).
        setTitle("Simulación de Fórmula 1");
        setSize(520, 620);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación entera.
        setLocationRelativeTo(null);                     // Centra la ventana.
        setLayout(new BorderLayout());

        // ----- Cabecera con la marca del programa. -----
        add(construirCabecera(), BorderLayout.NORTH);

        // ----- Menú central con las tarjetas de navegación. -----
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(TemaF1.margenes(12, 24, 60, 60));

        JButton btnCircuitos = nuevoBotonMenu("Circuitos", "Administra pistas y trazados", ACENTO_CIRCUITOS);
        JButton btnPilotos = nuevoBotonMenu("Pilotos", "Gestiona tu alineación", ACENTO_PILOTOS);
        JButton btnVehiculos = nuevoBotonMenu("Vehículos", "Configura tus escuderías", ACENTO_VEHICULOS);
        JButton btnSimulacion = nuevoBotonMenu("Simulación", "Vive la carrera en tiempo real", TemaF1.ROJO_F1);
        JButton btnArcade = nuevoBotonMenu("Juego Arcade", "Conduce y esquiva en modo arcade", new Color(0x00E5FF));

        menu.add(btnCircuitos);
        menu.add(Box.createVerticalStrut(14));
        menu.add(btnPilotos);
        menu.add(Box.createVerticalStrut(14));
        menu.add(btnVehiculos);
        menu.add(Box.createVerticalStrut(14));
        menu.add(btnSimulacion);
        menu.add(Box.createVerticalStrut(14));
        menu.add(btnArcade);
        add(menu, BorderLayout.CENTER);

        // ----- Pie con una nota estilizada. -----
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        pie.add(TemaF1.etiqueta("Proyecto F1 · Arquitectura hexagonal"));
        add(pie, BorderLayout.SOUTH);

        // Cada botón abre su ventana correspondiente.
        btnCircuitos.addActionListener(e -> new VentanaCircuitos(circuitoServicio).setVisible(true));
        btnPilotos.addActionListener(e -> new VentanaPilotos(pilotoServicio).setVisible(true));
        btnVehiculos.addActionListener(e -> new VentanaVehiculos(vehiculoServicio, pilotoServicio).setVisible(true));
        btnSimulacion.addActionListener(e -> new VentanaSimulacion(circuitoServicio, vehiculoServicio, simulacionService).setVisible(true));
        btnArcade.addActionListener(e -> new VentanaArcade(vehiculoServicio, new RecordJson()).setVisible(true));

    }

    /** Construye la cabecera: título grande, subtítulo y barra de acento roja. */
    private JPanel construirCabecera() {

        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(TemaF1.margenes(28, 6, 24, 24));

        JLabel titulo = new JLabel("Formulemon");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f));
        titulo.setForeground(TemaF1.ROJO_F1);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitulo = TemaF1.subtitulo("Gestiona tu equipo y compite en carreras reales");
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        // Barrita roja central como acento visual de la marca.
        JPanel barraAcento = new JPanel();
        barraAcento.setPreferredSize(new Dimension(72, 4));
        barraAcento.setMaximumSize(new Dimension(72, 4));
        barraAcento.setBackground(TemaF1.ROJO_F1);
        barraAcento.setAlignmentX(CENTER_ALIGNMENT);

        cabecera.add(titulo);
        cabecera.add(Box.createVerticalStrut(6));
        cabecera.add(subtitulo);
        cabecera.add(Box.createVerticalStrut(16));
        cabecera.add(barraAcento);

        return cabecera;
    }

    /**
     * Crea una tarjeta de menú grande, con texto principal y descripción,
     * barra de acento de color en el borde izquierdo y efectos al pasar el
     * ratón por encima.
     */
    private JButton nuevoBotonMenu(String nombre, String descripcion, Color acento) {

        JButton boton = new BotonMenu(nombre, descripcion, acento);
        boton.setAlignmentX(CENTER_ALIGNMENT);
        boton.setMinimumSize(new Dimension(380, 68));
        boton.setMaximumSize(new Dimension(380, 68));
        boton.setPreferredSize(new Dimension(380, 68));

        return boton;
    }

    /**
     * Botón con apariencia de tarjeta: fondo oscuro con borde de acento a la
     * izquierda, texto en negrita con descripción en gris y colores que cambian
     * al pasar el cursor o pulsar.
     */
    private class BotonMenu extends JButton {

        // Fondos de los distintos estados del botón.
        private static final Color HOVER = new Color(0x2A2A2A);
        private static final Color PRESIONADO = new Color(0x232323);

        BotonMenu(String nombre, String descripcion, Color acento) {

            super("<html><b style='font-size:14px'>" + nombre.toUpperCase()
                    + "</b><br><font color='#9A9A9A' size='2'>" + descripcion + "</font></html>");

            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(TemaF1.TEXTO);
            setBackground(TemaF1.PANEL);
            setOpaque(true);

            // Acento de color a la izquierda + relleno interior cómodo.
            Border acentoIzquierdo = BorderFactory.createMatteBorder(0, 6, 0, 0, acento);
            Border relleno = TemaF1.margenes(16, 18, 18, 16);
            setBorder(BorderFactory.createCompoundBorder(acentoIzquierdo, relleno));

            // Estados visuales: hover al pasar, más oscuro al pulsar.
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(TemaF1.PANEL);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setBackground(PRESIONADO);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setBackground(HOVER);
                }
            });
        }
    }

}