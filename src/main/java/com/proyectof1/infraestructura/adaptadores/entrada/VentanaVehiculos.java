package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
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

        JTextField txtMarcaEscuderia = new JTextField();
        JTextField txtVelocidadMaxima = new JTextField();
        JTextField txtAceleracion = new JTextField();
        JTextField txtFrenado = new JTextField();
        JTextField txtAgarre = new JTextField();

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

        JPanel panel = new JPanel(new MigLayout(
                "insets 16, gap 10, fill, flowy",
                "[right]rel[grow,fill]",
                "[][][][][][]20[]"));
        panel.setBackground(TemaF1.FONDO);

        panel.add(TemaF1.etiqueta("Escudería:"));
        panel.add(txtMarcaEscuderia, "growx");

        panel.add(TemaF1.etiqueta("Velocidad máxima:"));
        panel.add(txtVelocidadMaxima, "growx");

        panel.add(TemaF1.etiqueta("Aceleración (1-100):"));
        panel.add(txtAceleracion, "growx");

        panel.add(TemaF1.etiqueta("Frenado (1-100):"));
        panel.add(txtFrenado, "growx");

        panel.add(TemaF1.etiqueta("Agarre (1-100):"));
        panel.add(txtAgarre, "growx");

        panel.add(TemaF1.etiqueta("Piloto:"));
        panel.add(comboPilotos, "growx");

        panel.add(btnAceptar, "w 140!");
        panel.add(btnCancelar, "w 120!");

        dialogo.setContentPane(panel);
        dialogo.setSize(450, 490);
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
