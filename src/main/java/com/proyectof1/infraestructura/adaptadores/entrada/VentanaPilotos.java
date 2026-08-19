package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.dominio.Piloto;

/**
 * Ventana de gestión de pilotos (adaptador de entrada en Swing).
 * Lista los pilotos en una tabla y permite registrar, actualizar,
 * buscar y eliminar mediante el puerto de entrada PilotoServicio.
 */
public class VentanaPilotos extends JFrame {

    // Servicio de pilotos (puerto de entrada) inyectado.
    private final PilotoServicio pilotoServicio;

    // Modelo de datos que alimenta la JTable (no editable).
    private DefaultTableModel modelo;

    private JTable tabla;

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

        this.pilotoServicio = Objects.requireNonNull(pilotoServicio,
                "El servicio de pilotos no puede ser nulo.");

        // Configuración básica de la ventana.
        setTitle("Administración de Pilotos");
        setSize(580, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----- Cabecera: título y botón de eliminar. -----
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(12, 6, 16, 16));
        cabecera.add(TemaF1.titulo("Pilotos"), BorderLayout.WEST);

        btnEliminar = new JButton("Eliminar seleccionado");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ----- Centro: tabla con los pilotos. -----
        modelo = new DefaultTableModel(new String[]{"Nombre", "Experiencia (1-100)", "Habilidad lluvia (1-100)"}, 0) {

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
                int experiencia = Integer.parseInt(txtExperiencia.getText());
                int habilidadLluvia = Integer.parseInt(txtHabilidadLluvia.getText());

                pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);

                actualizarTabla();
                limpiarCampos();

            } catch (Exception ex) {

                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Registro de piloto", javax.swing.JOptionPane.ERROR_MESSAGE);

            }
        });

        // Acción del botón Eliminar: borra el piloto seleccionado de la tabla.
        btnEliminar.addActionListener(e -> {

            int fila = tabla.getSelectedRow();

            if (fila < 0) {

                javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un piloto de la tabla.");
                return;

            }

            String nombre = (String) modelo.getValueAt(fila, 0);

            if (pilotoServicio.eliminar(nombre)) {

                actualizarTabla();

            } else {

                javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el piloto.");

            }
        });

        // Acción del botón Buscar: muestra los datos del piloto encontrado.
        btnBuscar.addActionListener(e -> {

            Piloto encontrado = pilotoServicio.buscarPorNombre(txtBuscar.getText());

            if (encontrado != null) {

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Piloto: " + encontrado.getNombre()
                                + " | Exp: " + encontrado.getExperiencia()
                                + " | Lluvia: " + encontrado.getHabilidadLluvia());

            } else {

                javax.swing.JOptionPane.showMessageDialog(this, "Piloto no encontrado.");

            }
        });

        // Al abrir la ventana se carga la tabla con los pilotos existentes.
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
        txtExperiencia = new JTextField(5);
        txtHabilidadLluvia = new JTextField(5);
        btnRegistrar = new JButton("Registrar / Actualizar");

        panel.add(TemaF1.etiqueta("Nombre:"));
        panel.add(txtNombre);
        panel.add(TemaF1.etiqueta("Experiencia:"));
        panel.add(txtExperiencia);
        panel.add(TemaF1.etiqueta("Lluvia:"));
        panel.add(txtHabilidadLluvia);

        // El botón se coloca debajo, centrado, para no desbordar la fila.
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panel, BorderLayout.CENTER);

        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filaBoton.add(btnRegistrar);
        contenedor.add(filaBoton, BorderLayout.SOUTH);

        return contenedor;
    }

    /** Refresca la tabla mostrando todos los pilotos del servicio. */
    private void actualizarTabla() {

        modelo.setRowCount(0);

        for (Piloto piloto : pilotoServicio.listarPilotos()) {

            modelo.addRow(new Object[]{piloto.getNombre(), piloto.getExperiencia(), piloto.getHabilidadLluvia()});

        }
    }

    /** Vacía los campos de texto del formulario. */
    private void limpiarCampos() {

        txtNombre.setText("");
        txtExperiencia.setText("");
        txtHabilidadLluvia.setText("");

    }

}