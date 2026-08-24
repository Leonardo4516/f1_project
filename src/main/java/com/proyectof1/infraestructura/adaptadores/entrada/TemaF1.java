package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

// Replaced MigLayout usage to avoid dependency on net.miginfocom.layout.LC (missing class at compile time)

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Utilidad visual (infraestructura) con el tema gráfico estilo Fórmula 1.
 * Paleta oscura, rojo distintivo y colores oficiales de escuderías.
 * Centraliza colores, estilos, iconos SVG y utilidades de gráficas.
 */
public final class TemaF1 {

    public static final Color ROJO_F1 = new Color(0xE10600);
    public static final Color FONDO = new Color(0x141414);
    public static final Color PANEL = new Color(0x1E1E1E);
    public static final Color TEXTO = new Color(0xF5F5F5);
    public static final Color TEXTO_SECUNDARIO = new Color(0x9A9A9A);
    public static final Color BORDE = new Color(0x333333);
    public static final Color ASFALTO = new Color(0x2A2A2A);

    private static final Map<String, Color> COLORES_EQUIPOS = new HashMap<>();

    static {
        COLORES_EQUIPOS.put("Red Bull", new Color(0x3671C6));
        COLORES_EQUIPOS.put("Ferrari", new Color(0xE8002D));
        COLORES_EQUIPOS.put("McLaren", new Color(0xFF8000));
        COLORES_EQUIPOS.put("Mercedes", new Color(0x27F4D2));
        COLORES_EQUIPOS.put("Aston Martin", new Color(0x229971));
        COLORES_EQUIPOS.put("Williams", new Color(0x64C4FF));
        COLORES_EQUIPOS.put("Alpine", new Color(0xFF87BC));
        COLORES_EQUIPOS.put("Haas", new Color(0xB6BABD));
        COLORES_EQUIPOS.put("Racing Bulls", new Color(0x6692FF));
        COLORES_EQUIPOS.put("Sauber", new Color(0x52E252));
        COLORES_EQUIPOS.put("Alpha Tauri", new Color(0x6495ED));
        COLORES_EQUIPOS.put("Renault", new Color(0x0093CC));
        COLORES_EQUIPOS.put("Lotus", new Color(0xD4AF37));
        COLORES_EQUIPOS.put("Toyota", new Color(0xFF0000));
        COLORES_EQUIPOS.put("Force India", new Color(0xFF5F0F));
    }

    private TemaF1() {
    }

    public static void aplicarTema() {
        FlatDarkLaf.setup();

        UIManager.put("Panel.background", FONDO);
        UIManager.put("Panel.foreground", TEXTO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("TableHeader.background", FONDO);
        UIManager.put("TableHeader.foreground", TEXTO);
        UIManager.put("Table.selectionBackground", ROJO_F1);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("OptionPane.background", FONDO);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("List.background", PANEL);
        UIManager.put("List.foreground", TEXTO);
        UIManager.put("TextField.background", ASFALTO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", TEXTO);
        UIManager.put("ComboBox.background", ASFALTO);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("Button.background", ROJO_F1);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.hoverBackground", new Color(0xC00500));
        UIManager.put("Button.pressedBackground", new Color(0xA00400));
    }

    public static Color colorDeEscuderia(String escuderia) {
        Color oficial = COLORES_EQUIPOS.get(escuderia);
        if (oficial != null) {
            return oficial;
        }
        float matiz = Math.abs(escuderia.hashCode() % 360) / 360f;
        return Color.getHSBColor(matiz, 0.65f, 0.85f);
    }

    public static Color colorDeEscuderia(com.proyectof1.dominio.Vehiculo vehiculo) {
        return colorDeEscuderia(vehiculo.getMarcaEscuderia());
    }

    public static void estilizarBoton(JButton boton) {
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 13f));
        boton.setFocusPainted(false);
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 22f));
        etiqueta.setForeground(ROJO_F1);
        return etiqueta;
    }

    public static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(12f));
        etiqueta.setForeground(TEXTO_SECUNDARIO);
        return etiqueta;
    }

    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO_SECUNDARIO);
        return etiqueta;
    }

    public static javax.swing.border.Border margenes(int arriba, int abajo, int izq, int der) {
        return new EmptyBorder(arriba, izq, abajo, der);
    }

    public static javax.swing.border.Border margenes(int m) {
        return margenes(m, m, m, m);
    }

    public static void conBorde(javax.swing.JComponent componente) {
        componente.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1),
                margenes(12)));
    }

    /** Crea un icono SVG desde la carpeta de recursos icons/. */
    public static FlatSVGIcon icono(String nombre) {
        return new FlatSVGIcon("icons/" + nombre + ".svg", 1.0f);
    }

    /** Crea un icono SVG pequeño (para botones de tabla, etc.). */
    public static FlatSVGIcon iconoPequeno(String nombre) {
        return new FlatSVGIcon("icons/" + nombre + ".svg", 1.0f);
    }

    /** Crea una gráfica XY de velocidad vs tiempo con estilo oscuro F1. */
    public static JFreeChart crearGraficaVelocidad(String tituloGrafica) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                tituloGrafica,
                "Tiempo (s)",
                "Velocidad (km/h)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        chart.setBackgroundPaint(FONDO);
        chart.getTitle().setPaint(TEXTO);
        chart.getTitle().setFont(chart.getTitle().getFont().deriveFont(Font.BOLD, 14f));

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(PANEL);
        plot.setDomainGridlinePaint(BORDE);
        plot.setRangeGridlinePaint(BORDE);
        plot.getDomainAxis().setTickLabelPaint(TEXTO_SECUNDARIO);
        plot.getDomainAxis().setLabelPaint(TEXTO);
        plot.getRangeAxis().setTickLabelPaint(TEXTO_SECUNDARIO);
        plot.getRangeAxis().setLabelPaint(TEXTO);
        plot.setOutlinePaint(BORDE);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ROJO_F1);
        renderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));

        chart.getLegend().setItemPaint(TEXTO);
        chart.getLegend().setBackgroundPaint(FONDO);

        return chart;
    }

    /** Crea un panel con GridBagLayout (alternativa a MigLayout) con estilo oscuro para formularios. */
    public static JPanel panelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO);
        // Default constraints for form rows: label on left, field(s) on right
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0; // label
        // Note: caller should provide components and use these constraints as a template.
        return panel;
    }
}
