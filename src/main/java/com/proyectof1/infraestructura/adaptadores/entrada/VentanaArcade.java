package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.JuegoArcade;
import com.proyectof1.aplicacion.servicios.JuegoArcade.Dificultad;
import com.proyectof1.aplicacion.servicios.JuegoArcade.Obstaculo;
import com.proyectof1.dominio.Vehiculo;
import com.proyectof1.infraestructura.adaptadores.salida.RecordJson;

/**
 * Ventana del juego arcade (adaptador de entrada en Swing). Es un mini-juego
 * jugable de conducción entre carriles: el jugador mueve su coche con las
 * flechas izquierda/derecha (o A/D) para esquivar obstáculos que caen desde
 * arriba. Incluye vidas, dificultad seleccionable, pausa, marcadores en
 * pantalla y un récord persistido en JSON.
 *
 * <p>La pista se dibuja con {@code paintComponent} y avanza con un
 * {@link Timer}. El teclado se captura con {@link InputMap}/{@link ActionMap}
 * en modo {@code WHEN_IN_FOCUSED_WINDOW} para que responda aunque el foco esté
 * en un botón o desplegable. Toda la lógica vive en {@link JuegoArcade}.</p>
 */
public class VentanaArcade extends JFrame {

    // Estados posibles de la ventana.
    private enum Estado { INICIO, JUGANDO, PAUSA }

    // Dimensiones del área de juego.
    private static final int ANCHO_CARRIL = 80;
    private static final int ANCHO_PISTA = ANCHO_CARRIL * JuegoArcade.CANTIDAD_CARRILES;
    private static final int ALTO_PISTA = 540;
    private static final int MARGEN_INFERIOR = 30;

    // Tamaños de dibujo en píxeles.
    private static final int ANCHO_COCHE = 54;
    private static final int ALTO_COCHE_PX = 70;
    private static final int ANCHO_OBSTACULO = 40;
    private static final int ALTO_OBSTACULO_PX = 38;

    // Ritmo del bucle de juego.
    private static final int TICK_MS = 40;

    // Servicios inyectados: escuderías para elegir y récord persistente.
    private final VehiculoServicio vehiculoServicio;
    private final RecordJson recordJson;

    // Núcleo del juego.
    private JuegoArcade juego;

    // Estado actual de la ventana.
    private Estado estado;

    // Selectores y botones.
    private JComboBox<Vehiculo> comboVehiculos;
    private JComboBox<Dificultad> comboDificultad;
    private JButton btnIniciar;
    private JButton btnPausa;

    // Panel que dibuja la pista.
    private PanelPista panelPista;

    // Marcadores en pantalla.
    private JLabel etiquetaPuntos;
    private JLabel etiquetaVidas;
    private JLabel etiquetaNivel;
    private JLabel etiquetaRecord;

    // Color del coche según la escudería.
    private Color colorEscudo = TemaF1.ROJO_F1;

    // Temporizador del bucle de juego.
    private Timer timer;

    /**
     * Constructor de la ventana. Recibe el servicio de vehículos (para elegir
     * escudería) y el adaptador de récord. Valida que no sean nulos.
     */
    public VentanaArcade(VehiculoServicio vehiculoServicio, RecordJson recordJson) {

        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio,
                "El servicio de vehículos no puede ser nulo.");
        this.recordJson = Objects.requireNonNull(recordJson, "El récord no puede ser nulo.");

        this.juego = new JuegoArcade(recordJson.leer());
        this.estado = Estado.INICIO;

        setTitle("Juego Arcade · Formulemon");
        setSize(640, 760);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Cabecera.
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(10, 4, 16, 16));
        cabecera.add(TemaF1.titulo("Juego Arcade"), BorderLayout.WEST);
        add(cabecera, BorderLayout.NORTH);

        // Centro: pista + marcadores.
        JPanel centro = new JPanel(new BorderLayout());
        panelPista = new PanelPista();
        panelPista.setPreferredSize(new Dimension(ANCHO_PISTA, ALTO_PISTA));
        centro.add(panelPista, BorderLayout.CENTER);
        centro.add(construirPanelMarcadores(), BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // Sur: configuración y botones.
        add(construirPanelControles(), BorderLayout.SOUTH);

        // El teclado se captura con bindings para que funcione siempre que la
        // ventana tenga el foco, aunque el usuario haya pulsado un botón o combo.
        configurarAtajosDeTeclado();

        // Mensaje de ayuda inicial.
        panelPista.repaint();
    }

    /** Construye el panel de marcadores (puntos, vidas, nivel y récord). */
    private JPanel construirPanelMarcadores() {

        JPanel panel = new JPanel(new BorderLayout());

        etiquetaPuntos = TemaF1.etiqueta("Puntos: 0");
        etiquetaPuntos.setFont(etiquetaPuntos.getFont().deriveFont(Font.BOLD, 16f));
        etiquetaPuntos.setForeground(TemaF1.TEXTO);

        etiquetaVidas = TemaF1.etiqueta("Vidas: ♥♥♥");
        etiquetaVidas.setFont(etiquetaVidas.getFont().deriveFont(Font.BOLD, 16f));
        etiquetaVidas.setForeground(new Color(0xFF5252));

        etiquetaNivel = TemaF1.etiqueta("Nivel: 1");
        etiquetaNivel.setFont(etiquetaNivel.getFont().deriveFont(Font.BOLD, 14f));
        etiquetaNivel.setForeground(new Color(0x52E252));

        etiquetaRecord = TemaF1.etiqueta("Récord: " + juego.getRecord());
        etiquetaRecord.setFont(etiquetaRecord.getFont().deriveFont(Font.BOLD, 14f));
        etiquetaRecord.setForeground(new Color(0xF7C948));

        panel.add(etiquetaPuntos, BorderLayout.WEST);
        panel.add(etiquetaRecord, BorderLayout.EAST);

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        centro.add(etiquetaVidas);
        centro.add(etiquetaNivel);
        panel.add(centro, BorderLayout.CENTER);

        return panel;
    }

    /** Construye el panel inferior: escudería, dificultad y botones. */
    private JPanel construirPanelControles() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(TemaF1.margenes(8, 12, 16, 16));

        // Fila de configuración.
        JPanel filaConfig = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));

        comboVehiculos = new JComboBox<>();
        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {
            comboVehiculos.addItem(vehiculo);
        }

        comboDificultad = new JComboBox<>(Dificultad.values());

        filaConfig.add(TemaF1.etiqueta("Escudería:"));
        filaConfig.add(comboVehiculos);
        filaConfig.add(Box.createHorizontalStrut(14));
        filaConfig.add(TemaF1.etiqueta("Dificultad:"));
        filaConfig.add(comboDificultad);

        // Fila de acciones.
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));

        btnIniciar = new JButton("Iniciar");
        TemaF1.estilizarBoton(btnIniciar);

        btnPausa = new JButton("Pausa");
        TemaF1.estilizarBoton(btnPausa);
        btnPausa.setEnabled(false);

        filaBotones.add(btnIniciar);
        filaBotones.add(Box.createHorizontalStrut(14));
        filaBotones.add(btnPausa);

        panel.add(filaConfig);
        panel.add(Box.createVerticalStrut(6));
        panel.add(filaBotones);

        // Al cambiar de escudería se actualiza el color del coche (si no se corre).
        comboVehiculos.addActionListener(e -> {
            Vehiculo seleccion = (Vehiculo) comboVehiculos.getSelectedItem();
            if (seleccion != null && estado != Estado.JUGANDO) {
                colorEscudo = TemaF1.colorDeEscuderia(seleccion);
                panelPista.repaint();
            }
        });

        btnIniciar.addActionListener(e -> iniciarPartida());
        btnPausa.addActionListener(e -> alternarPausa());

        return panel;
    }

    /** Registra los atajos de teclado con WHEN_IN_FOCUSED_WINDOW. */
    private void configurarAtajosDeTeclado() {

        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("LEFT"), "moverIzq");
        im.put(KeyStroke.getKeyStroke("A"), "moverIzq");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "moverDer");
        im.put(KeyStroke.getKeyStroke("D"), "moverDer");
        im.put(KeyStroke.getKeyStroke("SPACE"), "iniciar");
        im.put(KeyStroke.getKeyStroke("P"), "pausa");

        am.put("moverIzq", new AccionTecla(() -> {
            if (estado == Estado.JUGANDO) {
                juego.cambiarCarrilIzquierda();
                panelPista.repaint();
            }
        }));

        am.put("moverDer", new AccionTecla(() -> {
            if (estado == Estado.JUGANDO) {
                juego.cambiarCarrilDerecha();
                panelPista.repaint();
            }
        }));

        am.put("iniciar", new AccionTecla(this::iniciarPartida));
        am.put("pausa", new AccionTecla(this::alternarPausa));

    }

    /** Inicia una partida nueva (o la reinicia si ya se estaba jugando). */
    private void iniciarPartida() {

        if (estado == Estado.JUGANDO && !juego.isGameOver()) {
            // Ya está corriendo; la barra espaciadora no la reinicia por error.
            return;
        }

        // Lee la dificultad elegida y reconstruye el juego con el récord actual.
        Dificultad dificultad = (Dificultad) comboDificultad.getSelectedItem();
        if (dificultad == null) {
            dificultad = Dificultad.NORMAL;
        }
        juego = new JuegoArcade(new java.util.Random(), juego.getRecord(), dificultad);

        Vehiculo seleccion = (Vehiculo) comboVehiculos.getSelectedItem();
        colorEscudo = (seleccion != null) ? TemaF1.colorDeEscuderia(seleccion) : TemaF1.ROJO_F1;

        estado = Estado.JUGANDO;
        actualizarMarcadores();
        panelPista.repaint();

        btnIniciar.setText("Reiniciar");
        btnPausa.setEnabled(true);

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(TICK_MS, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (estado != Estado.JUGANDO) {
                    return;
                }

                juego.avanzar();
                actualizarMarcadores();
                panelPista.repaint();

                if (juego.isGameOver()) {
                    ((Timer) e.getSource()).stop();
                    btnPausa.setEnabled(false);
                    btnIniciar.setText("Jugar de nuevo");
                    terminarPartida();
                }
            }
        });

        timer.start();
    }

    /** Pausa o reanuda la partida en curso. */
    private void alternarPausa() {

        // Tras el fin de partida no se puede pausar.
        if (juego.isGameOver()) {
            return;
        }

        if (estado == Estado.JUGANDO) {

            estado = Estado.PAUSA;
            btnPausa.setText("Reanudar");
            panelPista.repaint();

        } else if (estado == Estado.PAUSA) {

            estado = Estado.JUGANDO;
            btnPausa.setText("Pausa");
            panelPista.repaint();

        }
    }

    /** Refresca los marcadores de puntos, vidas, nivel y récord. */
    private void actualizarMarcadores() {

        etiquetaPuntos.setText("Puntos: " + juego.getPuntuacion());
        etiquetaNivel.setText("Nivel: " + (juego.getNivel() + 1));
        etiquetaVidas.setText("Vidas: " + corazones(juego.getVidas()));
        etiquetaRecord.setText("Récord: " + juego.getRecord());

    }

    /** Convierte las vidas en una cadena de corazones rellenos/vacíos. */
    private String corazones(int vidas) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < JuegoArcade.VIDAS_INICIALES; i++) {
            sb.append(i < vidas ? "♥" : "·");
        }
        return sb.toString();
    }

    /** Finaliza la partida, guarda el récord y avisa al jugador. */
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

        etiquetaRecord.setText("Récord: " + juego.getRecord());
    }

    /** Convierte una coordenada vertical lógica del modelo en píxel de pantalla. */
    private int aPixelY(double yLogica) {
        double ratio = yLogica / JuegoArcade.LARGO_PISTA;
        return (int) (ratio * (ALTO_PISTA - MARGEN_INFERIOR));
    }

    /**
     * Panel que dibuja la pista, el coche y los obstáculos, además de las
     * superposiciones de inicio y pausa.
     */
    private class PanelPista extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo asfaltado.
            g2.setColor(new Color(0x2A2A2A));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Líneas divisorias de carriles.
            g2.setColor(new Color(0xCCCCCC));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < JuegoArcade.CANTIDAD_CARRILES; i++) {
                g2.drawLine(i * ANCHO_CARRIL, 0, i * ANCHO_CARRIL, getHeight());
            }

            // Línea de meta (dónde está el jugador), para entender la hitbox.
            int yMeta = aPixelY(JuegoArcade.PARTE_SUPERIOR_COCHE);
            g2.setColor(new Color(0xFFFFFF));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(0, yMeta, getWidth(), yMeta);

            // Coche del jugador, anclado a la línea de meta.
            int xCoche = juego.getCarrilCoche() * ANCHO_CARRIL + (ANCHO_CARRIL - ANCHO_COCHE) / 2;
            int yCoche = yMeta;

            // Si es inmune (tras un golpe), el coche parpadea para avisar.
            if (!juego.estaInmune() || (juego.getPuntuacion() % 2 == 0)) {
                g2.setColor(colorEscudo);
                g2.fillRoundRect(xCoche, yCoche, ANCHO_COCHE, ALTO_COCHE_PX, 14, 14);
            }

            // Obstáculos: rectángulos que caen desde arriba.
            g2.setColor(new Color(0xCC3333));
            for (Obstaculo obstaculo : juego.getObstaculos()) {

                int y = aPixelY(obstaculo.getY());
                int x = obstaculo.getCarril() * ANCHO_CARRIL + (ANCHO_CARRIL - ANCHO_OBSTACULO) / 2;
                g2.fillRoundRect(x, y, ANCHO_OBSTACULO, ALTO_OBSTACULO_PX, 6, 6);

            }

            // Superposiciones de estado.
            if (estado == Estado.INICIO) {
                dibujarMensaje(g2, "Formulemon Arcade", "Usa ← → o A/D para moverte", "Pulsa Iniciar o Espacio para jugar");
            } else if (estado == Estado.PAUSA) {
                dibujarMensaje(g2, "Pausa", "Pulsa P o Reanudar para seguir", null);
            }

        }

        /** Dibuja un título, un subtítulo y una tercera línea centrados. */
        private void dibujarMensaje(Graphics2D g2, String titulo, String texto, String pie) {

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(TemaF1.ROJO_F1);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));
            int ancho = g2.getFontMetrics().stringWidth(titulo);
            g2.drawString(titulo, (getWidth() - ancho) / 2, getHeight() / 2 - 20);

            g2.setColor(TemaF1.TEXTO);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            if (texto != null) {
                g2.drawString(texto, (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2, getHeight() / 2 + 20);
            }
            if (pie != null) {
                g2.drawString(pie, (getWidth() - g2.getFontMetrics().stringWidth(pie)) / 2, getHeight() / 2 + 48);
            }
        }
    }

    /** Acción reutilizable para los atajos de teclado. */
    private class AccionTecla extends AbstractAction {

        private final Runnable accion;

        AccionTecla(Runnable accion) {
            this.accion = accion;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            accion.run();
        }
    }

}