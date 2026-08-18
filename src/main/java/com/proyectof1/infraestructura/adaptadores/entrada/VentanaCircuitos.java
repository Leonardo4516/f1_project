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

/**
 * Ventana de gestión de circuitos (adaptador de entrada en Swing).
 * Permite listar, registrar, actualizar, buscar y eliminar circuitos mediante
 * el puerto de entrada CircuitoServicio.
 */
public class VentanaCircuitos extends JFrame {

    // Servicio de circuitos (puerto de entrada) inyectado.
    private final CircuitoServicio circuitoServicio;

    // Modelo de datos que alimenta la JList (lista de strings).
    private DefaultListModel<String> modeloLista;

    private JList<String> listaCircuitos;

    // Campos de texto del formulario.
    private JTextField txtNombre;
    private JTextField txtKilometros;
    private JTextField txtUbicacion;
    private JTextField txtBuscar;

    // Botones de acción.
    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    /**
     * Constructor de la ventana. Recibe el servicio de circuitos.
     * Se valida que no sea nulo.
     */
    public VentanaCircuitos(CircuitoServicio circuitoServicio) {

        if (circuitoServicio != null) {

            this.circuitoServicio = circuitoServicio;

        } else {

            throw new IllegalArgumentException("El servicio de circuitos no puede ser nulo.");

        }

        // Configuración básica de la ventana.
        setTitle("Administración de Circuitos");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Zona central: la lista de circuitos con barra de desplazamiento.
        modeloLista = new DefaultListModel<>();
        listaCircuitos = new JList<>(modeloLista);
        add(new JScrollPane(listaCircuitos), BorderLayout.CENTER);

        // Botón para eliminar el circuito seleccionado de la lista.
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

        // Panel inferior: agrupa búsqueda (arriba) y formulario (abajo).
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBuscar, BorderLayout.NORTH);
        panelSur.add(panelFormulario, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        // Acción del botón Registrar: lee los campos, llama al servicio y refresca.
        btnRegistrar.addActionListener(e -> {

            try {

                String nombre = txtNombre.getText();
                double kilometros = Double.parseDouble(txtKilometros.getText());
                String ubicacion = txtUbicacion.getText();

                circuitoServicio.registrar(nombre, kilometros, ubicacion);

                actualizarLista();
                limpiarCampos();

            } catch (Exception ex) {

                // Si el parseo o la validación del dominio fallan, muestra el error.
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de circuito", JOptionPane.ERROR_MESSAGE);

            }

        });

        // Acción del botón Eliminar: elimina el circuito seleccionado de la lista.
        btnEliminar.addActionListener(e -> {

            String seleccion = listaCircuitos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un circuito de la lista.");
                return;

            }

            // extraerNombre obtiene solo el nombre a partir de la cadena mostrada.
            if (circuitoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el circuito.");

            }

        });

        // Acción del botón Buscar: muestra los datos del circuito encontrado.
        btnBuscar.addActionListener(e -> {

            Circuito encontrado = circuitoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Circuito: " + encontrado.getNombre() + " | " + encontrado.getKilometros() + " km | " + encontrado.getUbicacion());

            } else {

                JOptionPane.showMessageDialog(this, "Circuito no encontrado.");

            }

        });

        // Al abrir la ventana se carga la lista con los circuitos existentes.
        actualizarLista();

    }

    /** Refresca la lista mostrando todos los circuitos del servicio. */
    private void actualizarLista() {

        modeloLista.clear();

        for (Circuito circuito : circuitoServicio.listarCircuitos()) {

            modeloLista.addElement(circuito.getNombre() + " | " + circuito.getKilometros() + " km | " + circuito.getUbicacion());

        }

    }

    /** Vacía los campos de texto del formulario. */
    private void limpiarCampos() {

        txtNombre.setText("");
        txtKilometros.setText("");
        txtUbicacion.setText("");

    }

    /** Extrae el nombre real de un elemento de la lista (texto antes del primer '|'). */
    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}