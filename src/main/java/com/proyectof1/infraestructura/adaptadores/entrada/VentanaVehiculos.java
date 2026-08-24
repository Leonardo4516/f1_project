package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
    private DefaultComboBoxModel<String> modeloPilotos;

    private JTextField txtMarcaEscuderia;
    private JTextField txtVelocidadMaxima;
    private JTextField txtAceleracion;
    private JTextField txtFrenado;
    private JTextField txtAgarre;
    private JComboBox<String> comboPilotos;
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
        setSize(780, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new MigLayout("insets 8 16 8 16", "[grow][]", "[]"));
        cabecera.setBackground(TemaF1.FONDO);
        cabecera.add(TemaF1.titulo("Vehículos"), "growx");
        btnEliminar = new JButton(TemaF1.icono("delete"));
        btnEliminar.setText(" Eliminar");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, "w 140!, right");
        add(cabecera, BorderLayout.NORTH);

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
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel cuerpoSur = new JPanel();
        cuerpoSur.setLayout(new BoxLayout(cuerpoSur, BoxLayout.Y_AXIS));
        cuerpoSur.setBorder(TemaF1.margenes(8, 12, 16, 16));
        cuerpoSur.add(construirPanelBusqueda());
        cuerpoSur.add(construirPanelFormulario());
        add(cuerpoSur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> {
            try {
                String marcaEscuderia = txtMarcaEscuderia.getText();
                int velocidadMaxima = Integer.parseInt(txtVelocidadMaxima.getText());
                int aceleracion = Integer.parseInt(txtAceleracion.getText());
                int frenado = Integer.parseInt(txtFrenado.getText());
                int agarre = Integer.parseInt(txtAgarre.getText());
                String nombrePiloto = (String) comboPilotos.getSelectedItem();

                if (nombrePiloto == null) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Registra pilotos antes de crear vehículos.");
                    return;
                }

                Piloto piloto = pilotoServicio.buscarPorNombre(nombrePiloto);
                vehiculoServicio.registrar(marcaEscuderia, velocidadMaxima, aceleracion, frenado, agarre, piloto);
                actualizarTabla();
                limpiarCampos();
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registro de vehículo", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un vehículo de la tabla.");
                return;
            }
            String escuderia = (String) modelo.getValueAt(fila, 0);
            if (vehiculoServicio.eliminar(escuderia)) {
                actualizarTabla();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el vehículo.");
            }
        });

        btnBuscar.addActionListener(e -> {
            Vehiculo encontrado = vehiculoServicio.buscarPorEscuderia(txtBuscar.getText());
            if (encontrado != null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Vehículo: " + encontrado.getMarcaEscuderia()
                                + " | Vel: " + encontrado.getVelocidadMaxima()
                                + " | Acel: " + encontrado.getAceleracion()
                                + " | Fren: " + encontrado.getFrenado()
                                + " | Agarre: " + encontrado.getAgarre()
                                + " | Piloto: " + encontrado.getPiloto().getNombre());
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Vehículo no encontrado.");
            }
        });

        actualizarTabla();
        actualizarComboPilotos();
    }

    private JPanel construirPanelBusqueda() {
        JPanel panel = new JPanel(new MigLayout("insets 4 0 4 0", "[][grow][]", "[]"));
        panel.setBackground(TemaF1.FONDO);

        txtBuscar = new JTextField(18);
        btnBuscar = new JButton(TemaF1.icono("search"));
        btnBuscar.setText(" Buscar");

        panel.add(TemaF1.etiqueta("Escudería:"));
        panel.add(txtBuscar, "growx");
        panel.add(btnBuscar, "w 120!");

        return panel;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 12 8 8 8, gap 10",
                "[right]rel[200!,grow][right]rel[200!,grow]",
                "[]10[]10[]10[]"));
        panel.setBackground(TemaF1.FONDO);

        txtMarcaEscuderia = new JTextField(20);
        txtVelocidadMaxima = new JTextField(10);
        txtAceleracion = new JTextField(10);
        txtFrenado = new JTextField(10);
        txtAgarre = new JTextField(10);
        modeloPilotos = new DefaultComboBoxModel<>();
        comboPilotos = new JComboBox<>(modeloPilotos);
        btnRegistrar = new JButton(TemaF1.icono("add"));
        btnRegistrar.setText(" Registrar");

        panel.add(TemaF1.etiqueta("Escudería:"));
        panel.add(txtMarcaEscuderia);
        panel.add(TemaF1.etiqueta("Velocidad máxima:"));
        panel.add(txtVelocidadMaxima);

        panel.add(TemaF1.etiqueta("Aceleración (1-100):"));
        panel.add(txtAceleracion);
        panel.add(TemaF1.etiqueta("Frenado (1-100):"));
        panel.add(txtFrenado);

        panel.add(TemaF1.etiqueta("Agarre (1-100):"));
        panel.add(txtAgarre);
        panel.add(TemaF1.etiqueta("Piloto:"));
        panel.add(comboPilotos);

        panel.add(btnRegistrar, "span 2, w 160!");

        return panel;
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

    private void actualizarComboPilotos() {
        modeloPilotos.removeAllElements();
        for (Piloto piloto : pilotoServicio.listarPilotos()) {
            modeloPilotos.addElement(piloto.getNombre());
        }
    }

    private void limpiarCampos() {
        txtMarcaEscuderia.setText("");
        txtVelocidadMaxima.setText("");
        txtAceleracion.setText("");
        txtFrenado.setText("");
        txtAgarre.setText("");
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
