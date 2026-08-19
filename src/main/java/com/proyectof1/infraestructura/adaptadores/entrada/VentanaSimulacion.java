package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // Contador para descartar consultas de clima obsoletas (si el usuario cambia
    // de circuito rápido, la respuesta de un circuito anterior no pisa la actual).
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

    // Parrilla de salida calculada en la clasificación (si no se hizo, se
    // genera automáticamente al iniciar la carrera).
    private List<Vehiculo> parrillaActual;

    // Motor de la carrera en curso (null fuera de una carrera).
    private CarreraEnVivo carrera;

    /**
     * Constructor de la ventana. Recibe los servicios y valida que no sean nulos.
     */
    public VentanaSimulacion(CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio, SimulacionService simulacionService) {

        this.circuitoServicio = Objects.requireNonNull(circuitoServicio, "Los servicios no pueden ser nulos.");
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio, "Los servicios no pueden ser nulos.");
        this.simulacionService = Objects.requireNonNull(simulacionService, "Los servicios no pueden ser nulos.");

        // Configuración básica de la ventana.
        setTitle("Simulador de Fórmula 1");
        setSize(880, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera con el título. -----
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        cabecera.add(TemaF1.titulo("Simulador de Fórmula 1"));
        add(cabecera, BorderLayout.NORTH);

        // ----- Zona central: progreso arriba, ranking y eventos debajo. -----
        JPanel zonaCentral = new JPanel(new BorderLayout());

        barProgreso = new JProgressBar(0, 100);
        barProgreso.setStringPainted(true);
        barProgreso.setString("Configura tu carrera y presiona Iniciar");
        zonaCentral.add(barProgreso, BorderLayout.NORTH);

        JSplitPane panelCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                construirPanelRanking(), construirPanelEventos());
        panelCentral.setResizeWeight(0.6);
        panelCentral.setBorder(null);
        zonaCentral.add(panelCentral, BorderLayout.CENTER);

        add(zonaCentral, BorderLayout.CENTER);

        // ----- Zona sur: configuración y botones. -----
        JPanel configuracion = new JPanel();
        configuracion.setLayout(new BoxLayout(configuracion, BoxLayout.Y_AXIS));
        configuracion.setBorder(TemaF1.margenes(4, 12, 16, 16));
        configuracion.add(construirFilaSeleccion());
        configuracion.add(Box.createVerticalStrut(8));
        configuracion.add(construirFilaEstrategia());
        add(configuracion, BorderLayout.SOUTH);

        // ---- Acciones. ----
        conectarAcciones();

        // Mensaje de bienvenida con las instrucciones del nuevo flujo.
        anadirEvento("Bienvenido al simulador.", TemaF1.TEXTO);
        anadirEvento("1. Elige circuito, compuesto y vueltas.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("   El clima se obtiene automáticamente de la zona del circuito.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("2. Ejecuta la clasificación para definir la parrilla.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("3. Presiona Iniciar carrera: corren todos los autos en vivo, con paradas y abandonos.", TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

        // Consulta el clima real del circuito seleccionado por defecto.
        actualizarClimaDelCircuito();

    }

    /** Construye el panel superior del centro: tabla de clasificación en vivo. */
    private JPanel construirPanelRanking() {

        JPanel panel = new JPanel(new BorderLayout());

        modeloRanking = new DefaultTableModel(
                new String[]{"Pos", "Escudería", "Piloto", "Gap", "Estado"}, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaRanking = new JTable(modeloRanking);
        tablaRanking.setRowHeight(24);
        tablaRanking.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRanking.setFillsViewportHeight(true);
        tablaRanking.setDefaultRenderer(Object.class, new RendererRanking());
        tablaRanking.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaRanking.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaRanking.getColumnModel().getColumn(2).setPreferredWidth(130);
        tablaRanking.getColumnModel().getColumn(3).setPreferredWidth(70);
        tablaRanking.getColumnModel().getColumn(4).setPreferredWidth(80);

        panel.add(TemaF1.subtitulo("Clasificación en vivo"), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaRanking), BorderLayout.CENTER);

        return panel;
    }

    /** Construye el panel inferior del centro: registro de eventos de la carrera. */
    private JPanel construirPanelEventos() {

        JPanel panel = new JPanel(new BorderLayout());

        areaEventos = new JTextPane();
        areaEventos.setEditable(false);
        areaEventos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        TemaF1.conBorde(areaEventos);

        panel.add(TemaF1.subtitulo("Eventos de la carrera"), BorderLayout.NORTH);
        panel.add(new JScrollPane(areaEventos), BorderLayout.CENTER);

        return panel;
    }

    /** Construye la primera fila: selección de circuito e inicio de carrera. */
    private JPanel construirFilaSeleccion() {

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        comboCircuitos = new JComboBox<>();
        for (Circuito circuito : circuitoServicio.listarCircuitos()) {
            comboCircuitos.addItem(circuito);
        }

        // Al cambiar de circuito se consulta el clima real de su zona.
        comboCircuitos.addItemListener(e -> {

            if (e.getStateChange() == ItemEvent.SELECTED) {

                actualizarClimaDelCircuito();

            }
        });

        btnIniciar = new JButton("Iniciar carrera");
        TemaF1.estilizarBoton(btnIniciar);

        fila.add(TemaF1.etiqueta("Circuito:"));
        fila.add(comboCircuitos);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(btnIniciar);

        return fila;
    }

    /** Construye la segunda fila: condiciones de la carrera y clasificación. */
    private JPanel construirFilaEstrategia() {

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        etiquetaClima = TemaF1.etiqueta("--");

        comboCompuesto = new JComboBox<>(CompuestoNeumatico.values());
        comboCompuesto.setSelectedItem(CompuestoNeumatico.MEDIO);

        comboVueltas = new JComboBox<>(new Integer[]{3, 5, 10, 20, 40});
        comboVueltas.setSelectedItem(10);

        btnClasificacion = new JButton("Clasificación (parrilla)");
        TemaF1.estilizarBoton(btnClasificacion);

        fila.add(TemaF1.etiqueta("Clima:"));
        fila.add(etiquetaClima);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(TemaF1.etiqueta("Compuesto:"));
        fila.add(comboCompuesto);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(TemaF1.etiqueta("Vueltas:"));
        fila.add(comboVueltas);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(btnClasificacion);

        return fila;
    }

    /** Conecta las acciones de los botones de la ventana. */
    private void conectarAcciones() {

        // Clasificación: una vuelta lanzada por cada vehículo y parrilla ordenada.
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

                    // La pole se destaca en rojo; el resto en blanco.
                    anadirEvento(String.format("%2d. %-16s (%s)", i + 1, v.getPiloto().getNombre(), v.getMarcaEscuderia()),
                            i == 0 ? TemaF1.ROJO_F1 : TemaF1.TEXTO);

                }

                anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error en la clasificación: " + ex.getMessage(),
                        "Clasificación", JOptionPane.ERROR_MESSAGE);

            }
        });

        // Iniciar carrera: arma la parrilla y la corre en vivo con todos los autos.
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

        // Si hubo clasificación se usa esa parrilla; si no, se ordena por vuelta lanzada.
        List<Vehiculo> parrilla = (parrillaActual != null && !parrillaActual.isEmpty())
                ? new ArrayList<>(parrillaActual)
                : simulacionService.simularClasificacion(vehiculos, circuito, clima);

        // Todos los autos salen con el compuesto elegido en la configuración.
        CompuestoNeumatico compuesto = (CompuestoNeumatico) comboCompuesto.getSelectedItem();
        Map<Vehiculo, CompuestoNeumatico> compuestos = new HashMap<>();

        for (Vehiculo vehiculo : parrilla) {

            compuestos.put(vehiculo, compuesto);

        }

        int vueltas = (Integer) comboVueltas.getSelectedItem();

        carrera = new CarreraEnVivo(parrilla, circuito, clima, compuestos, vueltas);

        // Se reinicia la pantalla para la nueva carrera.
        modeloRanking.setRowCount(0);
        areaEventos.setText("");
        barProgreso.setValue(0);
        barProgreso.setString("Carrera en curso...");

        anadirEvento("=== CARRERA: " + circuito.getNombre() + " (" + circuito.getUbicacion() + ") ===", TemaF1.ROJO_F1);
        anadirEvento("Clima: " + clima + " | Compuesto: " + compuesto.getEtiqueta() + " | Vueltas: " + vueltas, TemaF1.TEXTO_SECUNDARIO);
        anadirEvento("", TemaF1.TEXTO_SECUNDARIO);

        deshabilitarControles(true);

        // La simulación corre en otro hilo para no congelar la interfaz.
        new Thread(() -> {

            // Solo se imprimen los eventos nuevos: se recuerda cuántos se vieron.
            int[] ultimoEvento = {0};

            try {

                while (!carrera.estaFinalizada()) {

                    carrera.avanzar(PASO_SIMULADO);

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

                // La carrera terminó: se muestra el resultado definitivo.
                ResultadoCarrera resultado = carrera.resultadoFinal();

                SwingUtilities.invokeLater(() -> {

                    actualizarRanking();
                    mostrarResultado(resultado);
                    deshabilitarControles(false);

                });

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> deshabilitarControles(false));

            }

        }).start();

    }

    /** Refresca la tabla de clasificación y la barra de progreso con el estado actual. */
    private void actualizarRanking() {

        modeloRanking.setRowCount(0);

        if (carrera == null) {

            return;

        }

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
    }

    /** Vuelca el resultado final de la carrera en los eventos y lo muestra en un diálogo. */
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

        // Clasificación final completa, todos los participantes.
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

    /** Devuelve el color temático de un evento según su tipo. */
    private Color colorDeEvento(String evento) {

        if (evento.startsWith("ABANDONO:")) {

            return COLOR_DNF;

        }

        if (evento.startsWith("Parada en boxes:") || evento.startsWith("Salida de boxes:")) {

            // "X en boxes: <escudería> (<piloto>)" -> se colorea de su escudería.
            String escuderia = evento.substring(evento.indexOf(": ") + 2, evento.indexOf(" ("));

            return TemaF1.colorDeEscuderia(escuderia);

        }

        if (evento.startsWith("Vuelta rápida:")) {

            return COLOR_VUELTA_RAPIDA;

        }

        return TemaF1.TEXTO;

    }

    /** Añade una línea de texto al área de eventos con el color indicado. */
    private void anadirEvento(String texto, Color color) {

        StyledDocument documento = areaEventos.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);
        StyleConstants.setFontFamily(atributos, Font.MONOSPACED);
        StyleConstants.setFontSize(atributos, 13);

        try {

            documento.insertString(documento.getLength(), texto + "\n", atributos);

        } catch (BadLocationException ex) {

            // No debería ocurrir: siempre se inserta al final del documento.
            ex.printStackTrace();

        }
    }

    /** Activa o desactiva los controles de configuración durante la carrera. */
    private void deshabilitarControles(boolean deshabilitado) {

        btnIniciar.setEnabled(!deshabilitado);
        btnClasificacion.setEnabled(!deshabilitado);
        comboCircuitos.setEnabled(!deshabilitado);
        comboCompuesto.setEnabled(!deshabilitado);
        comboVueltas.setEnabled(!deshabilitado);

    }

    /**
     * Consulta el clima real (vía API) de la zona del circuito seleccionado y
     * lo muestra en la etiqueta de clima. La consulta corre en un hilo aparte
     * para no congelar la interfaz, y se descartan respuestas obsoletas si el
     * usuario cambió de circuito mientras la API respondía.
     */
    private void actualizarClimaDelCircuito() {

        Circuito circuito = (Circuito) comboCircuitos.getSelectedItem();

        if (circuito == null) {

            etiquetaClima.setText("--");
            return;

        }

        // Se marca esta consulta como la vigente.
        int generacion = ++consultaClimaId;
        etiquetaClima.setText("Consultando...");

        new Thread(() -> {

            String clima = simulacionService.resolverClima(circuito, ConfiguracionCarrera.CLIMA_AUTO);

            SwingUtilities.invokeLater(() -> {

                // Si mientras tanto se eligió otro circuito o llegó una consulta más
                // nueva, esta respuesta ya no interesa.
                if (generacion == consultaClimaId && circuito == comboCircuitos.getSelectedItem()) {

                    etiquetaClima.setText(clima);

                }
            });

        }).start();
    }

    /**
     * Renderer de la tabla de clasificación con temática Fórmula 1:
     * el líder se resalta en rojo, los abandonos en rojo apagado y las
     * escuderías se pintan con su color oficial.
     */
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

            // Posición y alineación según la columna.
            celda.setHorizontalAlignment(columna == 0 || columna == 3
                    ? javax.swing.SwingConstants.CENTER
                    : javax.swing.SwingConstants.LEFT);

            if (posicion == 1) {

                // Líder de la carrera: fila completa en rojo F1.
                celda.setBackground(TemaF1.ROJO_F1);
                celda.setForeground(Color.WHITE);

            } else if ("DNF".equals(estado)) {

                // Abandonos: fondo rojo apagado y texto claro.
                celda.setBackground(new Color(0x2A1A1A));
                celda.setForeground(COLOR_DNF);

            }

            // La columna de escudería se pinta con su color oficial.
            if (columna == 1 && posicion != 1) {

                celda.setForeground(TemaF1.colorDeEscuderia(String.valueOf(valor)));

            }

            return celda;

        }
    }

}
