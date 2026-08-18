package com.proyectof1;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.servicios.CircuitoServicioImpl;
import com.proyectof1.aplicacion.servicios.PilotoServicioImpl;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.aplicacion.servicios.VehiculoServicioImpl;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.infraestructura.adaptadores.entrada.VentanaPrincipal;
import com.proyectof1.infraestructura.adaptadores.salida.CircuitosRepositorioEnMemoria;
import com.proyectof1.infraestructura.adaptadores.salida.ClimaHttpAdapter;
import com.proyectof1.infraestructura.adaptadores.salida.PilotosRepositorioEnMemoria;
import com.proyectof1.infraestructura.adaptadores.salida.VehiculosRepositorioEnMemoria;

/**
 * Clase principal del programa. Ejecuta la composición de todo el sistema:
 *  - Crea las dependencias de infraestructura (adaptadores de salida).
 *  - Las inyecta en los servicios de aplicación (puertos de entrada).
 *  - Carga unos datos de prueba.
 *  - Abre la ventana principal (adaptador de entrada en Swing).
 * Este punto de montaje no usa frameworks de inyección: se hace a mano,
 * siguiendo la arquitectura hexagonal (dominio + aplicación + infraestructura).
 */
public class Main {

    public static void main(String[] args) {

        // Creación de las dependencias de infraestructura.
        ClimaHttpAdapter climaAdapter = new ClimaHttpAdapter();

        // El servicio de simulación necesita el adaptador de clima.
        SimulacionService simulacionService = new SimulacionService(climaAdapter);

        // Los servicios de gestión se construyen con sus repositorios en memoria.
        CircuitoServicio circuitoServicio = new CircuitoServicioImpl(new CircuitosRepositorioEnMemoria());

        PilotoServicio pilotoServicio = new PilotoServicioImpl(new PilotosRepositorioEnMemoria());

        VehiculoServicio vehiculoServicio = new VehiculoServicioImpl(new VehiculosRepositorioEnMemoria());

        // Carga de datos de ejemplo para que la aplicación arranque con contenido.
        cargarDatosDePrueba(pilotoServicio, circuitoServicio, vehiculoServicio);

        // Creación y visualización de la ventana principal de la interfaz.
        VentanaPrincipal principal = new VentanaPrincipal(circuitoServicio, pilotoServicio, vehiculoServicio, simulacionService);

        principal.setVisible(true);

    }

    /**
     * Registra un conjunto de pilotos, circuitos y vehículos de ejemplo
     * para poder probar la aplicación inmediatamente al iniciarla.
     */
    private static void cargarDatosDePrueba(PilotoServicio pilotoServicio, CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio) {

        // Pilotos de ejemplo con su nivel de experiencia y habilidad en lluvia.
        Piloto leonardo = new Piloto("Leonardo", 90, 90);

        Piloto maxVerstappen = new Piloto("Max Verstappen", 95, 88);

        Piloto charlesLeclerc = new Piloto("Charles Leclerc", 92, 85);

        pilotoServicio.registrar(leonardo.getNombre(), leonardo.getExperiencia(), leonardo.getHabilidadLluvia());

        pilotoServicio.registrar(maxVerstappen.getNombre(), maxVerstappen.getExperiencia(), maxVerstappen.getHabilidadLluvia());

        pilotoServicio.registrar(charlesLeclerc.getNombre(), charlesLeclerc.getExperiencia(), charlesLeclerc.getHabilidadLluvia());

        // Circuitos de ejemplo (nombre, kilómetros por vuelta, ubicación).
        circuitoServicio.registrar("Gran Premio Especial", 5.793, "Monza");

        circuitoServicio.registrar("Circuito de Mónaco", 3.337, "Mónaco");

        circuitoServicio.registrar("Silverstone", 5.891, "Reino Unido");

        // Vehículos de ejemplo asociados a cada piloto.
        vehiculoServicio.registrar("Williams", 320, 0.0, leonardo);

        vehiculoServicio.registrar("Red Bull", 330, 0.0, maxVerstappen);

        vehiculoServicio.registrar("Ferrari", 325, 0.0, charlesLeclerc);

    }

}