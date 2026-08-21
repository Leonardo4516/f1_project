package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.JuegoArcade;
import com.proyectof1.dominio.Vehiculo;
import com.proyectof1.infraestructura.adaptadores.salida.RecordJson;

/**
 * Ventana del juego arcade (adaptador de entrada en Swing). Es un mini-juego
 * jugable: el jugador conduce un coche entre tres carriles con las flechas
 * izquierda/derecha, esquivando obstáculos que caen con dificultad progresiva.
 * Elige su escudería antes de empezar y el récord se guarda en JSON.
 *
 * <p>El juego dibuja su pista con {@code paintComponent} y avanza con un
 * {@link Timer}; toda la lógica de juego vive en {@link JuegoArcade} (capa de
 * aplicación), de modo que esta ventana solo presenta y captura el teclado.</p>
 */
public class VentanaArcade extends JFrame {

    // Dimensiones del área de juego dibujada.
    private static final int ANCHO_PISTA = 360;
    private static final int ALTO_PISTA = 480;
    private static final int ANCHO_CARRIL = ANCHO_PISTA / JuegoArcade.CANTIDAD_CARRILES;

    // Tamaño del coche del jugador en el dibujo.
    private static final int ANCHO_COCHE = 70;
    private static final int ALTO_COCHE = 40;

    // Ritmo del bucle: cada cuántos milisegundos avanza un paso del juego.
    private static final int TICK_MS = 90;

    // Servicios inyectados: escuderías para elegir y récord persistente.
    private final VehiculoServicio vehiculoServicio;
    private final RecordJson recordJson;

    // Núcleo del juego (capa de aplicación).
    private JuegoArcade juego;

    // Selector de escudería y botón de acción.
    private JComboBox<Vehiculo> comboVehiculos;
    private JButton btnIniciar;

    // Panel que dibuja la pista y los elementos del juego.
    private PanelPista panelPista;

    // Etiquetas de marcador.
    private JLabel etiquetaPuntos;
    private JLabel etiquetaRecord;

    // Color del coche según la escudería elegida.
    private Color colorEscudo = TemaF1.ROJO_F1;

    /**
     * Constructor de la ventana. Recibe el servicio de vehículos (para elegir
     * escudería) y el adaptador de récord. Valida que no sean nulos.
     */
    public VentanaArcade(VehiculoServicio vehiculoServicio, RecordJson recordJson) {

        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio,
                "El servicio de vehículos no puede ser nulo.");
        this.recordJson = Objects.requireNonNull(recordJson, "El récord no puede ser nulo.");

        // El récord previo se carga desde la persistencia.
        this.juego = new JuegoArcade(recordJson.leer());

        // Configuración básica de la ventana.
        setTitle("Juego Arcade · Formulemon");
        setSize(560, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera con el título. -----
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(12, 6, 16, 16));
        cabecera.add(TemaF1.titulo("Juego Arcade"), BorderLayout.WEST);
        add(cabecera, BorderLayout.NORTH);

        // ----- Centro: panel de pista y, bajo él, los marcadores. -----
        JPanel centro = new JPanel(new BorderLayout());
        panelPista = new PanelPista();
        panelPista.setPreferredSize(new Dimension(ANCHO_PISTA, ALTO_PISTA));
        centro.add(panelPista, BorderLayout.CENTER);
        centro.add(construirPanelMarcadores(), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // ----- Sur: selector de escudería y botón de inicio. -----
        add(construirPanelControles(), BorderLayout.SOUTH);

        // Control por teclado en todo el juego.
        addKeyListener(new TecladoJuego());
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Mensaje de ayuda inicial.
        panelPista.setMensaje("Elige tu escudería y presiona Iniciar");
        panelPista.repaint();

    }

    /** Construye el panel inferior del centro: puntos y récord. */
    private JPanel construirPanelMarcadores() {

        JPanel panel = new JPanel(new BorderLayout());

        etiquetaPuntos = TemaF1.etiqueta("Puntos: 0");
        etiquetaPuntos.setFont(etiquetaPuntos.getFont().deriveFont(Font.BOLD, 16f));
        etiquetaPuntos.setForeground(TemaF1.TEXTO);

        etiquetaRecord = TemaF1.etiqueta("Récord: " + juego.getRecord());
        etiquetaRecord.setFont(etiquetaRecord.getFont().deriveFont(Font.BOLD, 16f));
        etiquetaRecord.setForeground(new Color(0xF7C948));

        panel.add(etiquetaPuntos, BorderLayout.WEST);
        panel.add(etiquetaRecord, BorderLayout.EAST);

        return panel;
    }

    /** Construye el panel sur: selector de escudería y botón de inicio. */
    private JPanel construirPanelControles() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(TemaF1.margenes(8, 12, 16, 16));

        JPanel fila = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 4));

        comboVehiculos = new JComboBox<>();
        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {
            comboVehiculos.addItem(vehiculo);
        }

        btnIniciar = new JButton("Iniciar");
        TemaF1.estilizarBoton(btnIniciar);

        fila.add(TemaF1.etiqueta("Escudería:"));
        fila.add(comboVehiculos);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(btnIniciar);

        panel.add(fila);

        // Al cambiar de escudería, se actualiza el color del coche si no se corre.
        comboVehiculos.addActionListener(e -> {
            Vehiculo seleccion = (Vehiculo) comboVehiculos.getSelectedItem();
            if (seleccion != null && !juegoEnCurso()) {
                colorEscudo = TemaF1.colorDeEscuderia(seleccion);
                panelPista.repaint();
            }
        });

        btnIniciar.addActionListener(e -> iniciarPartida());

        return panel;
    }

    /** ¿Hay una partida corriendo en este momento? */
    private boolean juegoEnCurso() {
        return juego != null && !juego.isGameOver() && (juego.getPuntuacion() > 0 || !juego.getObstaculos().isEmpty());
    }

    /** Prepara y arranca una nueva partida (o reinicia la actual). */
    private void iniciarPartida() {

        // Reinicia el estado lógico; el récord se conserva en memoria.
        juego.reiniciar();

        Vehiculo seleccion = (Vehiculo) comboVehiculos.getSelectedItem();
        colorEscudo = (seleccion != null) ? TemaF1.colorDeEscuderia(seleccion) : TemaF1.ROJO_F1;

        etiquetaPuntos.setText("Puntos: 0");
        panelPista.setMensaje(null);
        panelPista.repaint();

        btnIniciar.setText("Reiniciar");

        // El bucle de juego avanza el estado y repinta la pista.
        Timer temporizador = new Timer(TICK_MS, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                juego.avanzar();
                actualizarMarcadores();
                panelPista.repaint();

                if (juego.isGameOver()) {
                    ((Timer) e.getSource()).stop();
                    terminarPartida();
                }

            }
        });

        temporizador.start();

    }

    /** Refresca los marcadores de puntos y récord con el estado del juego. */
    private void actualizarMarcadores() {

        etiquetaPuntos.setText("Puntos: " + juego.getPuntuacion());
        etiquetaRecord.setText("Récord: " + juego.getRecord());

    }

    /** Finaliza la partida, guarda el récord si procede y avisa al jugador. */
    private void terminarPartida() {

        boolean nuevoRecord = recordJson.guardar(juego.getPuntuacion());

        if (nuevoRecord) {
            JOptionPane.showMessageDialog(this,
                    "¡Nuevo récord! " + juego.getPuntuacion() + " puntos.",
                    "Fin de la carrera", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Chocaste. Puntuación: " + juego.getPuntuacion() + " puntos.",
                    "Fin de la carrera", JOptionPane.INFORMATION_MESSAGE);
        }

        btnIniciar.setText("Jugar de nuevo");
    }

    /**
     * Panel que dibuja la pista, el coche y los obstáculos del juego.
     * Cada tick repinta el estado actual del {@link JuegoArcade}.
     */
    private class PanelPista extends JPanel {

        // Mensaje de bienvenida/ayuda (null durante la partida).
        private String mensaje;

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo asfaltado.
            g2.setColor(new Color(0x2A2A2A));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Líneas divisorias de carriles.
            g2.setColor(new Color(0xFFFFFF));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < JuegoArcade.CANTIDAD_CARRILES; i++) {
                g2.drawLine(i * ANCHO_CARRIL, 0, i * ANCHO_CARRIL, getHeight());
            }

            // Coche del jugador, en el color de su escudería.
            int xCoche = juego.getCarrilCoche() * ANCHO_CARRIL + (ANCHO_CARRIL - ANCHO_COCHE) / 2;
            int yCoche = getHeight() - ALTO_COCHE - 20;
            g2.setColor(colorEscudo);
            g2.fillRoundRect(xCoche, yCoche, ANCHO_COCHE, ALTO_COCHE, 14, 14);

            // Obstáculos: rectángulos que descienden por los carriles.
            g2.setColor(new Color(0xCC3333));
            for (int[] obstaculo : juego.getObstaculos()) {

                // Escala de la distancia (0-800) al alto de la pista.
                int y = (int) ((double) obstaculo[1] / 800.0 * (getHeight() - 60));
                int x = obstaculo[0] * ANCHO_CARRIL + (ANCHO_CARRIL - 60) / 2;
                g2.fillRoundRect(x, y, 60, 34, 8, 8);

            }

            // Mensaje de bienvenida superpuesto al centro.
            if (mensaje != null) {
                g2.setColor(TemaF1.TEXTO);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
                g2.drawString(mensaje, 30, getHeight() / 2);
            }

        }

        void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }

    /**
     * Escucha las teclas del jugador: flechas para moverse y Enter/R para
     * reiniciar. Solo se mueve si la partida está en curso.
     */
    private class TecladoJuego implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {

            if (juego.isGameOver()) {
                return;
            }

            switch (e.getKeyCode()) {

                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    juego.cambiarCarrilIzquierda();
                    panelPista.repaint();
                    break;

                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    juego.cambiarCarrilDerecha();
                    panelPista.repaint();
                    break;

                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_R:
                    iniciarPartida();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // No se necesita acción al soltar.
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // No se necesita acción por carácter.
        }
    }

}