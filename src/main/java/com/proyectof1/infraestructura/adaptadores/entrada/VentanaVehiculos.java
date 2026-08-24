package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public class VentanaVehiculos extends JFrame {

    private final VehiculoServicio vehiculoServicio;
    private final PilotoServicio pilotoServicio;

    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public VentanaVehiculos(VehiculoServicio vehiculoServicio, PilotoServicio pilotoServicio) {
        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio,
                "Los servicios de vehículo y piloto no pueden ser nulos.");
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio,
                "Los servicios de vehículo y piloto no pueden ser nulos.");

        setTitle("Administración de Vehículos");
        setSize(700, 500);
        setMinimumSize(new java.awt.Dimension(480, 320));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16, gap 10", "[grow][]20[]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Vehículos"), "growx");

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
        barraBusqueda.add(TemaF1.etiqueta("Escudería:"));
        barraBusqueda.add(txtBuscar, "growx, wmin 100");
        barraBusqueda.add(btnBuscar, "w 120!");

        JPanel sur = new JPanel(new MigLayout("insets 0, fill, flowy", "[grow]", "[][grow]"));
        sur.setBackground(TemaF1.FONDO);
        sur.setBorder(TemaF1.margenes(0, 0, 8, 0));
        sur.add(barraBusqueda, "growx");

        modelo = new DefaultTableModel(new String[]{"Escudería", "Vel. máx", "Acel", "Fren", "Agar", "Piloto"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        tabla.getColumnModel().getColumn(0).setCellRenderer(new RendererEscuderia());
        sur.add(new JScrollPane(tabla), "grow");
        add(sur, BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> mostrarDialogoRegistro());

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Selecciona un vehículo de la tabla.");
                return;
            }
            String escuderia = (String) modelo.getValueAt(fila, 0);
            if (vehiculoServicio.eliminar(escuderia)) {
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el vehículo.");
            }
        });

        btnBuscar.addActionListener(e -> {
            Vehiculo encontrado = vehiculoServicio.buscarPorEscuderia(txtBuscar.getText());
            if (encontrado != null) {
                JOptionPane.showMessageDialog(this,
                        "Vehículo: " + encontrado.getMarcaEscuderia()
                                + " | Vel: " + encontrado.getVelocidadMaxima()
                                + " | Acel: " + encontrado.getAceleracion()
                                + " | Fren: " + encontrado.getFrenado()
                                + " | Agarre: " + encontrado.getAgarre()
                                + " | Piloto: " + encontrado.getPiloto().getNombre());
            } else {
                JOptionPane.showMessageDialog(this, "Vehículo no encontrado.");
            }
        });

        actualizarTabla();
    }

    private void mostrarDialogoRegistro() {
        JDialog dialogo = new JDialog(this, "Registrar Vehículo", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField txtMarcaEscuderia = new JTextField(15);
        JTextField txtVelocidadMaxima = new JTextField(5);
        JTextField txtAceleracion = new JTextField(5);
        JTextField txtFrenado = new JTextField(5);
        JTextField txtAgarre = new JTextField(5);

        DefaultComboBoxModel<String> modeloPilotos = new DefaultComboBoxModel<>();
        for (Piloto piloto : pilotoServicio.listarPilotos()) {
            modeloPilotos.addElement(piloto.getNombre());
        }
        JComboBox<String> comboPilotos = new JComboBox<>(modeloPilotos);

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

        c.gridx = 0; c.gridy = 0; c.weightx = 0; panel.add(TemaF1.etiqueta("Escudería:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(txtMarcaEscuderia, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Velocidad máx:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(txtVelocidadMaxima, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; panel.add(TemaF1.etiqueta("Aceleración:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(txtAceleracion, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Frenado:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(txtFrenado, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Agarre:"), c);
        c.gridx = 1; c.weightx = 1; panel.add(txtAgarre, c);
        c.gridx = 2; c.weightx = 0; panel.add(TemaF1.etiqueta("Piloto:"), c);
        c.gridx = 3; c.weightx = 1; panel.add(comboPilotos, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 1; panel.add(btnAceptar, c);
        c.gridx = 3; c.weightx = 0; panel.add(btnCancelar, c);

        dialogo.setContentPane(panel);
        dialogo.setSize(560, 250);
        dialogo.setLocationRelativeTo(this);

        btnCancelar.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            try {
                String marcaEscuderia = txtMarcaEscuderia.getText();
                int velocidadMaxima = Integer.parseInt(txtVelocidadMaxima.getText());
                int aceleracion = Integer.parseInt(txtAceleracion.getText());
                int frenado = Integer.parseInt(txtFrenado.getText());
                int agarre = Integer.parseInt(txtAgarre.getText());
                String nombrePiloto = (String) comboPilotos.getSelectedItem();

                if (nombrePiloto == null) {
                    JOptionPane.showMessageDialog(dialogo, "Registra pilotos antes de crear vehículos.");
                    return;
                }

                Piloto piloto = pilotoServicio.buscarPorNombre(nombrePiloto);
                vehiculoServicio.registrar(marcaEscuderia, velocidadMaxima, aceleracion, frenado, agarre, piloto);
                actualizarTabla();
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error: " + ex.getMessage(),
                        "Registro de vehículo", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.setVisible(true);
    }

    private void actualizarTabla() {
        modelo.setRowCount(0);
        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {
            modelo.addRow(new Object[]{
                    vehiculo.getMarcaEscuderia(),
                    vehiculo.getVelocidadMaxima(),
                    vehiculo.getAceleracion(),
                    vehiculo.getFrenado(),
                    vehiculo.getAgarre(),
                    vehiculo.getPiloto().getNombre()});
        }
    }

    private static class RendererEscuderia extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {

            super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila, columna);
            setHorizontalAlignment(CENTER);

            if (!seleccionado) {
                setBackground(TemaF1.colorDeEscuderia((String) valor));
                setForeground(java.awt.Color.BLACK);
                setFont(getFont().deriveFont(Font.BOLD));
            }

            return this;
        }
    }
}
