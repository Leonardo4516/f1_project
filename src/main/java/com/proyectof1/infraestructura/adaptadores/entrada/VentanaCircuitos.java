package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

    // Servicio de circuitos (puerto de entrada) inyectado.
    private final CircuitoServicio circuitoServicio;

    // Modelo de datos que alimenta la JTable (no editable).
    private DefaultTableModel modelo;

    private JTable tabla;

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

        if (circuitoServicio == null) {

            throw new IllegalArgumentException("El servicio de circuitos no puede ser nulo.");

        }

        this.circuitoServicio = circuitoServicio;

        // Configuración básica de la ventana.
        setTitle("Administración de Circuitos");
        setSize(580, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera: título y botón de eliminar. -----
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(12, 6, 16, 16));
        cabecera.add(TemaF1.titulo("Circuitos"), BorderLayout.WEST);

        btnEliminar = new JButton("Eliminar seleccionado");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ----- Centro: tabla con los circuitos. -----
        modelo = new DefaultTableModel(new String[]{"Nombre", "Kilómetros (km)", "Ubicación"}, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(26);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ----- Sur: fila de búsqueda y fila de formulario. -----
        JPanel cuerpoSur = new JPanel();
        cuerpoSur.setLayout(new BoxLayout(cuerpoSur, BoxLayout.Y_AXIS));
        cuerpoSur.setBorder(TemaF1.margenes(8, 12, 16, 16));
        cuerpoSur.add(construirPanelBusqueda());
        cuerpoSur.add(construirPanelFormulario());
        add(cuerpoSur, BorderLayout.SOUTH);

        // Acción del botón Registrar: lee los campos, llama al servicio y refresca.
        btnRegistrar.addActionListener(e -> {

            try {

                String nombre = txtNombre.getText();
                double kilometros = Double.parseDouble(txtKilometros.getText());
                String ubicacion = txtUbicacion.getText();

                circuitoServicio.registrar(nombre, kilometros, ubicacion);

                actualizarTabla();
                limpiarCampos();

            } catch (Exception ex) {

                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registro de circuito", javax.swing.JOptionPane.ERROR_MESSAGE);

            }
        });

        // Acción del botón Eliminar: borra el circuito seleccionado de la tabla.
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

        // Acción del botón Buscar: muestra los datos del circuito encontrado.
        btnBuscar.addActionListener(e -> {

            Circuito encontrado = circuitoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Circuito: " + encontrado.getNombre() + " | " + encontrado.getKilometros() + " km | " + encontrado.getUbicacion());

            } else {

                javax.swing.JOptionPane.showMessageDialog(this, "Circuito no encontrado.");

            }
        });

        // Al abrir la ventana se carga la tabla con los circuitos existentes.
        actualizarTabla();

    }

    /** Construye la fila superior del sur: búsqueda por nombre. */
    private JPanel construirPanelBusqueda() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        txtBuscar = new JTextField(18);
        btnBuscar = new JButton("Buscar por nombre");

        panel.add(TemaF1.etiqueta("Nombre a buscar:"));
        panel.add(txtBuscar);
        panel.add(btnBuscar);

        return panel;
    }

    /** Construye la fila inferior del sur: formulario para registrar/actualizar. */
    private JPanel construirPanelFormulario() {

        // Usa GridLayout para que todos los pares etiqueta-campo queden alineados.
        JPanel panel = new JPanel(new GridLayout(1, 6, 8, 0));

        txtNombre = new JTextField(10);
        txtKilometros = new JTextField(6);
        txtUbicacion = new JTextField(10);
        btnRegistrar = new JButton("Registrar / Actualizar");

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre);
        panel.add(TemaF1.etiqueta("Km:"));
        panel.add(txtKilometros);
        panel.add(TemaF1.etiqueta("Ubicación:"));
        panel.add(txtUbicacion);

        // El botón se coloca debajo, centrado, para no desbordar la fila.
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panel, BorderLayout.CENTER);

        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filaBoton.add(btnRegistrar);
        contenedor.add(filaBoton, BorderLayout.SOUTH);

        return contenedor;
    }

    /** Refresca la tabla mostrando todos los circuitos del servicio. */
    private void actualizarTabla() {

        modelo.setRowCount(0);

        for (Circuito circuito : circuitoServicio.listarCircuitos()) {

            modelo.addRow(new Object[]{circuito.getNombre(), circuito.getKilometros(), circuito.getUbicacion()});

        }
    }

    /** Vacía los campos de texto del formulario. */
    private void limpiarCampos() {

        txtNombre.setText("");
        txtKilometros.setText("");
        txtUbicacion.setText("");

    }

}