package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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

import net.miginfocom.swing.MigLayout;

public class VentanaSimulacion extends JFrame {

    private static final double PASO_SIMULADO = 10.0;
    private static final int ESPERA_TICK_MS = 500;
    private static final Color COLOR_DNF = new Color(0xFF6B5E);
    private static final Color COLOR_VUELTA_RAPIDA = new Color(0xF7C948);

    private final CircuitoServicio circuitoServicio;
    private final VehiculoServicio vehiculoServicio;
    private final SimulacionService simulacionService;

    private JComboBox<Circuito> comboCircuitos;
    private JComboBox<CompuestoNeumatico> comboCompuesto;
    private JComboBox<Integer> comboVueltas;
    private JLabel etiquetaClima;
    private int consultaClimaId;

    private JButton btnIniciar;
    private JButton btnClasificacion;
    private JProgressBar barProgreso;

    private JTable tablaRanking;
    private DefaultTableModel modeloRanking;
    private JTextPane areaEventos;
    private List<Vehiculo> parrillaActual;
    private CarreraEnVivo carrera;

    private volatile boolean carreraPausada;
    private volatile int multiplicadorVelocidad = 1;
    private Thread hiloCarrera;
    private JButton btnPausa;
    private JButton btn1x;
    private JButton btn2x;
    private JButton btn4x;

    private JLabel lblTelemetriaVelocidad;
    private JLabel lblTelemetriaDesgaste;
    private JLabel lblTelemetriaCompuesto;
    private JLabel lblTelemetriaParadas;
    private JLabel lblTelemetriaUltimaVuelta;

    private JFreeChart graficaVelocidad;
    private XYSeriesCollection graficaDataset;
    private int tiempoGrafica;

    public VentanaSimulacion(CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {
        this.circuitoServicio = Objects.requireNonNull(circuitoServicio, "Los servicios no pueden ser nulos.");
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio, "Los servicios no pueden ser nulos.");
        this.simulacionService = Objects.requireNonNull(simulacionService, "Los servicios no pueden ser nulos.");

        setTitle("Simulador de Fórmula 1");
        setSize(960, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16", "[]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Simulador de Fórmula 1"));
        add(cabecera, BorderLayout.NORTH);

        JPanel zonaCentral = new JPanel(new MigLayout("insets 0 8 0 8, fill", "[grow]", "[][grow][]"));
        zonaCentral.setBackground(TemaF1.FONDO);

        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Configura tu carrera y presiona Iniciar");
        barProgreso.setPreferredSize(new Dimension(0, 28));
        zonaCentral.add(barProgreso, "growx, wrap");

        JSplitPane panelCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                construirPanelRanking(), construirPanelEventos());
        panelCentral.setResizeWeight(0.55);
        panelCentral.setDividerLocation(320);
        panelCentral.setBorder(null);
        zonaCentral.add(panelCentral, "grow, wrap");

        JPanel panelControles = construirPanelControles();
        zonaCentral.add(panelControles, "growx");

        add(zonaCentral, BorderLayout.CENTER);

        JPanel configuracion = construirPanelConfiguracion();
        add(configuracion, BorderLayout.SOUTH);

        conectarAcciones();

        anadirEvento("Bienvenido al simulador.", TemaF1.TEXTO);
        anadirEvento("1. Elige circuito, compuesto y vueltas.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("   El clima se obtiene automáticamente de la zona del circuito.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("2. Ejecuta la clasificación para definir la parrilla.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("3. Presiona Iniciar carrera: corren todos los autos en vivo.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

        actualizarClimaDelCircuito();
    }

    private JPanel construirPanelConfiguracion() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 8 12 8 12, gap 8",
                "[][grow,fill][][grow,fill][][grow,fill]",
                "[]4[]"));
        panel.setBackground(TemaF1.FONDO);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE));

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

        comboCompuesto = new JComboBox<>(CompuestoNeumatico.values());
        comboCompuesto.setSelectedItem(CompuestoNeumatico.MEDIO);

        comboVueltas = new JComboBox<>(new Integer[]{3, 5, 10, 20, 40});
        comboVueltas.setSelectedItem(10);

        btnClasificacion = new JButton(TemaF1.icono("flag"));
        btnClasificacion.setText(" Clasificación");
        TemaF1.estilizarBoton(btnClasificacion);

        btnIniciar = new JButton(TemaF1.icono("play"));
        btnIniciar.setText(" Iniciar carrera");
        TemaF1.estilizarBoton(btnIniciar);

        panel.add(TemaF1.etiqueta("Circuito:"));
        panel.add(comboCircuitos);
        panel.add(TemaF1.etiqueta("Clima:"));
        panel.add(etiquetaClima);
        panel.add(btnClasificacion, "w 140!");
        panel.add(btnIniciar, "w 140!, wrap");

        panel.add(TemaF1.etiqueta("Compuesto:"));
        panel.add(comboCompuesto);
        panel.add(TemaF1.etiqueta("Vueltas:"));
        panel.add(comboVueltas);

        return panel;
    }

    private JPanel construirPanelRanking() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[][grow][]"));

        JPanel tituloPanel = new JPanel(new MigLayout("insets 4 8 4 8", "[]"));
        tituloPanel.setBackground(TemaF1.PANEL);
        tituloPanel.add(TemaF1.subtitulo("Clasificación en vivo"));
        panel.add(tituloPanel, "growx, wrap");

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
        panel.add(scroll, "grow, wrap");

        JPanel panelTelemetria = construirPanelTelemetria();
        panel.add(panelTelemetria, "growx");

        return panel;
    }

    private JPanel construirPanelTelemetria() {
        JPanel panel = new JPanel(new MigLayout("insets 6 12 4 12, gap 16", "[][][][][]"));
        panel.setBackground(TemaF1.PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE));

        Font fuenteMono = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        lblTelemetriaVelocidad = TemaF1.etiqueta("Vel:  ---- km/h");
        lblTelemetriaVelocidad.setFont(fuenteMono);
        lblTelemetriaVelocidad.setPreferredSize(new Dimension(130, 18));

        lblTelemetriaDesgaste = TemaF1.etiqueta("Desg: ---.- %%");
        lblTelemetriaDesgaste.setFont(fuenteMono);
        lblTelemetriaDesgaste.setPreferredSize(new Dimension(110, 18));

        lblTelemetriaCompuesto = TemaF1.etiqueta("Comp: ------");
        lblTelemetriaCompuesto.setFont(fuenteMono);
        lblTelemetriaCompuesto.setPreferredSize(new Dimension(120, 18));

        lblTelemetriaParadas = TemaF1.etiqueta("Pits: --");
        lblTelemetriaParadas.setFont(fuenteMono);
        lblTelemetriaParadas.setPreferredSize(new Dimension(70, 18));

        lblTelemetriaUltimaVuelta = TemaF1.etiqueta("Vuelta: --.-- s");
        lblTelemetriaUltimaVuelta.setFont(fuenteMono);
        lblTelemetriaUltimaVuelta.setPreferredSize(new Dimension(130, 18));

        panel.add(lblTelemetriaVelocidad);
        panel.add(lblTelemetriaDesgaste);
        panel.add(lblTelemetriaCompuesto);
        panel.add(lblTelemetriaParadas);
        panel.add(lblTelemetriaUltimaVuelta);

        tablaRanking.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarTelemetria();
            }
        });

        return panel;
    }

    private JPanel construirPanelEventos() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[][grow]"));

        JPanel tituloPanel = new JPanel(new MigLayout("insets 4 8 4 8", "[]"));
        tituloPanel.setBackground(TemaF1.PANEL);
        tituloPanel.add(TemaF1.subtitulo("Eventos de la carrera"));
        panel.add(tituloPanel, "growx, wrap");

        areaEventos = new JTextPane();
        areaEventos.setEditable(false);
        areaEventos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaEventos.setBackground(TemaF1.PANEL);
        areaEventos.setForeground(TemaF1.TEXTO);

        JScrollPane scroll = new JScrollPane(areaEventos);
        scroll.setBorder(BorderFactory.createLineBorder(TemaF1.BORDE, 1));
        panel.add(scroll, "grow");

        return panel;
    }

    private JPanel construirPanelControles() {
        JPanel panel = new JPanel(new MigLayout("insets 6 8 6 8, gap 4", "[][][][][][]", "[][grow]"));
        panel.setBackground(TemaF1.FONDO);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaF1.BORDE));

        btnPausa = new JButton(TemaF1.icono("pause"));
        btnPausa.setText(" Pausa");
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
        panel.add(btnPausa, "w 100!");
        panel.add(TemaF1.etiqueta("Velocidad:"));
        panel.add(btn1x, "w 50!");
        panel.add(btn2x, "w 50!");
        panel.add(btn4x, "w 50!");

        JPanel panelGrafica = construirPanelGrafica();
        panel.add(panelGrafica, "span, growx, gap top 8");

        return panel;
    }

    private JPanel construirPanelGrafica() {
        graficaDataset = new XYSeriesCollection();
        graficaVelocidad = TemaF1.crearGraficaVelocidad("Velocidad en vivo");

        ChartPanel chartPanel = new ChartPanel(graficaVelocidad);
        chartPanel.setPreferredSize(new Dimension(400, 180));
        chartPanel.setBackground(TemaF1.PANEL);

        JPanel panel = new JPanel(new MigLayout("insets 4, fill", "[grow]", "[grow]"));
        panel.setBackground(TemaF1.PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TemaF1.BORDE, 1),
                " Gráfica de velocidad ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                panel.getFont().deriveFont(Font.BOLD, 12f),
                TemaF1.TEXTO_SECUNDARIO));
        panel.add(chartPanel, "grow");

        return panel;
    }

    private void togglePausa() {
        carreraPausada = !carreraPausada;
        btnPausa.setText(carreraPausada ? " Reanudar" : " Pausa");
        btnPausa.setIcon(carreraPausada ? TemaF1.icono("play") : TemaF1.icono("pause"));
    }

    private void setVelocidad(int multiplicador) {
        multiplicadorVelocidad = multiplicador;
        btn1x.setEnabled(multiplicadorVelocidad != 1);
        btn2x.setEnabled(multiplicadorVelocidad != 2);
        btn4x.setEnabled(multiplicadorVelocidad != 4);
    }

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

        graficaDataset.removeAllSeries();
        tiempoGrafica = 0;
        for (Vehiculo v : parrilla) {
            XYSeries serie = new XYSeries(v.getMarcaEscuderia());
            graficaDataset.addSeries(serie);
        }

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
                        actualizarGrafica();
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

    private void actualizarGrafica() {
        if (carrera == null) return;
        tiempoGrafica++;

        List<AutoEnCarrera> ranking = carrera.ranking();
        for (int i = 0; i < ranking.size() && i < graficaDataset.getSeriesCount(); i++) {
            AutoEnCarrera auto = ranking.get(i);
            XYSeries serie = graficaDataset.getSeries(i);
            serie.add(tiempoGrafica, auto.getVelocidadActual());
        }
    }

    private void actualizarTelemetria() {
        if (carrera == null) return;

        int filaSeleccionada = tablaRanking.getSelectedRow();
        if (filaSeleccionada < 0) {
            lblTelemetriaVelocidad.setText("Vel:  ---- km/h");
            lblTelemetriaDesgaste.setText("Desg: ---.- %%");
            lblTelemetriaCompuesto.setText("Comp: ------");
            lblTelemetriaParadas.setText("Pits: --");
            lblTelemetriaUltimaVuelta.setText("Vuelta: --.-- s");
            return;
        }

        List<AutoEnCarrera> ranking = carrera.ranking();
        if (filaSeleccionada < ranking.size()) {
            AutoEnCarrera auto = ranking.get(filaSeleccionada);

            lblTelemetriaVelocidad.setText(String.format("Vel: %4.0f km/h", auto.getVelocidadActual()));
            lblTelemetriaDesgaste.setText(String.format("Desg: %5.1f %%", auto.getDesgaste()));
            lblTelemetriaCompuesto.setText(String.format("Comp: %-6s", auto.getCompuesto().getEtiqueta()));
            lblTelemetriaParadas.setText(String.format("Pits: %2d", auto.getParadas()));

            String ultimaVuelta = auto.getHoraUltimaVuelta() > 0
                    ? String.format(Locale.US, "%5.2f s", auto.getHoraUltimaVuelta())
                    : "--.-- s";
            lblTelemetriaUltimaVuelta.setText("Vuelta: " + ultimaVuelta);
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
            btnPausa.setText(" Pausa");
            btnPausa.setIcon(TemaF1.icono("pause"));
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
