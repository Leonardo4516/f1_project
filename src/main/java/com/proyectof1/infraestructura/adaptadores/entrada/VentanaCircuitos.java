package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.dominio.Circuito;

public class VentanaCircuitos extends JFrame {

    private final CircuitoServicio circuitoServicio;

    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public VentanaCircuitos(CircuitoServicio circuitoServicio) {
        this.circuitoServicio = Objects.requireNonNull(circuitoServicio,
                "El servicio de circuitos no puede ser nulo.");

        setTitle("Administración de Circuitos");
        setSize(750, 500);
        setMinimumSize(new java.awt.Dimension(500, 350));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16, gap 10", "[grow][]20[]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Circuitos"), "growx");

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

        modelo = new DefaultTableModel(new String[]{
                "Nombre", "Km", "Ubicación", "Curvas", "Tipo", "Vueltas", "Récord"
        }, 0) {
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
                JOptionPane.showMessageDialog(this, "Selecciona un circuito de la tabla.");
                return;
            }
            String nombre = (String) modelo.getValueAt(fila, 0);
            if (circuitoServicio.eliminar(nombre)) {
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el circuito.");
            }
        });

        btnBuscar.addActionListener(e -> {
            Circuito encontrado = circuitoServicio.buscarPorNombre(txtBuscar.getText());
            if (encontrado != null) {
                JOptionPane.showMessageDialog(this,
                        "Circuito: " + encontrado.getNombre()
                                + " | " + encontrado.getKilometros() + " km"
                                + " | " + encontrado.getUbicacion()
                                + "\nCurvas: " + encontrado.getNumCurvas()
                                + " | Tipo: " + encontrado.getTipoCircuito()
                                + " | Vueltas: " + encontrado.getVueltasTipicas()
                                + "\nRécord: " + encontrado.getRecordVuelta());
            } else {
                JOptionPane.showMessageDialog(this, "Circuito no encontrado.");
            }
        });

        actualizarTabla();
    }

    private void mostrarDialogoRegistro() {
        JDialog dialogo = new JDialog(this, "Registrar Circuito", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField txtNombre = new JTextField(15);
        JTextField txtKilometros = new JTextField(5);
        JTextField txtUbicacion = new JTextField(15);
        JTextField txtNumCurvas = new JTextField(5);
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Permanente", "Urbano", "Semiacotico"});
        JTextField txtVueltas = new JTextField(5);
        JTextField txtRecord = new JTextField(15);

        JButton btnAceptar = new JButton(TemaF1.icono("add"));
        btnAceptar.setText(" Registrar");
        TemaF1.estilizarBoton(btnAceptar);

        JButton btnCancelar = new JButton("Cancelar");
        TemaF1.estilizarBoton(btnCancelar);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(TemaF1.FONDO);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0; panel.add(TemaF1.etiqueta("Nombre:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(txtNombre, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Kilómetros:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(txtKilometros, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; panel.add(TemaF1.etiqueta("Ubicación:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(txtUbicacion, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Nº Curvas:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(txtNumCurvas, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Tipo:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(comboTipo, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Vueltas típicas:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(txtVueltas, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0; panel.add(TemaF1.etiqueta("Récord:"), c);
        c.gridx = 1; c.gridy = 3; c.weightx = 1; c.gridwidth = 3;
        panel.add(txtRecord, c);
        c.gridwidth = 1;

        c.gridx = 0; c.gridy = 4; c.weightx = 1; panel.add(btnAceptar, c);
        c.gridx = 3; c.weightx = 0; panel.add(btnCancelar, c);

        dialogo.setContentPane(panel);
        dialogo.setSize(560, 300);
        dialogo.setLocationRelativeTo(this);

        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                double kilometros = Double.parseDouble(txtKilometros.getText());
                String ubicacion = txtUbicacion.getText();
                int numCurvas = Integer.parseInt(txtNumCurvas.getText());
                String tipo = (String) comboTipo.getSelectedItem();
                int vueltas = Integer.parseInt(txtVueltas.getText());
                String record = txtRecord.getText();

                circuitoServicio.registrar(nombre, kilometros, ubicacion, numCurvas, tipo, vueltas, record);
                actualizarTabla();
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error: " + ex.getMessage(),
                        "Registro de circuito", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.setVisible(true);
    }

    private void actualizarTabla() {
        modelo.setRowCount(0);
        for (Circuito circuito : circuitoServicio.listarCircuitos()) {
            modelo.addRow(new Object[]{
                    circuito.getNombre(),
                    circuito.getKilometros(),
                    circuito.getUbicacion(),
                    circuito.getNumCurvas(),
                    circuito.getTipoCircuito(),
                    circuito.getVueltasTipicas(),
                    circuito.getRecordVuelta()
            });
        }
    }
}
