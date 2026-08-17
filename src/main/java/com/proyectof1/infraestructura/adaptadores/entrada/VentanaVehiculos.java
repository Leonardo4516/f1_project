package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public class VentanaVehiculos extends JFrame {

    private final VehiculoServicio vehiculoServicio;

    private final PilotoServicio pilotoServicio;

    private DefaultListModel<String> modeloLista;

    private DefaultComboBoxModel<String> modeloPilotos;

    private JList<String> listaVehiculos;

    private JTextField txtMarcaEscuderia;

    private JTextField txtVelocidadMaxima;

    private JTextField txtDesgaste;

    private JComboBox<String> comboPilotos;

    private JTextField txtBuscar;

    private JButton btnRegistrar;

    private JButton btnEliminar;

    private JButton btnBuscar;

    public VentanaVehiculos(VehiculoServicio vehiculoServicio, PilotoServicio pilotoServicio) {

        if (vehiculoServicio != null && pilotoServicio != null) {

            this.vehiculoServicio = vehiculoServicio;

            this.pilotoServicio = pilotoServicio;

        } else {

            throw new IllegalArgumentException("Los servicios de vehículo y piloto no pueden ser nulos.");

        }

        setTitle("Administración de Vehículos");

        setSize(520, 460);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        modeloLista = new DefaultListModel<>();

        listaVehiculos = new JList<>(modeloLista);

        add(new JScrollPane(listaVehiculos), BorderLayout.CENTER);

        btnEliminar = new JButton("Eliminar seleccionado");

        add(btnEliminar, BorderLayout.NORTH);

        JPanel panelBuscar = new JPanel(new FlowLayout());

        txtBuscar = new JTextField(15);

        btnBuscar = new JButton("Buscar por escudería");

        panelBuscar.add(new JLabel("Escudería a buscar:"));

        panelBuscar.add(txtBuscar);

        panelBuscar.add(btnBuscar);

        JPanel panelFormulario = new JPanel(new FlowLayout());

        txtMarcaEscuderia = new JTextField(9);

        txtVelocidadMaxima = new JTextField(5);

        txtDesgaste = new JTextField(5);

        modeloPilotos = new DefaultComboBoxModel<>();

        comboPilotos = new JComboBox<>(modeloPilotos);

        btnRegistrar = new JButton("Registrar/Actualizar");

        panelFormulario.add(new JLabel("Escudería:"));

        panelFormulario.add(txtMarcaEscuderia);

        panelFormulario.add(new JLabel("Vel. máx:"));

        panelFormulario.add(txtVelocidadMaxima);

        panelFormulario.add(new JLabel("Desgaste %:"));

        panelFormulario.add(txtDesgaste);

        panelFormulario.add(new JLabel("Piloto:"));

        panelFormulario.add(comboPilotos);

        panelFormulario.add(btnRegistrar);

        JPanel panelSur = new JPanel(new BorderLayout());

        panelSur.add(panelBuscar, BorderLayout.NORTH);

        panelSur.add(panelFormulario, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> {

            try {

                String marcaEscuderia = txtMarcaEscuderia.getText();

                int velocidadMaxima = Integer.parseInt(txtVelocidadMaxima.getText());

                double desgaste = Double.parseDouble(txtDesgaste.getText());

                String nombrePiloto = (String) comboPilotos.getSelectedItem();

                if (nombrePiloto == null) {

                    JOptionPane.showMessageDialog(this, "Registra pilotos antes de crear vehículos.");

                    return;

                }

                Piloto piloto = pilotoServicio.buscarPorNombre(nombrePiloto);

                vehiculoServicio.registrar(marcaEscuderia, velocidadMaxima, desgaste, piloto);

                actualizarLista();

                limpiarCampos();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de vehículo", JOptionPane.ERROR_MESSAGE);

            }

        });

        btnEliminar.addActionListener(e -> {

            String seleccion = listaVehiculos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un vehículo de la lista.");

                return;

            }

            if (vehiculoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el vehículo.");

            }

        });

        btnBuscar.addActionListener(e -> {

            Vehiculo encontrado = vehiculoServicio.buscarPorEscuderia(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Vehículo: " + encontrado.getMarcaEscuderia() + " | Vel: " + encontrado.getVelocidadMaxima() + " | Desgaste: " + encontrado.getDesgasteNeumaticos() + "% | Piloto: " + encontrado.getPiloto().getNombre());

            } else {

                JOptionPane.showMessageDialog(this, "Vehículo no encontrado.");

            }

        });

        actualizarLista();

        actualizarComboPilotos();

    }

    private void actualizarLista() {

        modeloLista.clear();

        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {

            modeloLista.addElement(vehiculo.getMarcaEscuderia() + " | Vel: " + vehiculo.getVelocidadMaxima() + " | Desgaste: " + vehiculo.getDesgasteNeumaticos() + "% | Piloto: " + vehiculo.getPiloto().getNombre());

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

        txtDesgaste.setText("");

    }

    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}