package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.dominio.Piloto;

public class VentanaPilotos extends JFrame {

    private final PilotoServicio pilotoServicio;
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtNombre;
    private JTextField txtExperiencia;
    private JTextField txtHabilidadLluvia;
    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public VentanaPilotos(PilotoServicio pilotoServicio) {
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio,
                "El servicio de pilotos no puede ser nulo.");

        setTitle("Administración de Pilotos");
        setSize(620, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16", "[grow][]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Pilotos"), "growx");
        btnEliminar = new JButton(TemaF1.icono("delete"));
        btnEliminar.setText(" Eliminar");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, "w 140!, right");
        add(cabecera, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"Nombre", "Experiencia (1-100)", "Habilidad lluvia (1-100)"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel cuerpoSur = new JPanel();
        cuerpoSur.setLayout(new BoxLayout(cuerpoSur, BoxLayout.Y_AXIS));
        cuerpoSur.setBorder(TemaF1.margenes(8, 12, 16, 16));
        cuerpoSur.add(construirPanelBusqueda());
        cuerpoSur.add(construirPanelFormulario());
        add(cuerpoSur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                int experiencia = Integer.parseInt(txtExperiencia.getText());
                int habilidadLluvia = Integer.parseInt(txtHabilidadLluvia.getText());
                pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);
                actualizarTabla();
                limpiarCampos();
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registro de piloto", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un piloto de la tabla.");
                return;
            }
            String nombre = (String) modelo.getValueAt(fila, 0);
            if (pilotoServicio.eliminar(nombre)) {
                actualizarTabla();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el piloto.");
            }
        });

        btnBuscar.addActionListener(e -> {
            Piloto encontrado = pilotoServicio.buscarPorNombre(txtBuscar.getText());
            if (encontrado != null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Piloto: " + encontrado.getNombre()
                                + " | Exp: " + encontrado.getExperiencia()
                                + " | Lluvia: " + encontrado.getHabilidadLluvia());
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Piloto no encontrado.");
            }
        });

        actualizarTabla();
    }

    private JPanel construirPanelBusqueda() {
        JPanel panel = new JPanel(new MigLayout("insets 4 0 4 0", "[][grow][]", "[]"));
        panel.setBackground(TemaF1.FONDO);

        txtBuscar = new JTextField(18);
        btnBuscar = new JButton(TemaF1.icono("search"));
        btnBuscar.setText(" Buscar");

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtBuscar, "growx");
        panel.add(btnBuscar, "w 120!");

        return panel;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 12 8 8 8, gap 10",
                "[right]rel[200!,grow][right]rel[200!,grow]",
                "[]10[]"));
        panel.setBackground(TemaF1.FONDO);

        txtNombre = new JTextField(20);
        txtExperiencia = new JTextField(10);
        txtHabilidadLluvia = new JTextField(10);
        btnRegistrar = new JButton(TemaF1.icono("add"));
        btnRegistrar.setText(" Registrar");

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre);
        panel.add(TemaF1.etiqueta("Experiencia (1-100):"));
        panel.add(txtExperiencia);

        panel.add(TemaF1.etiqueta("Habilidad lluvia (1-100):"));
        panel.add(txtHabilidadLluvia, "span 2, growx");
        panel.add(btnRegistrar, "w 160!");

        return panel;
    }

    private void actualizarTabla() {
        modelo.setRowCount(0);
        for (Piloto piloto : pilotoServicio.listarPilotos()) {
            modelo.addRow(new Object[]{piloto.getNombre(), piloto.getExperiencia(), piloto.getHabilidadLluvia()});
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtExperiencia.setText("");
        txtHabilidadLluvia.setText("");
    }
}
