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

/**
 * Ventana de gestión de vehículos (adaptador de entrada en Swing).
 * Permite listar, registrar, actualizar, buscar y eliminar vehículos mediante
 * el puerto de entrada VehiculoServicio. Para asignar el piloto, consulta el
 * servicio de pilotos y lo muestra en un desplegable (JComboBox).
 */
public class VentanaVehiculos extends JFrame {

    // Servicios (puertos de entrada) inyectados.
    private final VehiculoServicio vehiculoServicio;
    private final PilotoServicio pilotoServicio;

    // Modelo de datos que alimenta la JList (lista de strings).
    private DefaultListModel<String> modeloLista;

    // Modelo del combo de pilotos (lista de nombres).
    private DefaultComboBoxModel<String> modeloPilotos;

    private JList<String> listaVehiculos;

    // Campos de texto del formulario.
    private JTextField txtMarcaEscuderia;
    private JTextField txtVelocidadMaxima;
    private JTextField txtDesgaste;

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

        if (vehiculoServicio != null && pilotoServicio != null) {

            this.vehiculoServicio = vehiculoServicio;
            this.pilotoServicio = pilotoServicio;

        } else {

            throw new IllegalArgumentException("Los servicios de vehículo y piloto no pueden ser nulos.");

        }

        // Configuración básica de la ventana.
        setTitle("Administración de Vehículos");
        setSize(520, 460);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Zona central: la lista de vehículos con barra de desplazamiento.
        modeloLista = new DefaultListModel<>();
        listaVehiculos = new JList<>(modeloLista);
        add(new JScrollPane(listaVehiculos), BorderLayout.CENTER);

        // Botón para eliminar el vehículo seleccionado de la lista.
        btnEliminar = new JButton("Eliminar seleccionado");
        add(btnEliminar, BorderLayout.NORTH);

        // Panel superior-superior: búsqueda por escudería.
        JPanel panelBuscar = new JPanel(new FlowLayout());
        txtBuscar = new JTextField(15);
        btnBuscar = new JButton("Buscar por escudería");

        panelBuscar.add(new JLabel("Escudería a buscar:"));
        panelBuscar.add(txtBuscar);
        panelBuscar.add(btnBuscar);

        // Panel inferior-superior: formulario para registrar/actualizar.
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

        // Panel inferior: agrupa búsqueda (arriba) y formulario (abajo).
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBuscar, BorderLayout.NORTH);
        panelSur.add(panelFormulario, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        // Acción del botón Registrar: lee los campos, busca el piloto seleccionado y registra.
        btnRegistrar.addActionListener(e -> {

            try {

                String marcaEscuderia = txtMarcaEscuderia.getText();
                int velocidadMaxima = Integer.parseInt(txtVelocidadMaxima.getText());
                double desgaste = Double.parseDouble(txtDesgaste.getText());
                String nombrePiloto = (String) comboPilotos.getSelectedItem();

                // Si no hay pilotos registrados, no se puede crear el vehículo.
                if (nombrePiloto == null) {

                    JOptionPane.showMessageDialog(this, "Registra pilotos antes de crear vehículos.");
                    return;

                }

                // Recupera el piloto completo elegido en el combo.
                Piloto piloto = pilotoServicio.buscarPorNombre(nombrePiloto);

                vehiculoServicio.registrar(marcaEscuderia, velocidadMaxima, desgaste, piloto);

                actualizarLista();
                limpiarCampos();

            } catch (Exception ex) {

                // Si el parseo o la validación del dominio fallan, muestra el error.
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Registro de vehículo", JOptionPane.ERROR_MESSAGE);

            }

        });

        // Acción del botón Eliminar: elimina el vehículo seleccionado de la lista.
        btnEliminar.addActionListener(e -> {

            String seleccion = listaVehiculos.getSelectedValue();

            if (seleccion == null) {

                JOptionPane.showMessageDialog(this, "Selecciona un vehículo de la lista.");
                return;

            }

            // extraerNombre obtiene solo la escudería a partir de la cadena mostrada.
            if (vehiculoServicio.eliminar(extraerNombre(seleccion))) {

                actualizarLista();

            } else {

                JOptionPane.showMessageDialog(this, "No se encontró el vehículo.");

            }

        });

        // Acción del botón Buscar: muestra los datos del vehículo encontrado.
        btnBuscar.addActionListener(e -> {

            Vehiculo encontrado = vehiculoServicio.buscarPorEscuderia(txtBuscar.getText());

            if (encontrado != null) {

                JOptionPane.showMessageDialog(this, "Vehículo: " + encontrado.getMarcaEscuderia() + " | Vel: " + encontrado.getVelocidadMaxima() + " | Desgaste: " + encontrado.getDesgasteNeumaticos() + "% | Piloto: " + encontrado.getPiloto().getNombre());

            } else {

                JOptionPane.showMessageDialog(this, "Vehículo no encontrado.");

            }

        });

        // Al abrir la ventana se cargan la lista de vehículos y el combo de pilotos.
        actualizarLista();
        actualizarComboPilotos();

    }

    /** Refresca la lista mostrando todos los vehículos del servicio. */
    private void actualizarLista() {

        modeloLista.clear();

        for (Vehiculo vehiculo : vehiculoServicio.listarVehiculos()) {

            modeloLista.addElement(vehiculo.getMarcaEscuderia() + " | Vel: " + vehiculo.getVelocidadMaxima() + " | Desgaste: " + vehiculo.getDesgasteNeumaticos() + "% | Piloto: " + vehiculo.getPiloto().getNombre());

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
        txtDesgaste.setText("");

    }

    /** Extrae la escudería real de un elemento de la lista (texto antes del primer '|'). */
    private String extraerNombre(String elemento) {

        return elemento.split("\\|")[0].trim();

    }

}