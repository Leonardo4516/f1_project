package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.dominio.Circuito;

public class VentanaCircuitos extends JFrame {

    private final CircuitoServicio circuitoServicio;

    private DefaultListModel<String> modeloLista;

    private JList<String> listaCircuitos;

    private JTextField txtNombre;

    private JTextField txtKilometros;

    private JTextField txtUbicacion;

    private JTextField txtBuscar;

    private JButton btnRegistrar;

    private JButton btnEliminar;

    private JButton btnBuscar;

    public VentanaCircuitos(CircuitoServicio circuitoServicio) {

        if (circuitoServicio != null) {

            this.circuitoServicio = circuitoServicio;

        } else {

            throw new IllegalArgumentException("El servicio de circuitos no puede ser nulo.");

        }

        setTitle("Administración de Circuitos");

        setSize(500, 450);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        modeloLista = new DefaultListModel<>();

        listaCircuitos = new JList<>(modeloLista);

        add(new JScrollPane(listaCircuitos), BorderLayout.CENTER);

        btnEliminar = new JButton("Eliminar seleccionado");

        add(btnEliminar, BorderLayout.NORTH);

        JPanel panelBuscar = new JPanel(new FlowLayout());

        txtBuscar = new JTextField(15);

        btnBuscar = new JButton("Buscar por nombre");

        panelBuscar.add(new JLabel("Nombre a buscar:"));

        panelBuscar.add(txtBuscar);

        panelBuscar.add(btnBuscar);

        JPanel panelFormulario = new JPanel(new FlowLayout());

        txtNombre = new JTextField(10);

        txtKilometros = new JTextField(6);

        txtUbicacion = new JTextField(10);

        btnRegistrar = new JButton("Registrar/Actualizar");

        panelFormulario.add(new JLabel("Nombre:"));

        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Kilómetros:"));

        panelFormulario.add(txtKilometros);

        panelFormulario.add(new JLabel("Ubicación:"));

        panelFormulario.add(txtUbicacion);

        panelFormulario.add(btnRegistrar);

        JPanel panelSur = new JPanel(new BorderLayout());

        panelSur.add(panelBuscar, BorderLayout.NORTH);

        panelSur.add(panelFormulario, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> {

            try {

                String nombre = txtNombre.getText();

                double kilometros = Double.parseDouble(txtKilometros.getText());

                String ubicacion = txtUbicacion.getText();

                circuitoServicio.registrar(nombre, kilometros, ubicacion);

                actualizarLista();

                limpiarCampos();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de circuito", JOptionPane.ERROR_MESSAGE);

            }

        });

        btnEliminar.addActionListener(e -> {

            String seleccion = listaCircuitos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un circuito de la lista.");

                return;

            }

            if (circuitoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el circuito.");

            }

        });

        btnBuscar.addActionListener(e -> {

            Circuito encontrado = circuitoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Circuito: " + encontrado.getNombre() + " | " + encontrado.getKilometros() + " km | " + encontrado.getUbicacion());

            } else {

                JOptionPane.showMessageDialog(this, "Circuito no encontrado.");

            }

        });

        actualizarLista();

    }

    private void actualizarLista() {

        modeloLista.clear();

        for (Circuito circuito : circuitoServicio.listarCircuitos()) {

            modeloLista.addElement(circuito.getNombre() + " | " + circuito.getKilometros() + " km | " + circuito.getUbicacion());

        }

    }

    private void limpiarCampos() {

        txtNombre.setText("");

        txtKilometros.setText("");

        txtUbicacion.setText("");

    }

    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}