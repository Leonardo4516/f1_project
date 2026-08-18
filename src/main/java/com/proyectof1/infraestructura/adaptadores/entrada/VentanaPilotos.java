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

/**
 * Ventana de gestión de pilotos (adaptador de entrada en Swing).
 * Permite listar, registrar, actualizar, buscar y eliminar pilotos mediante
 * el puerto de entrada PilotoServicio.
 */
public class VentanaPilotos extends JFrame {

    // Servicio de pilotos (puerto de entrada) inyectado.
    private final PilotoServicio pilotoServicio;

    // Modelo de datos que alimenta la JList (lista de strings).
    private DefaultListModel<String> modeloLista;

    private JList<String> listaPilotos;

    // Campos de texto del formulario.
    private JTextField txtNombre;
    private JTextField txtExperiencia;
    private JTextField txtHabilidadLluvia;
    private JTextField txtBuscar;

    // Botones de acción.
    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    /**
     * Constructor de la ventana. Recibe el servicio de pilotos.
     * Se valida que no sea nulo.
     */
    public VentanaPilotos(PilotoServicio pilotoServicio) {

        if (pilotoServicio != null) {

            this.pilotoServicio = pilotoServicio;

        } else {

            throw new IllegalArgumentException("El servicio de pilotos no puede ser nulo.");

        }

        // Configuración básica de la ventana.
        setTitle("Administración de Pilotos");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Zona central: la lista de pilotos con barra de desplazamiento.
        modeloLista = new DefaultListModel<>();
        listaPilotos = new JList<>(modeloLista);
        add(new JScrollPane(listaPilotos), BorderLayout.CENTER);

        // Botón para eliminar el piloto seleccionado de la lista.
        btnEliminar = new JButton("Eliminar seleccionado");
        add(btnEliminar, BorderLayout.NORTH);

        // Panel superior-superior: búsqueda por nombre.
        JPanel panelBuscar = new JPanel(new FlowLayout());
        txtBuscar = new JTextField(15);
        btnBuscar = new JButton("Buscar por nombre");

        panelBuscar.add(new JLabel("Nombre a buscar:"));
        panelBuscar.add(txtBuscar);
        panelBuscar.add(btnBuscar);

        // Panel inferior-superior: formulario para registrar/actualizar.
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

        // Panel inferior: agrupa búsqueda (arriba) y formulario (abajo).
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBuscar, BorderLayout.NORTH);
        panelSur.add(panelFormulario, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        // Acción del botón Registrar: lee los campos, llama al servicio y refresca.
        btnRegistrar.addActionListener(e -> {

            try {

                String nombre = txtNombre.getText();
                int experiencia = Integer.parseInt(txtExperiencia.getText());
                int habilidadLluvia = Integer.parseInt(txtHabilidadLluvia.getText());

                pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);

                actualizarLista();
                limpiarCampos();

            } catch (Exception ex) {

                // Si el parseo o la validación del dominio fallan, muestra el error.
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de piloto", JOptionPane.ERROR_MESSAGE);

            }

        });

        // Acción del botón Eliminar: elimina el piloto seleccionado de la lista.
        btnEliminar.addActionListener(e -> {

            String seleccion = listaPilotos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un piloto de la lista.");
                return;

            }

            // extraerNombre obtiene solo el nombre a partir de la cadena mostrada.
            if (pilotoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el piloto.");

            }

        });

        // Acción del botón Buscar: muestra los datos del piloto encontrado.
        btnBuscar.addActionListener(e -> {

            Piloto encontrado = pilotoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Piloto: " + encontrado.getNombre() + " | Exp: " + encontrado.getExperiencia() + " | Lluvia: " + encontrado.getHabilidadLluvia());

            } else {

                JOptionPane.showMessageDialog(this, "Piloto no encontrado.");

            }

        });

        // Al abrir la ventana se carga la lista con los pilotos existentes.
        actualizarLista();

    }

    /** Refresca la lista mostrando todos los pilotos del servicio. */
    private void actualizarLista() {

        modeloLista.clear();

        for (Piloto piloto : pilotoServicio.listarPilotos()) {

            modeloLista.addElement(piloto.getNombre() + " | Exp: " + piloto.getExperiencia() + " | Lluvia: " + piloto.getHabilidadLluvia());

        }

    }

    /** Vacía los campos de texto del formulario. */
    private void limpiarCampos() {

        txtNombre.setText("");
        txtExperiencia.setText("");
        txtHabilidadLluvia.setText("");

    }

    /** Extrae el nombre real de un elemento de la lista (texto antes del primer '|'). */
    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}