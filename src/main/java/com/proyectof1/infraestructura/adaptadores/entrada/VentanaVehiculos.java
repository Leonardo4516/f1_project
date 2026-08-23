package com.proyectof1.infraestructura.adaptadores.entrada;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

/**
 * Ventana de gestión de vehículos (adaptador de entrada en Swing).
 * Lista los vehículos en una tabla (con el color oficial de cada escudería)
 * y permite registrar, actualizar, buscar y eliminar mediante el puerto de
 * entrada VehiculoServicio. Para asignar el piloto consulta el servicio de
 * pilotos y lo muestra en un desplegable.
 */
public class VentanaVehiculos extends JFrame {

    // Servicios (puertos de entrada) inyectados.
    private final VehiculoServicio vehiculoServicio;
    private final PilotoServicio pilotoServicio;

    // Modelo de datos que alimenta la JTable (no editable).
    private DefaultTableModel modelo;

    private JTable tabla;

    // Modelo del combo de pilotos (lista de nombres).
    private DefaultComboBoxModel<String> modeloPilotos;

    // Campos de texto del formulario.
    private JTextField txtMarcaEscuderia;
    private JTextField txtVelocidadMaxima;
    private JTextField txtAceleracion;
    private JTextField txtFrenado;
    private JTextField txtAgarre;

    // Desplegable para elegir el piloto asignado.
    private JComboBox<String> comboPilotos;

    private JTextField txtBuscar;

    // Botones de acción.
    private JButton btnRegistrar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    /**
     * Constructor de la ventana. Recibe el servicio de vehículos y el de pilotos.
     * Se valida que ninguno sea nulo.
     */
    public VentanaVehiculos(VehiculoServicio vehiculoServicio, PilotoServicio pilotoServicio) {

        this.vehiculoServicio = Objects.requireNonNull(vehiculoServicio,
                "Los servicios de vehículo y piloto no pueden ser nulos.");
        this.pilotoServicio = Objects.requireNonNull(pilotoServicio,
                "Los servicios de vehículo y piloto no pueden ser nulos.");

        // Configuración básica de la ventana.
        setTitle("Administración de Vehículos");
        setSize(640, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ----- Cabecera: título y botón de eliminar. -----
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(TemaF1.margenes(12, 6, 16, 16));
        cabecera.add(TemaF1.titulo("Vehículos"), BorderLayout.WEST);

        btnEliminar = new JButton("Eliminar seleccionado");
        TemaF1.estilizarBoton(btnEliminar);
        cabecera.add(btnEliminar, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ----- Centro: tabla con los vehículos. -----
        modelo = new DefaultTableModel(new String[]{"Escudería", "Vel. máx (km/h)", "Acel", "Fren", "Agar", "Piloto"}, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(26);
        tabla.getColumnModel().getColumn(0).setCellRenderer(new RendererEscuderia());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ----- Sur: fila de búsqueda y fila de formulario. -----
        JPanel cuerpoSur = new JPanel();
        cuerpoSur.setLayout(new BoxLayout(cuerpoSur, BoxLayout.Y_AXIS));
        cuerpoSur.setBorder(TemaF1.margenes(8, 12, 16, 16));
        cuerpoSur.add(construirPanelBusqueda());
        cuerpoSur.add(construirPanelFormulario());
        add(cuerpoSur, BorderLayout.SOUTH);

        // Acción del botón Registrar: lee los campos, busca el piloto y registra.
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

        // Acción del botón Eliminar: borra el vehículo seleccionado de la tabla.
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

        // Acción del botón Buscar: muestra los datos del vehículo encontrado.
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

        // Al abrir la ventana se cargan la tabla y el combo de pilotos.
        actualizarTabla();
        actualizarComboPilotos();

    }

    /** Construye la fila superior del sur: búsqueda por escudería. */
    private JPanel construirPanelBusqueda() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        txtBuscar = new JTextField(18);
        btnBuscar = new JButton("Buscar por escudería");

        panel.add(TemaF1.etiqueta("Escudería a buscar:"));
        panel.add(txtBuscar);
        panel.add(btnBuscar);

        return panel;
    }

    /** Construye la fila inferior del sur: formulario para registrar/actualizar. */
    private JPanel construirPanelFormulario() {

        // Fila 1: Escudería y Velocidad máxima
        JPanel fila1 = new JPanel(new GridLayout(1, 4, 8, 0));
        txtMarcaEscuderia = new JTextField(9);
        txtVelocidadMaxima = new JTextField(5);
        fila1.add(TemaF1.etiqueta("Escudería:"));
        fila1.add(txtMarcaEscuderia);
        fila1.add(TemaF1.etiqueta("Vel. máx:"));
        fila1.add(txtVelocidadMaxima);

        // Fila 2: Aceleración, Frenado y Agarre
        JPanel fila2 = new JPanel(new GridLayout(1, 6, 8, 0));
        txtAceleracion = new JTextField(4);
        txtFrenado = new JTextField(4);
        txtAgarre = new JTextField(4);
        fila2.add(TemaF1.etiqueta("Acel:"));
        fila2.add(txtAceleracion);
        fila2.add(TemaF1.etiqueta("Fren:"));
        fila2.add(txtFrenado);
        fila2.add(TemaF1.etiqueta("Agarre:"));
        fila2.add(txtAgarre);

        // Fila 3: Piloto
        JPanel fila3 = new JPanel(new GridLayout(1, 2, 8, 0));
        modeloPilotos = new DefaultComboBoxModel<>();
        comboPilotos = new JComboBox<>(modeloPilotos);
        fila3.add(TemaF1.etiqueta("Piloto:"));
        fila3.add(comboPilotos);

        btnRegistrar = new JButton("Registrar / Actualizar");

        // El botón se coloca debajo, centrado, para no desbordar la fila.
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.add(fila1);
        contenedor.add(fila2);
        contenedor.add(fila3);

        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filaBoton.add(btnRegistrar);
        contenedor.add(filaBoton);

        return contenedor;
    }

    /** Refresca la tabla mostrando todos los vehículos del servicio. */
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

    /** Refresca el desplegable con los nombres de todos los pilotos registrados. */
    private void actualizarComboPilotos() {

        modeloPilotos.removeAllElements();

        for (Piloto piloto : pilotoServicio.listarPilotos()) {

            modeloPilotos.addElement(piloto.getNombre());

        }
    }

    /** Vacía los campos de texto del formulario. */
    private void limpiarCampos() {

        txtMarcaEscuderia.setText("");
        txtVelocidadMaxima.setText("");
        txtAceleracion.setText("");
        txtFrenado.setText("");
        txtAgarre.setText("");

    }

    /**
     * Renderer que pinta la celda de la escudería con su color oficial
     * y el texto en negro y negrita para que destaque sobre el color.
     */
    private static class RendererEscuderia extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {

            super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila, columna);

            setHorizontalAlignment(CENTER);

            // Solo pintamos cuando no está seleccionado; si lo está, se ven
            // los colores de selección de FlatLaf.
            if (!seleccionado) {

                setBackground(TemaF1.colorDeEscuderia((String) valor));
                setForeground(java.awt.Color.BLACK);
                setFont(getFont().deriveFont(Font.BOLD));

            }

            return this;
        }
    }

}