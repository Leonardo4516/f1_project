package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.dominio.Circuito;

/**
 * Ventana de gestión de circuitos (adaptador de entrada en Swing).
 * Lista los circuitos en una tabla y permite registrar, actualizar,
 * buscar y eliminar mediante el puerto de entrada CircuitoServicio.
 */
public class VentanaCircuitos extends JFrame {

    private final CircuitoServicio circuitoServicio;

    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtNombre;
    private JTextField txtKilometros;
    private JTextField txtUbicacion;
    private JTextField txtNumCurvas;
    private JComboBox<String> comboTipoCircuito;
    private JTextField txtVueltasTipicas;
    private JTextField txtRecordVuelta;
    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public VentanaCircuitos(CircuitoServicio circuitoServicio) {

        this.circuitoServicio = Objects.requireNonNull(circuitoServicio,
                "El servicio de circuitos no puede ser nulo.");

        setTitle("Administración de Circuitos");
        setSize(720, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(12, 6, 16, 16));
        cabecera.add(TemaF1.titulo("Circuitos"), BorderLayout.WEST);

        btnEliminar = new JButton("Eliminar seleccionado");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

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
        tabla.setRowHeight(26);
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
                double kilometros = Double.parseDouble(txtKilometros.getText());
                String ubicacion = txtUbicacion.getText();
                int numCurvas = Integer.parseInt(txtNumCurvas.getText());
                String tipoCircuito = (String) comboTipoCircuito.getSelectedItem();
                int vueltasTipicas = Integer.parseInt(txtVueltasTipicas.getText());
                String recordVuelta = txtRecordVuelta.getText();

                circuitoServicio.registrar(nombre, kilometros, ubicacion,
                        numCurvas, tipoCircuito, vueltasTipicas, recordVuelta);

                actualizarTabla();
                limpiarCampos();

            } catch (Exception ex) {

                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registro de circuito", javax.swing.JOptionPane.ERROR_MESSAGE);

            }
        });

        btnEliminar.addActionListener(e -> {

            int fila = tabla.getSelectedRow();

            if (fila < 0) {

                javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un circuito de la tabla.");
                return;

            }

            String nombre = (String) modelo.getValueAt(fila, 0);

            if (circuitoServicio.eliminar(nombre)) {

                actualizarTabla();

            } else {

                javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el circuito.");

            }
        });

        btnBuscar.addActionListener(e -> {

            Circuito encontrado = circuitoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Circuito: " + encontrado.getNombre()
                                + " | " + encontrado.getKilometros() + " km"
                                + " | " + encontrado.getUbicacion()
                                + "\nCurvas: " + encontrado.getNumCurvas()
                                + " | Tipo: " + encontrado.getTipoCircuito()
                                + " | Vueltas: " + encontrado.getVueltasTipicas()
                                + "\nRécord: " + encontrado.getRecordVuelta());

            } else {

                javax.swing.JOptionPane.showMessageDialog(this, "Circuito no encontrado.");

            }
        });

        actualizarTabla();

    }

    private JPanel construirPanelBusqueda() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        txtBuscar = new JTextField(18);
        btnBuscar = new JButton("Buscar por nombre");

        panel.add(TemaF1.etiqueta("Nombre a buscar:"));
        panel.add(txtBuscar);
        panel.add(btnBuscar);

        return panel;
    }

    private JPanel construirPanelFormulario() {

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(4, 8, 4, 8);
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        JPanel panel = new JPanel(new java.awt.GridLayout(0, 2, 0, 4));

        txtNombre = new JTextField(16);
        txtKilometros = new JTextField(8);
        txtUbicacion = new JTextField(16);
        txtNumCurvas = new JTextField(6);
        comboTipoCircuito = new JComboBox<>(new String[]{"Permanente", "Urbano", "Semiacotico"});
        txtVueltasTipicas = new JTextField(6);
        txtRecordVuelta = new JTextField(16);
        btnRegistrar = new JButton("Registrar / Actualizar");

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre);
        panel.add(TemaF1.etiqueta("Kilómetros:"));
        panel.add(txtKilometros);
        panel.add(TemaF1.etiqueta("Ubicación:"));
        panel.add(txtUbicacion);
        panel.add(TemaF1.etiqueta("Nº Curvas:"));
        panel.add(txtNumCurvas);
        panel.add(TemaF1.etiqueta("Tipo circuito:"));
        panel.add(comboTipoCircuito);
        panel.add(TemaF1.etiqueta("Vueltas típicas:"));
        panel.add(txtVueltasTipicas);
        panel.add(TemaF1.etiqueta("Récord vuelta:"));
        panel.add(txtRecordVuelta);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panel, BorderLayout.CENTER);

        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filaBoton.add(btnRegistrar);
        contenedor.add(filaBoton, BorderLayout.SOUTH);

        return contenedor;
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

    private void limpiarCampos() {

        txtNombre.setText("");
        txtKilometros.setText("");
        txtUbicacion.setText("");
        txtNumCurvas.setText("");
        txtVueltasTipicas.setText("");
        txtRecordVuelta.setText("");

    }

}
