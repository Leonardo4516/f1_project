package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public VentanaPilotos(PilotoServicio pilotoServicio) {
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio,
                "El servicio de pilotos no puede ser nulo.");

        setTitle("Administración de Pilotos");
        setSize(620, 480);
        setMinimumSize(new java.awt.Dimension(450, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16, gap 10", "[grow][]20[]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Pilotos"), "growx");

        btnRegistrar = new JButton(TemaF1.icono("add"));
        btnRegistrar.setText(" Registrar");
        TemaF1.estilizarBoton(btnRegistrar);
        cabecera.add(btnRegistrar, "w 140!");

        btnEliminar = new JButton(TemaF1.icono("delete"));
        btnEliminar.setText(" Eliminar");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, "w 140!");
        add(cabecera, BorderLayout.NORTH);

        JPanel barraBusqueda = new JPanel(new MigLayout("insets 4 16 4 16, gap 8", "[][grow][]", "[]"));
        barraBusqueda.setBackground(TemaF1.FONDO);
        txtBuscar = new JTextField();
        btnBuscar = new JButton(TemaF1.icono("search"));
        btnBuscar.setText(" Buscar");
        barraBusqueda.add(TemaF1.etiqueta("Nombre:"));
        barraBusqueda.add(txtBuscar, "growx, wmin 100");
        barraBusqueda.add(btnBuscar, "w 120!");

        JPanel sur = new JPanel(new MigLayout("insets 0, fill, flowy", "[grow]", "[][grow]"));
        sur.setBackground(TemaF1.FONDO);
        sur.setBorder(TemaF1.margenes(0, 0, 8, 0));
        sur.add(barraBusqueda, "growx");

        modelo = new DefaultTableModel(new String[]{"Nombre", "Experiencia (1-100)", "Habilidad lluvia (1-100)"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        sur.add(new JScrollPane(tabla), "grow");
        add(sur, BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> mostrarDialogoRegistro());

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un piloto de la tabla.");
                return;
            }
            String nombre = (String) modelo.getValueAt(fila, 0);
            if (pilotoServicio.eliminar(nombre)) {
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el piloto.");
            }
        });

        btnBuscar.addActionListener(e -> {
            Piloto encontrado = pilotoServicio.buscarPorNombre(txtBuscar.getText());
            if (encontrado != null) {
                JOptionPane.showMessageDialog(this,
                        "Piloto: " + encontrado.getNombre()
                                + " | Exp: " + encontrado.getExperiencia()
                                + " | Lluvia: " + encontrado.getHabilidadLluvia());
            } else {
                JOptionPane.showMessageDialog(this, "Piloto no encontrado.");
            }
        });

        actualizarTabla();
    }

    private void mostrarDialogoRegistro() {
        JDialog dialogo = new JDialog(this, "Registrar Piloto", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField txtNombre = new JTextField();
        JTextField txtExperiencia = new JTextField();
        JTextField txtHabilidadLluvia = new JTextField();

        JButton btnAceptar = new JButton(TemaF1.icono("add"));
        btnAceptar.setText(" Registrar");
        TemaF1.estilizarBoton(btnAceptar);

        JButton btnCancelar = new JButton("Cancelar");
        TemaF1.estilizarBoton(btnCancelar);

        JPanel panel = new JPanel(new MigLayout(
                "insets 16, gap 10, fill, flowy",
                "[right]rel[grow,fill]",
                "[][][]20[]"));
        panel.setBackground(TemaF1.FONDO);

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre, "growx");

        panel.add(TemaF1.etiqueta("Experiencia (1-100):"));
        panel.add(txtExperiencia, "growx");

        panel.add(TemaF1.etiqueta("Habilidad lluvia (1-100):"));
        panel.add(txtHabilidadLluvia, "growx");

        panel.add(btnAceptar, "w 140!");
        panel.add(btnCancelar, "w 120!");

        dialogo.setContentPane(panel);
        dialogo.setSize(400, 280);
        dialogo.setLocationRelativeTo(this);

        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                int experiencia = Integer.parseInt(txtExperiencia.getText());
                int habilidadLluvia = Integer.parseInt(txtHabilidadLluvia.getText());
                pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);
                actualizarTabla();
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error: " + ex.getMessage(),
                        "Registro de piloto", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.setVisible(true);
    }

    private void actualizarTabla() {
        modelo.setRowCount(0);
        for (Piloto piloto : pilotoServicio.listarPilotos()) {
            modelo.addRow(new Object[]{piloto.getNombre(), piloto.getExperiencia(), piloto.getHabilidadLluvia()});
        }
    }
}
