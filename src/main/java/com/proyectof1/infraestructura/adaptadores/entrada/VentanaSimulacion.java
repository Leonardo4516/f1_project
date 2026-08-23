package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.CarreraEnVivo;
import com.proyectof1.aplicacion.servicios.CarreraEnVivo.AutoEnCarrera;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.dominio.Circuito;
import com.proyectof1.dominio.CompuestoNeumatico;
import com.proyectof1.dominio.ConfiguracionCarrera;
import com.proyectof1.dominio.ResultadoCarrera;
import com.proyectof1.dominio.ResultadoParticipante;
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana de simulación de carreras (adaptador de entrada en Swing).
 * Permite configurar la carrera (circuito, clima, compuesto de neumáticos y
 * vueltas), ver la parrilla de salida tras una clasificación y correr una
 * carrera en vivo con toda la parrilla: clasificación actualizada por tick,
 * eventos (paradas, abandonos, vuelta rápida) y resultado final con el
 * ganador. Mantiene la arquitectura hexagonal: toda la lógica vive en
 * {@link CarreraEnVivo} (capa de aplicación) y esta ventana solo presenta.
 */
public class VentanaSimulacion extends JFrame {

    // Segundos simulados que se avanzan en cada tick de la interfaz (ritmo de la demo).
    private static final double PASO_SIMULADO = 10.0;

    // Pausa real entre ticks para que el ojo pueda seguir la carrera.
    private static final int ESPERA_TICK_MS = 500;

    // Colores temáticos propios de la ventana (abandonos y vuelta rápida).
    private static final Color COLOR_DNF = new Color(0xFF6B5E);
    private static final Color COLOR_VUELTA_RAPIDA = new Color(0xF7C948);

    // Servicios inyectados.
    private final CircuitoServicio circuitoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;

    // Desplegables para elegir circuito y condiciones.
    private JComboBox<Circuito> comboCircuitos;
    private JComboBox<CompuestoNeumatico> comboCompuesto;
    private JComboBox<Integer> comboVueltas;

    // Etiqueta que muestra el clima real que devolvió la API para la zona del circuito.
    private JLabel etiquetaClima;

    // Contador para descartar consultas de clima obsoletas.
    private int consultaClimaId;

    // Botones de acción.
    private JButton btnIniciar;
    private JButton btnClasificacion;

    // Barra de progreso que muestra el avance de la carrera.
    private JProgressBar barProgreso;

    // Tabla con la clasificación en vivo y su modelo (no editable).
    private JTable tablaRanking;
    private DefaultTableModel modeloRanking;

    // Registro de eventos de la carrera con colores temáticos.
    private JTextPane areaEventos;

    // Parrilla de salida calculada en la clasificación.
    private List<Vehiculo> parrillaActual;

    // Motor de la carrera en curso (null fuera de una carrera).
    private CarreraEnVivo carrera;

    // Control de velocidad y pausa de la carrera (volatile: compartidos entre
    // hiloCarrera y el EDT de Swing).
    private volatile boolean carreraPausada;
    private volatile int multiplicadorVelocidad = 1;
    private Thread hiloCarrera;
    private JButton btnPausa;
    private JButton btn1x;
    private JButton btn2x;
    private JButton btn4x;

    // Etiquetas de telemetría del auto seleccionado.
    private JLabel lblTelemetriaVelocidad;
    private JLabel lblTelemetriaDesgaste;
    private JLabel lblTelemetriaCompuesto;
    private JLabel lblTelemetriaParadas;
    private JLabel lblTelemetriaUltimaVuelta;

    /**
     * Constructor de la ventana. Recibe los servicios y valida que no sean nulos.
     */
    public VentanaSimulacion(CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        this.circuitoServicio = Objects.requireNonNull(circuitoServicio, "Los servicios no pueden ser nulos.");
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio, "Los servicios no pueden ser nulos.");
        this.simulacionService = Objects.requireNonNull(simulacionService, "Los servicios no pueden ser nulos.");

        setTitle("Simulador de Fórmula 1");
        setSize(920, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // ===== NORTE: Cabecera con título =====
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Simulador de Fórmula 1"));
        add(cabecera, BorderLayout.NORTH);

        // ===== CENTRO: Zona principal de carrera =====
        JPanel zonaCentral = new JPanel(new BorderLayout(0, 0));
        zonaCentral.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // Barra de progreso
        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Configura tu carrera y presiona Iniciar");
        barProgreso.setPreferredSize(new Dimension(0, 28));
        zonaCentral.add(barProgreso, BorderLayout.NORTH);

        // Split pane: ranking arriba, eventos abajo
        JSplitPane panelCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                construirPanelRanking(), construirPanelEventos());
        panelCentral.setResizeWeight(0.55);
        panelCentral.setDividerLocation(320);
        panelCentral.setBorder(null);
        zonaCentral.add(panelCentral, BorderLayout.CENTER);

        // Panel de controles de pausa/velocidad debajo del split
        JPanel panelControles = construirPanelControles();
        zonaCentral.add(panelControles, BorderLayout.SOUTH);

        add(zonaCentral, BorderLayout.CENTER);

        // ===== SUR: Configuración de carrera =====
        JPanel configuracion = construirPanelConfiguracion();
        add(configuracion, BorderLayout.SOUTH);

        // ---- Acciones. ----
        conectarAcciones();

        // Mensaje de bienvenida
        anadirEvento("Bienvenido al simulador.", TemaF1.TEXTO);
        anadirEvento("1. Elige circuito, compuesto y vueltas.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("   El clima se obtiene automáticamente de la zona del circuito.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("2. Ejecuta la clasificación para definir la parrilla.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("3. Presiona Iniciar carrera: corren todos los autos en vivo.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

        actualizarClimaDelCircuito();

    }

    // =====================================================================
    //  CONSTRUCCIÓN DE PANELES
    // =====================================================================

    /** Panel de configuración: circuito, compuesto, vueltas y botones principales. */
    private JPanel construirPanelConfiguracion() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(TemaF1.FONDO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE),
                TemaF1.margenes(10, 12, 12, 12)));

        // --- Fila 1: Circuito + Clima ---
        JPanel filaCircuito = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filaCircuito.setBackground(TemaF1.FONDO);

        comboCircuitos = new JComboBox<>();
        for (Circuito circuito : circuitoServicio.listarCircuitos()) {
            comboCircuitos.addItem(circuito);
        }
        comboCircuitos.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                actualizarClimaDelCircuito();
            }
        });

        etiquetaClima = TemaF1.etiqueta("--");

        filaCircuito.add(TemaF1.etiqueta("Circuito:"));
        filaCircuito.add(comboCircuito);
        filaCircuito.add(Box.createHorizontalStrut(20));
        filaCircuito.add(TemaF1.etiqueta("Clima:"));
        filaCircuito.add(etiquetaClima);

        // --- Fila 2: Compuesto + Vueltas + Botones ---
        JPanel filaEstrategia = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filaEstrategia.setBackground(TemaF1.FONDO);

        comboCompuesto = new JComboBox<>(CompuestoNeumatico.values());
        comboCompuesto.setSelectedItem(CompuestoNeumatico.MEDIO);

        comboVueltas = new JComboBox<>(new Integer[]{3, 5, 10, 20, 40});
        comboVueltas.setSelectedItem(10);

        btnClasificacion = new JButton("Clasificación");
        TemaF1.estilizarBoton(btnClasificacion);

        btnIniciar = new JButton("Iniciar carrera");
        TemaF1.estilizarBoton(btnIniciar);

        filaEstrategia.add(TemaF1.etiqueta("Compuesto:"));
        filaEstrategia.add(comboCompuesto);
        filaEstrategia.add(Box.createHorizontalStrut(10));
        filaEstrategia.add(TemaF1.etiqueta("Vueltas:"));
        filaEstrategia.add(comboVueltas);
        filaEstrategia.add(Box.createHorizontalStrut(20));
        filaEstrategia.add(btnClasificacion);
        filaEstrategia.add(Box.createHorizontalStrut(8));
        filaEstrategia.add(btnIniciar);

        panel.add(filaCircuito);
        panel.add(filaEstrategia);

        return panel;
    }

    /** Panel de clasificación en vivo + telemetría. */
    private JPanel construirPanelRanking() {

        JPanel panel = new JPanel(new BorderLayout(0, 0));

        JPanel tituloPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tituloPanel.setBackground(TemaF1.PANEL);
        tituloPanel.add(TemaF1.subtitulo("Clasificación en vivo"));
        panel.add(tituloPanel, BorderLayout.NORTH);

        modeloRanking = new DefaultTableModel(
                new String[]{"Pos", "Escudería", "Piloto", "Gap", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaRanking = new JTable(modeloRanking);
        tablaRanking.setRowHeight(26);
        tablaRanking.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRanking.setFillsViewportHeight(true);
        tablaRanking.setDefaultRenderer(Object.class, new RendererRanking());
        tablaRanking.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaRanking.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaRanking.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaRanking.getColumnModel().getColumn(3).setPreferredWidth(70);
        tablaRanking.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(tablaRanking);
        scroll.setBorder(BorderFactory.createLineBorder(TemaF1.BORDE, 1));
        panel.add(scroll, BorderLayout.CENTER);

        panel.add(construirPanelTelemetria(), BorderLayout.SOUTH);

        return panel;
    }

    /** Panel de telemetría del auto seleccionado. */
    private JPanel construirPanelTelemetria() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE),
                TemaF1.margenes(6, 4, 4, 4)));
        panel.setBackground(TemaF1.PANEL);

        lblTelemetriaVelocidad = TemaF1.etiqueta("Vel: -- km/h");
        lblTelemetriaDesgaste = TemaF1.etiqueta("Desgaste: -- %");
        lblTelemetriaCompuesto = TemaF1.etiqueta("Compuesto: --");
        lblTelemetriaParadas = TemaF1.etiqueta("Paradas: --");
        lblTelemetriaUltimaVuelta = TemaF1.etiqueta("Última vuelta: --");

        panel.add(lblTelemetriaVelocidad);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(lblTelemetriaDesgaste);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(lblTelemetriaCompuesto);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(lblTelemetriaParadas);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(lblTelemetriaUltimaVuelta);

        tablaRanking.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarTelemetria();
            }
        });

        return panel;
    }

    /** Panel de eventos de la carrera. */
    private JPanel construirPanelEventos() {

        JPanel panel = new JPanel(new BorderLayout(0, 0));

        JPanel tituloPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tituloPanel.setBackground(TemaF1.PANEL);
        tituloPanel.add(TemaF1.subtitulo("Eventos de la carrera"));
        panel.add(tituloPanel, BorderLayout.NORTH);

        areaEventos = new JTextPane();
        areaEventos.setEditable(false);
        areaEventos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaEventos.setBackground(TemaF1.PANEL);
        areaEventos.setForeground(TemaF1.TEXTO);

        JScrollPane scroll = new JScrollPane(areaEventos);
        scroll.setBorder(BorderFactory.createLineBorder(TemaF1.BORDE, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /** Panel de controles de pausa y velocidad durante la carrera. */
    private JPanel construirPanelControles() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        panel.setBackground(TemaF1.FONDO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE),
                TemaF1.margenes(4, 4, 4, 4)));

        btnPausa = new JButton("⏸ Pausa");
        TemaF1.estilizarBoton(btnPausa);
        btnPausa.setEnabled(false);

        btn1x = new JButton("1x");
        btn2x = new JButton("2x");
        btn4x = new JButton("4x");
        TemaF1.estilizarBoton(btn1x);
        TemaF1.estilizarBoton(btn2x);
        TemaF1.estilizarBoton(btn4x);
        btn1x.setEnabled(false);
        btn2x.setEnabled(false);
        btn4x.setEnabled(false);

        btnPausa.addActionListener(e -> togglePausa());
        btn1x.addActionListener(e -> setVelocidad(1));
        btn2x.addActionListener(e -> setVelocidad(2));
        btn4x.addActionListener(e -> setVelocidad(4));

        panel.add(TemaF1.etiqueta("Control:"));
        panel.add(btnPausa);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(TemaF1.etiqueta("Velocidad:"));
        panel.add(btn1x);
        panel.add(btn2x);
        panel.add(btn4x);

        return panel;
    }

    // =====================================================================
    //  LÓGICA DE CONTROLES
    // =====================================================================

    private void togglePausa() {
        carreraPausada = !carreraPausada;
        btnPausa.setText(carreraPausada ? "▶ Reanudar" : "⏸ Pausa");
    }

    private void setVelocidad(int multiplicador) {
        multiplicadorVelocidad = multiplicador;
        btn1x.setEnabled(multiplicadorVelocidad != 1);
        btn2x.setEnabled(multiplicadorVelocidad != 2);
        btn4x.setEnabled(multiplicadorVelocidad != 4);
    }

    // =====================================================================
    //  ACCIONES
    // =====================================================================

    private void conectarAcciones() {

        btnClasificacion.addActionListener(e -> {

            Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
            List<Vehiculo> vehiculos = vehiculoServicio.listarVehiculos();

            if (circuito == null || vehiculos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Registra circuitos y vehículos antes de clasificar.");
                return;
            }

            try {
                String clima = simulacionService.resolverClima(circuito, ConfiguracionCarrera.CLIMA_AUTO);
                parrillaActual = simulacionService.simularClasificacion(vehiculos, circuito, clima);

                anadirEvento("=== CLASIFICACIÓN (" + circuito.getNombre() + ") - Clima: " + clima + " ===", TemaF1.ROJO_F1);

                for (int i = 0; i < parrillaActual.size(); i++) {
                    Vehiculo v = parrillaActual.get(i);
                    anadirEvento(String.format("%2d. %-16s (%s)", i + 1, v.getPiloto().getNombre(), v.getMarcaEscuderia()),
                            i == 0 ? TemaF1.ROJO_F1 : TemaF1.TEXTO);
                }

                anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en la clasificación: " + ex.getMessage(),
                        "Clasificación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnIniciar.addActionListener(e -> iniciarCarrera());
    }

    /** Prepara la carrera y lanza el hilo que la va avanzando por ticks. */
    private void iniciarCarrera() {

        Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
        if (circuito == null) {
            JOptionPane.showMessageDialog(this, "No hay circuitos registrados.");
            return;
        }

        List<Vehiculo> vehiculos = vehiculoServicio.listarVehiculos();
        if (vehiculos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay vehículos registrados.");
            return;
        }

        String clima;
        try {
            clima = simulacionService.resolverClima(circuito, ConfiguracionCarrera.CLIMA_AUTO);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo consultar el clima: " + ex.getMessage(),
                    "Clima", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Vehiculo> parrilla = (parrillaActual != null && !parrillaActual.isEmpty())
                ? new ArrayList<>(parrillaActual)
                : simulacionService.simularClasificacion(vehiculos, circuito, clima);

        CompuestoNeumatico compuesto = (CompuestoNeumatico) comboCompuesto.getSelectedItem();
        Map<Vehiculo, CompuestoNeumatico> compuestos = new HashMap<>();
        for (Vehiculo vehiculo : parrilla) {
            compuestos.put(vehiculo, compuesto);
        }

        int vueltas = (Integer) comboVueltas.getSelectedItem();
        carrera = new CarreraEnVivo(parrilla, circuito, clima, compuestos, vueltas);

        modeloRanking.setRowCount(0);
        areaEventos.setText("");
        barProgreso.setValue(0);
        barProgreso.setString("Carrera en curso...");

        anadirEvento("=== CARRERA: " + circuito.getNombre() + " (" + circuito.getUbicacion() + ") ===", TemaF1.ROJO_F1);
        anadirEvento("Clima: " + clima + " | Compuesto: " + compuesto.getEtiqueta() + " | Vueltas: " + vueltas, TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

        deshabilitarControles(true);
        habilitarControlesCarrera(true);

        hiloCarrera = new Thread(() -> {
            int[] ultimoEvento = {0};

            try {
                while (!carrera.estaFinalizada()) {
                    if (!carreraPausada) {
                        double paso = PASO_SIMULADO * multiplicadorVelocidad;
                        carrera.avanzar(paso);
                    }

                    List<String> eventos = carrera.getEventos();

                    SwingUtilities.invokeLater(() -> {
                        actualizarRanking();
                        for (int i = ultimoEvento[0]; i < eventos.size(); i++) {
                            anadirEvento(eventos.get(i), colorDeEvento(eventos.get(i)));
                        }
                        ultimoEvento[0] = eventos.size();
                    });

                    Thread.sleep(ESPERA_TICK_MS);
                }

                ResultadoCarrera resultado = carrera.resultadoFinal();

                SwingUtilities.invokeLater(() -> {
                    actualizarRanking();
                    mostrarResultado(resultado);
                    deshabilitarControles(false);
                    habilitarControlesCarrera(false);
                });

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> deshabilitarControles(false));
            }
        });
        hiloCarrera.start();
    }

    // =====================================================================
    //  ACTUALIZACIÓN DE ESTADO
    // =====================================================================

    private void actualizarRanking() {
        modeloRanking.setRowCount(0);
        if (carrera == null) return;

        int posicion = 1;
        for (AutoEnCarrera auto : carrera.ranking()) {
            String estado = auto.isDnf() ? "DNF"
                    : auto.estaEnPits() ? "En pits" : "En pista";
            String gap = posicion == 1 ? "--" : String.format("%+.1f s", carrera.gapAlLider(auto));

            modeloRanking.addRow(new Object[]{
                posicion,
                auto.getVehiculo().getMarcaEscuderia(),
                auto.getVehiculo().getPiloto().getNombre(),
                gap,
                estado
            });
            posicion++;
        }

        double progreso = carrera.progresoPorcentaje();
        barProgreso.setValue((int) Math.round(progreso));

        if (carrera.estaFinalizada()) {
            barProgreso.setString("Carrera finalizada");
        } else {
            barProgreso.setString("Vuelta " + carrera.vueltaDelLider() + " de " + carrera.getVueltasTotales());
        }

        actualizarTelemetria();
    }

    private void actualizarTelemetria() {
        if (carrera == null) return;

        int filaSeleccionada = tablaRanking.getSelectedRow();
        if (filaSeleccionada < 0) {
            lblTelemetriaVelocidad.setText("Vel: -- km/h");
            lblTelemetriaDesgaste.setText("Desgaste: -- %");
            lblTelemetriaCompuesto.setText("Compuesto: --");
            lblTelemetriaParadas.setText("Paradas: --");
            lblTelemetriaUltimaVuelta.setText("Última vuelta: --");
            return;
        }

        List<AutoEnCarrera> ranking = carrera.ranking();
        if (filaSeleccionada < ranking.size()) {
            AutoEnCarrera auto = ranking.get(filaSeleccionada);

            lblTelemetriaVelocidad.setText(String.format("Vel: %.0f km/h", auto.getVelocidadActual()));
            lblTelemetriaDesgaste.setText(String.format("Desgaste: %.1f %%", auto.getDesgaste()));
            lblTelemetriaCompuesto.setText("Compuesto: " + auto.getCompuesto().getEtiqueta());
            lblTelemetriaParadas.setText("Paradas: " + auto.getParadas());

            String ultimaVuelta = auto.getHoraUltimaVuelta() > 0
                    ? String.format(Locale.US, "%.2f s", auto.getHoraUltimaVuelta())
                    : "--";
            lblTelemetriaUltimaVuelta.setText("Última vuelta: " + ultimaVuelta);
        }
    }

    private void mostrarResultado(ResultadoCarrera resultado) {
        ResultadoParticipante ganador = resultado.ganador();

        StringBuilder resumen = new StringBuilder("===== RESULTADO FINAL =====\n");
        if (ganador != null) {
            resumen.append("Ganador: ").append(ganador.vehiculo().getMarcaEscuderia())
                    .append(" (").append(ganador.vehiculo().getPiloto().getNombre()).append(")\n");
        }

        ResultadoParticipante vueltaRapida = resultado.autorVueltaRapida();
        if (vueltaRapida != null) {
            resumen.append("Vuelta rápida: ").append(vueltaRapida.vehiculo().getPiloto().getNombre())
                    .append(" (").append(vueltaRapida.vehiculo().getMarcaEscuderia()).append(")\n");
        }

        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento(resumen.toString(), TemaF1.ROJO_F1);

        for (ResultadoParticipante participante : resultado.participantes()) {
            anadirEvento(String.format("%2d. %-14s (%s) - %s",
                    participante.posicion(),
                    participante.vehiculo().getMarcaEscuderia(),
                    participante.vehiculo().getPiloto().getNombre(),
                    participante.estado()),
                    TemaF1.TEXTO);
        }

        barProgreso.setValue(100);
        JOptionPane.showMessageDialog(this, resumen.toString(),
                "Resultado de la carrera", JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    //  UTILIDADES
    // =====================================================================

    private Color colorDeEvento(String evento) {
        if (evento.startsWith("ABANDONO:")) {
            return COLOR_DNF;
        }
        if (evento.startsWith("Parada en boxes:") || evento.startsWith("Salida de boxes:")) {
            String escuderia = evento.substring(evento.indexOf(": ") + 2, evento.indexOf(" ("));
            return TemaF1.colorDeEscuderia(escuderia);
        }
        if (evento.startsWith("Vuelta rápida:")) {
            return COLOR_VUELTA_RAPIDA;
        }
        return TemaF1.TEXTO;
    }

    private void anadirEvento(String texto, Color color) {
        StyledDocument documento = areaEventos.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);
        StyleConstants.setFontFamily(atributos, Font.MONOSPACED);
        StyleConstants.setFontSize(atributos, 13);

        try {
            documento.insertString(documento.getLength(), texto + "\n", atributos);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void deshabilitarControles(boolean deshabilitado) {
        btnIniciar.setEnabled(!deshabilitado);
        btnClasificacion.setEnabled(!deshabilitado);
        comboCircuitos.setEnabled(!deshabilitado);
        comboCompuesto.setEnabled(!deshabilitado);
        comboVueltas.setEnabled(!deshabilitado);
    }

    private void habilitarControlesCarrera(boolean habilitado) {
        btnPausa.setEnabled(habilitado);
        btn1x.setEnabled(habilitado && multiplicadorVelocidad != 1);
        btn2x.setEnabled(habilitado && multiplicadorVelocidad != 2);
        btn4x.setEnabled(habilitado && multiplicadorVelocidad != 4);

        if (!habilitado) {
            carreraPausada = false;
            multiplicadorVelocidad = 1;
            btnPausa.setText("⏸ Pausa");
        }
    }

    private void actualizarClimaDelCircuito() {
        Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();
        if (circuito == null) {
            etiquetaClima.setText("--");
            return;
        }

        int generacion = ++consultaClimaId;
        etiquetaClima.setText("Consultando...");

        new Thread(() -> {
            String clima = simulacionService.resolverClima(circuito, ConfiguracionCarrera.CLIMA_AUTO);
            SwingUtilities.invokeLater(() -> {
                if (generacion == consultaClimaId && circuito == comboCircuitos.getSelectedItem()) {
                    etiquetaClima.setText(clima);
                }
            });
        }).start();
    }

    // =====================================================================
    //  RENDERER DE LA TABLA
    // =====================================================================

    private class RendererRanking extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccion, boolean tieneFoco, int fila, int columna) {

            JLabel celda = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccion, tieneFoco, fila, columna);

            Object posicionValor = tabla.getModel().getValueAt(fila, 0);
            int posicion = (posicionValor instanceof Integer) ? (Integer) posicionValor : 0;
            String estado = String.valueOf(tabla.getModel().getValueAt(fila, 4));

            celda.setBackground(TemaF1.PANEL);
            celda.setForeground(TemaF1.TEXTO);

            celda.setHorizontalAlignment(columna == 0 || columna == 3
                    ? javax.swing.SwingConstants.CENTER
                    : javax.swing.SwingConstants.LEFT);

            if (posicion == 1) {
                celda.setBackground(TemaF1.ROJO_F1);
                celda.setForeground(Color.WHITE);
            } else if ("DNF".equals(estado)) {
                celda.setBackground(new Color(0x2A1A1A));
                celda.setForeground(COLOR_DNF);
            }

            if (columna == 1 && posicion != 1) {
                celda.setForeground(TemaF1.colorDeEscuderia(String.valueOf(valor)));
            }

            return celda;
        }
    }

}
