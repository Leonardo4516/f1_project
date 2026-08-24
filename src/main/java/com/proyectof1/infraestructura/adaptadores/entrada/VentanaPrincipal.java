package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.puertos.salida.RankingRepositorio;
import com.proyectof1.aplicacion.servicios.SimulacionService;

public class VentanaPrincipal extends JFrame {

    private static final int SEPARACION_BOTONES = 14;

    private final CircuitoServicio circuitoServicio;
    private final PilotoServicio pilotoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;
    private final RankingRepositorio rankingRepositorio;

    public VentanaPrincipal(CircuitoServicio circuitoServicio, PilotoServicio pilotoServicio,
            VehiculoServicio vehiculoServicio, SimulacionService simulacionService,
            RankingRepositorio rankingRepositorio) {

        this.circuitoServicio = Objects.requireNonNull(circuitoServicio, "Los servicios no pueden ser nulos.");
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio, "Los servicios no pueden ser nulos.");
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio, "Los servicios no pueden ser nulos.");
        this.simulacionService = Objects.requireNonNull(simulacionService, "Los servicios no pueden ser nulos.");
        this.rankingRepositorio = Objects.requireNonNull(rankingRepositorio, "El ranking no puede ser nulo.");

        setTitle("Simulación de Fórmula 1");
        setSize(520, 620);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(construirCabecera(), BorderLayout.NORTH);

        UIManager.put("Button.arc", 10);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        TemaF1.conBorde(menu);
        menu.setBorder(BorderFactory.createCompoundBorder(
                menu.getBorder(),
                TemaF1.margenes(8, 16, 48, 48)));

        JButton btnCircuitos = nuevoBotonMenu("Circuitos", "Administra pistas y trazados", TemaF1.colorDeEscuderia("Red Bull"), "circuits");
        JButton btnPilotos = nuevoBotonMenu("Pilotos", "Gestiona tu alineación", TemaF1.colorDeEscuderia("Sauber"), "drivers");
        JButton btnVehiculos = nuevoBotonMenu("Vehículos", "Configura tus escuderías", TemaF1.colorDeEscuderia("McLaren"), "car");
        JButton btnSimulacion = nuevoBotonMenu("Simulación", "Vive la carrera en tiempo real", TemaF1.ROJO_F1, "flag");
        JButton btnArcade = nuevoBotonMenu("Juego Arcade", "Conduce y esquiva en modo arcade", TemaF1.colorDeEscuderia("Mercedes"), "gamepad");

        menu.add(btnCircuitos);
        menu.add(Box.createVerticalStrut(SEPARACION_BOTONES));
        menu.add(btnPilotos);
        menu.add(Box.createVerticalStrut(SEPARACION_BOTONES));
        menu.add(btnVehiculos);
        menu.add(Box.createVerticalStrut(SEPARACION_BOTONES));
        menu.add(btnSimulacion);
        menu.add(Box.createVerticalStrut(SEPARACION_BOTONES));
        menu.add(btnArcade);
        add(menu, BorderLayout.CENTER);

        btnCircuitos.addActionListener(e -> {
            VentanaCircuitos v = new VentanaCircuitos(circuitoServicio);
            v.setLocationRelativeTo(this);
            v.setVisible(true);
            v.toFront();
        });
        btnPilotos.addActionListener(e -> {
            VentanaPilotos v = new VentanaPilotos(pilotoServicio);
            v.setLocationRelativeTo(this);
            v.setVisible(true);
            v.toFront();
        });
        btnVehiculos.addActionListener(e -> {
            VentanaVehiculos v = new VentanaVehiculos(vehiculoServicio, pilotoServicio);
            v.setLocationRelativeTo(this);
            v.setVisible(true);
            v.toFront();
        });
        btnSimulacion.addActionListener(e -> {
            VentanaSimulacion v = new VentanaSimulacion(circuitoServicio, vehiculoServicio, simulacionService);
            v.setLocationRelativeTo(this);
            v.setVisible(true);
            v.toFront();
        });
        btnArcade.addActionListener(e -> {
            VentanaArcade v = new VentanaArcade(vehiculoServicio, rankingRepositorio);
            v.setLocationRelativeTo(this);
            v.setVisible(true);
            v.toFront();
        });
    }

    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(TemaF1.margenes(28, 6, 24, 24));

        JLabel titulo = TemaF1.titulo("Formulemon");
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 30f));
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitulo = TemaF1.subtitulo("Gestiona tu equipo y compite en carreras reales");
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

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

    private JButton nuevoBotonMenu(String nombre, String descripcion, Color acento, String iconoNombre) {
        JButton boton = new BotonMenu(nombre, descripcion, acento, iconoNombre);
        boton.setAlignmentX(CENTER_ALIGNMENT);
        boton.setMinimumSize(new Dimension(380, 68));
        boton.setMaximumSize(new Dimension(380, 68));
        boton.setPreferredSize(new Dimension(380, 68));
        return boton;
    }

    private class BotonMenu extends JButton {

        private static final Color HOVER_COLOR = new Color(0x333333);

        BotonMenu(String nombre, String descripcion, Color acento, String iconoNombre) {
            super("<html><b style='font-size:14px'>" + nombre.toUpperCase()
                    + "</b><br><font color='#9A9A9A' size='2'>" + descripcion + "</font></html>");

            setIcon(TemaF1.icono(iconoNombre));

            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(TemaF1.TEXTO);
            setBackground(TemaF1.PANEL);
            setOpaque(true);

            Border acentoIzquierdo = BorderFactory.createMatteBorder(0, 6, 0, 0, acento);
            Border relleno = TemaF1.margenes(16, 18, 18, 16);
            setBorder(BorderFactory.createCompoundBorder(acentoIzquierdo, relleno));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(HOVER_COLOR);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(TemaF1.PANEL);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setBackground(TemaF1.FONDO);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setBackground(HOVER_COLOR);
                }
            });
        }
    }
}
