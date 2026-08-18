package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * Utilidad visual (infraestructura) con el tema gráfico estilo Fórmula 1:
 * paleta oscura, rojo distintivo de la F1 y colores oficiales de las
 * escuderías. Centraliza colores y estilos para todas las ventanas.
 */
public final class TemaF1 {

    public static final Color ROJO_F1 = new Color(0xE10600);
    public static final Color FONDO = new Color(0x141414);
    public static final Color PANEL = new Color(0x1E1E1E);
    public static final Color TEXTO = new Color(0xF5F5F5);
    public static final Color TEXTO_SECUNDARIO = new Color(0x9A9A9A);
    public static final Color BORDE = new Color(0x333333);

    // Colores oficiales de las escuderías (hexadecimal real de cada equipo).
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
        // Clase estática, no se instancia.
    }

    /**
     * Instala el tema oscuro de FlatLaf y adapta los colores de los
     * componentes estándar al estilo de la aplicación.
     */
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

    }

    /**
     * Devuelve el color oficial de una escudería. Si la escudería no está
     * en el catálogo, genera un color estable a partir de su nombre para
     * que cada equipo siempre se vea identificable.
     */
    public static Color colorDeEscuderia(String escuderia) {

        Color oficial = COLORES_EQUIPOS.get(escuderia);

        if (oficial != null) {

            return oficial;

        }

        // Color determinista: mismo nombre -> mismo color en cada ejecución.
        float matiz = Math.abs(escuderia.hashCode() % 360) / 360f;

        return Color.getHSBColor(matiz, 0.65f, 0.85f);

    }

    /** Devuelve el color oficial de la escudería de un vehículo si se conoce. */
    public static Color colorDeEscuderia(com.proyectof1.dominio.Vehiculo vehiculo) {

        return colorDeEscuderia(vehiculo.getMarcaEscuderia());

    }

    /** Estiliza un botón: texto en negrita, sin foco dibujado y con relleno. */
    public static void estilizarBoton(JButton boton) {

        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 14f));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(8, 18, 8, 18));

    }

    /** Crea una etiqueta de título con el color rojo de la F1. */
    public static JLabel titulo(String texto) {

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 22f));
        etiqueta.setForeground(ROJO_F1);

        return etiqueta;

    }

    /** Crea una etiqueta de subtítulo con el color secundario. */
    public static JLabel subtitulo(String texto) {

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(12f));
        etiqueta.setForeground(TEXTO_SECUNDARIO);

        return etiqueta;

    }

    /** Crea una etiqueta estándar de formulario con el color secundario. */
    public static JLabel etiqueta(String texto) {

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO_SECUNDARIO);

        return etiqueta;

    }

    /** Devuelve un borde con relleno generoso (márgenes estéticos). */
    public static javax.swing.border.Border margenes(int arriba, int abajo, int izq, int der) {

        return new EmptyBorder(arriba, izq, abajo, der);

    }

    /** Devuelve un borde con relleno uniforme. */
    public static javax.swing.border.Border margenes(int m) {

        return margenes(m, m, m, m);

    }

    /** Aplica un borde delgado con el color de separación del tema. */
    public static void conBorde(javax.swing.JComponent componente) {

        componente.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1),
                margenes(12)));

    }

}