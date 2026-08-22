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
import java.util.List;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.puertos.salida.RankingRepositorio;
import com.proyectof1.aplicacion.servicios.JuegoArcade;
import com.proyectof1.aplicacion.servicios.JuegoArcade.Dificultad;
import com.proyectof1.aplicacion.servicios.JuegoArcade.Obstaculo;
import com.proyectof1.dominio.EntradaRanking;
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana del juego arcade (adaptador de entrada en Swing). Es un mini-juego
 * jugable de conducción entre carriles: el jugador mueve su coche con las
 * flechas izquierda/derecha (o A/D) para esquivar obstáculos que caen desde
 * arriba. Incluye vidas, dificultad seleccionable, pausa, marcadores en
 * pantalla, modo clasificación con top 5 y récord persistido en PostgreSQL.
 */
public class VentanaArcade extends JFrame {

    private enum Estado { INICIO, JUGANDO, PAUSA }

    private static final int ANCHO_CARRIL = 80;
    private static final int ANCHO_PISTA = ANCHO_CARRIL * JuegoArcade.CANTIDAD_CARRILES;
    private static final int ALTO_PISTA = 540;
    private static final int MARGEN_INFERIOR = 30;

    private static final int ANCHO_COCHE = 44;
    private static final int ALTO_COCHE_PX = 60;
    private static final int ANCHO_OBSTACULO = 40;
    private static final int ALTO_OBSTACULO_PX = 38;

    private static final int TICK_MS = 40;

    private final VehiculoServicio vehiculoServicio;
    private final RankingRepositorio rankingRepositorio;

    private JuegoArcade juego;
    private Estado estado;

    private JComboBox<Vehiculo> comboVehiculos;
    private JComboBox<Dificultad> comboDificultad;
    private JButton btnIniciar;
    private JButton btnPausa;

    private PanelPista panelPista;

    private JLabel etiquetaPuntos;
    private JLabel etiquetaVidas;
    private JLabel etiquetaNivel;
    private JLabel etiquetaRecord;

    private Color colorEscudo = TemaF1.ROJO_F1;
    private Timer timer;

    // Clasificación: nombre del jugador y panel de top 5.
    private String nombreJugador;
    private JPanel panelRanking;
    private DefaultTableModel modeloRanking;

    /**
     * Constructor de la ventana. Recibe el servicio de vehículos y el repositorio
     * de ranking. Valida que no sean nulos.
     */
    public VentanaArcade(VehiculoServicio vehiculoServicio, RankingRepositorio rankingRepositorio) {

        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio,
                "El servicio de vehículos no puede ser nulo.");
        this.rankingRepositorio = Objects.requireNonNull(rankingRepositorio,
                "El repositorio de ranking no puede ser nulo.");

        this.juego = new JuegoArcade(0);
        this.estado = Estado.INICIO;

        setTitle("Juego Arcade · Formulemon");
        setSize(900, 760);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Cabecera.
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(10, 4, 16, 16));
        cabecera.add(TemaF1.titulo("Juego Arcade"), BorderLayout.WEST);
        add(cabecera, BorderLayout.NORTH);

        // Centro: pista centrada + marcadores + ranking.
        JPanel centro = new JPanel(new BorderLayout());
        panelPista = new PanelPista();
        panelPista.setPreferredSize(new Dimension(ANCHO_PISTA, ALTO_PISTA));

        JPanel wrapperPista = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperPista.setBackground(TemaF1.ASFALTO);
        wrapperPista.add(panelPista);
        centro.add(wrapperPista, BorderLayout.CENTER);
        centro.add(construirPanelMarcadores(), BorderLayout.SOUTH);

        // Panel de ranking a la derecha (solo visible en modo clasificación).
        panelRanking = construirPanelRanking();
        panelRanking.setVisible(false);
        centro.add(panelRanking, BorderLayout.EAST);

        add(centro, BorderLayout.CENTER);

        // Sur: configuración y botones.
        add(construirPanelControles(), BorderLayout.SOUTH);

        configurarAtajosDeTeclado();
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

    /** Construye el panel del top 5 de clasificación. */
    private JPanel construirPanelRanking() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(TemaF1.margenes(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(200, ALTO_PISTA + 40));
        panel.setBackground(TemaF1.PANEL);

        JLabel titulo = TemaF1.etiqueta("TOP 5");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setForeground(new Color(0xF7C948));
        titulo.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titulo, BorderLayout.NORTH);

        String[] columnas = {"#", "Jugador", "Pts"};
        modeloRanking = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(modeloRanking);
        tabla.setFont(tabla.getFont().deriveFont(13f));
        tabla.setRowHeight(26);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 2));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(30);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(50);
        tabla.getTableHeader().setFont(tabla.getFont().deriveFont(Font.BOLD, 12f));
        tabla.getTableHeader().setBackground(TemaF1.FONDO);
        tabla.getTableHeader().setForeground(TemaF1.TEXTO_SECUNDARIO);
        tabla.setBackground(TemaF1.PANEL);
        tabla.setForeground(TemaF1.TEXTO);
        tabla.setSelectionBackground(TemaF1.ROJO_F1);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TemaF1.PANEL);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /** Actualiza la tabla del top 5 desde la base de datos. */
    private void actualizarTop5() {

        Dificultad diff = (Dificultad) comboDificultad.getSelectedItem();
        List<EntradaRanking> entradas;

        if (diff == Dificultad.CLASIFICACION) {
            entradas = rankingRepositorio.top5();
        } else {
            entradas = rankingRepositorio.top5PorDificultad(diff.name());
        }

        modeloRanking.setRowCount(0);

        for (int i = 0; i < entradas.size(); i++) {
            EntradaRanking e = entradas.get(i);
            modeloRanking.addRow(new Object[]{i + 1, e.jugador(), e.puntuacion()});
        }
    }

    /** Construye el panel inferior: escudería, dificultad y botones. */
    private JPanel construirPanelControles() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(TemaF1.margenes(8, 12, 16, 16));

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

        comboVehiculos.addActionListener(e -> {
            Vehiculo seleccion = (Vehiculo) comboVehiculos.getSelectedItem();
            if (seleccion != null && estado != Estado.JUGANDO) {
                colorEscudo = TemaF1.colorDeEscuderia(seleccion);
                panelPista.repaint();
            }
        });

        comboDificultad.addActionListener(e -> {
            boolean esClasificacion = comboDificultad.getSelectedItem() == Dificultad.CLASIFICACION;
            panelRanking.setVisible(esClasificacion);
            actualizarTop5();
            pack();
            setLocationRelativeTo(null);
        });

        btnIniciar.addActionListener(e -> iniciarPartida());
        btnPausa.addActionListener(e -> alternarPausa());

        return panel;
    }

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
            return;
        }

        Dificultad dificultad = (Dificultad) comboDificultad.getSelectedItem();
        if (dificultad == null) {
            dificultad = Dificultad.NORMAL;
        }

        // En modo clasificación se pide el nombre del jugador.
        if (dificultad == Dificultad.CLASIFICACION) {
            nombreJugador = (String) JOptionPane.showInputDialog(this,
                    "Ingresa tu nombre para la clasificación:",
                    "Clasificación", JOptionPane.PLAIN_MESSAGE,
                    null, null, nombreJugador);
            if (nombreJugador == null || nombreJugador.isBlank()) {
                return;
            }
            nombreJugador = nombreJugador.trim();
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

    private void alternarPausa() {

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

    private void actualizarMarcadores() {

        etiquetaPuntos.setText("Puntos: " + juego.getPuntuacion());
        etiquetaNivel.setText("Nivel: " + (juego.getNivel() + 1));
        etiquetaVidas.setText("Vidas: " + corazones(juego.getVidas()));
        etiquetaRecord.setText("Récord: " + juego.getRecord());
    }

    private String corazones(int vidas) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < JuegoArcade.VIDAS_INICIALES; i++) {
            sb.append(i < vidas ? "♥" : "·");
        }
        return sb.toString();
    }

    /** Finaliza la partida, guarda en ranking si es clasificación y avisa. */
    private void terminarPartida() {

        Dificultad diff = (Dificultad) comboDificultad.getSelectedItem();

        // En modo clasificación, guardar la puntuación en el ranking.
        if (diff == Dificultad.CLASIFICACION && nombreJugador != null) {
            rankingRepositorio.guardar(nombreJugador, juego.getPuntuacion(), diff.name());
            actualizarTop5();
        }

        JOptionPane.showMessageDialog(this,
                "Chocaste. Puntuación: " + juego.getPuntuacion() + " puntos.",
                "Fin de la carrera", JOptionPane.INFORMATION_MESSAGE);

        etiquetaRecord.setText("Récord: " + juego.getRecord());
    }

    private int aPixelY(double yLogica) {
        double ratio = yLogica / JuegoArcade.LARGO_PISTA;
        return (int) (ratio * (ALTO_PISTA - MARGEN_INFERIOR));
    }

    private class PanelPista extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(TemaF1.ASFALTO);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(0xCCCCCC));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < JuegoArcade.CANTIDAD_CARRILES; i++) {
                g2.drawLine(i * ANCHO_CARRIL, 0, i * ANCHO_CARRIL, getHeight());
            }

            int yMeta = aPixelY(JuegoArcade.PARTE_SUPERIOR_COCHE);
            g2.setColor(new Color(0xFFFFFF));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(0, yMeta, getWidth(), yMeta);

            int xCoche = juego.getCarrilCoche() * ANCHO_CARRIL + (ANCHO_CARRIL - ANCHO_COCHE) / 2;
            int yCoche = yMeta;

            if (!juego.estaInmune() || (juego.getPuntuacion() % 2 == 0)) {
                g2.setColor(colorEscudo);
                g2.fillRoundRect(xCoche, yCoche, ANCHO_COCHE, ALTO_COCHE_PX, 14, 14);
            }

            g2.setColor(new Color(0xCC3333));
            for (Obstaculo obstaculo : juego.getObstaculos()) {
                int y = aPixelY(obstaculo.getY());
                int x = obstaculo.getCarril() * ANCHO_CARRIL + (ANCHO_CARRIL - ANCHO_OBSTACULO) / 2;
                g2.fillRoundRect(x, y, ANCHO_OBSTACULO, ALTO_OBSTACULO_PX, 6, 6);
            }

            if (estado == Estado.INICIO) {
                dibujarMensaje(g2, "Formulemon Arcade",
                        "Usa ← → o A/D para moverte",
                        "Pulsa Iniciar o Espacio para jugar");
            } else if (estado == Estado.PAUSA) {
                dibujarMensaje(g2, "Pausa", "Pulsa P o Reanudar para seguir", null);
            }
        }

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
                g2.drawString(texto,
                        (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2,
                        getHeight() / 2 + 20);
            }
            if (pie != null) {
                g2.drawString(pie,
                        (getWidth() - g2.getFontMetrics().stringWidth(pie)) / 2,
                        getHeight() / 2 + 48);
            }
        }
    }

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
