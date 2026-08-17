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

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.dominio.Piloto;

public class VentanaPilotos extends JFrame {

    private final PilotoServicio pilotoServicio;

    private DefaultListModel<String> modeloLista;

    private JList<String> listaPilotos;

    private JTextField txtNombre;

    private JTextField txtExperiencia;

    private JTextField txtHabilidadLluvia;

    private JTextField txtBuscar;

    private JButton btnRegistrar;

    private JButton btnEliminar;

    private JButton btnBuscar;

    public VentanaPilotos(PilotoServicio pilotoServicio) {

        if (pilotoServicio != null) {

            this.pilotoServicio = pilotoServicio;

        } else {

            throw new IllegalArgumentException("El servicio de pilotos no puede ser nulo.");

        }

        setTitle("Administración de Pilotos");

        setSize(500, 450);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        modeloLista = new DefaultListModel<>();

        listaPilotos = new JList<>(modeloLista);

        add(new JScrollPane(listaPilotos), BorderLayout.CENTER);

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

        txtExperiencia = new JTextField(6);

        txtHabilidadLluvia = new JTextField(6);

        btnRegistrar = new JButton("Registrar/Actualizar");

        panelFormulario.add(new JLabel("Nombre:"));

        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Experiencia:"));

        panelFormulario.add(txtExperiencia);

        panelFormulario.add(new JLabel("Lluvia:"));

        panelFormulario.add(txtHabilidadLluvia);

        panelFormulario.add(btnRegistrar);

        JPanel panelSur = new JPanel(new BorderLayout());

        panelSur.add(panelBuscar, BorderLayout.NORTH);

        panelSur.add(panelFormulario, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> {

            try {

                String nombre = txtNombre.getText();

                int experiencia = Integer.parseInt(txtExperiencia.getText());

                int habilidadLluvia = Integer.parseInt(txtHabilidadLluvia.getText());

                pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);

                actualizarLista();

                limpiarCampos();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de piloto", JOptionPane.ERROR_MESSAGE);

            }

        });

        btnEliminar.addActionListener(e -> {

            String seleccion = listaPilotos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un piloto de la lista.");

                return;

            }

            if (pilotoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el piloto.");

            }

        });

        btnBuscar.addActionListener(e -> {

            Piloto encontrado = pilotoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Piloto: " + encontrado.getNombre() + " | Exp: " + encontrado.getExperiencia() + " | Lluvia: " + encontrado.getHabilidadLluvia());

            } else {

                JOptionPane.showMessageDialog(this, "Piloto no encontrado.");

            }

        });

        actualizarLista();

    }

    private void actualizarLista() {

        modeloLista.clear();

        for (Piloto piloto : pilotoServicio.listarPilotos()) {

            modeloLista.addElement(piloto.getNombre() + " | Exp: " + piloto.getExperiencia() + " | Lluvia: " + piloto.getHabilidadLluvia());

        }

    }

    private void limpiarCampos() {

        txtNombre.setText("");

        txtExperiencia.setText("");

        txtHabilidadLluvia.setText("");

    }

    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}