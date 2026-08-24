package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
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

        JTextField txtNombre = new JTextField();
        JTextField txtKilometros = new JTextField();
        JTextField txtUbicacion = new JTextField();
        JTextField txtNumCurvas = new JTextField();
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Permanente", "Urbano", "Semiacotico"});
        JTextField txtVueltas = new JTextField();
        JTextField txtRecord = new JTextField();

        JButton btnAceptar = new JButton(TemaF1.icono("add"));
        btnAceptar.setText(" Registrar");
        TemaF1.estilizarBoton(btnAceptar);

        JButton btnCancelar = new JButton("Cancelar");
        TemaF1.estilizarBoton(btnCancelar);

        JPanel panel = new JPanel(new MigLayout(
                "insets 16, gap 10, fill, flowy",
                "[right]rel[grow,fill]",
                "[][][][][][][]20[]"));
        panel.setBackground(TemaF1.FONDO);

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre, "growx");

        panel.add(TemaF1.etiqueta("Kilómetros:"));
        panel.add(txtKilometros, "growx");

        panel.add(TemaF1.etiqueta("Ubicación:"));
        panel.add(txtUbicacion, "growx");

        panel.add(TemaF1.etiqueta("Nº Curvas:"));
        panel.add(txtNumCurvas, "growx");

        panel.add(TemaF1.etiqueta("Tipo:"));
        panel.add(comboTipo, "growx");

        panel.add(TemaF1.etiqueta("Vueltas típicas:"));
        panel.add(txtVueltas, "growx");

        panel.add(TemaF1.etiqueta("Récord de vuelta:"));
        panel.add(txtRecord, "growx");

        panel.add(btnAceptar, "w 140!");
        panel.add(btnCancelar, "w 120!");

        dialogo.setContentPane(panel);
        dialogo.setSize(450, 460);
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
