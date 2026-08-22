package com.proyectof1;

import com.proyectof1.aplicacion.puertos.entrada.CircuitoServicio;
import com.proyectof1.aplicacion.puertos.entrada.PilotoServicio;
import com.proyectof1.aplicacion.puertos.entrada.VehiculoServicio;
import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.aplicacion.puertos.salida.RankingRepositorio;
import com.proyectof1.aplicacion.servicios.CircuitoServicioImpl;
import com.proyectof1.aplicacion.servicios.PilotoServicioImpl;
import com.proyectof1.aplicacion.servicios.SimulacionService;
import com.proyectof1.aplicacion.servicios.VehiculoServicioImpl;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.infraestructura.adaptadores.entrada.VentanaPrincipal;
import com.proyectof1.infraestructura.adaptadores.entrada.TemaF1;
import com.proyectof1.infraestructura.adaptadores.salida.CircuitosRepositorioJDBC;
import com.proyectof1.infraestructura.adaptadores.salida.ClimaHttpAdapter;
import com.proyectof1.infraestructura.adaptadores.salida.ConexionJDBC;
import com.proyectof1.infraestructura.adaptadores.salida.PilotosRepositorioJDBC;
import com.proyectof1.infraestructura.adaptadores.salida.RankingRepositorioJDBC;
import com.proyectof1.infraestructura.adaptadores.salida.VehiculosRepositorioJDBC;

/**
 * Clase principal del programa. Ejecuta la composición de todo el sistema:
 *  - Crea la conexión a PostgreSQL y los repositorios JDBC.
 *  - Las inyecta en los servicios de aplicación (puertos de entrada).
 *  - Si es el primer arranque (sin datos), carga un campeonato de ejemplo.
 *  - Abre la ventana principal (adaptador de entrada en Swing).
 */
public class Main {

    public static void main(String[] args) {

        TemaF1.aplicarTema();

        // Conexión a PostgreSQL (lee PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD).
        ConexionJDBC conexion = new ConexionJDBC();

        // Repositorios JDBC.
        PilotosRepositorio pilotosRepositorio = new PilotosRepositorioJDBC(conexion);
        PilotoServicio pilotoServicio = new PilotoServicioImpl(pilotosRepositorio);

        CircuitoServicio circuitoServicio = new CircuitoServicioImpl(
                new CircuitosRepositorioJDBC(conexion));

        VehiculoServicio vehiculoServicio = new VehiculoServicioImpl(
                new VehiculosRepositorioJDBC(conexion, pilotosRepositorio));

        RankingRepositorio rankingRepositorio = new RankingRepositorioJDBC(conexion);

        SimulacionService simulacionService = new SimulacionService(new ClimaHttpAdapter());

        // Semilla de datos de prueba si la DB está vacía.
        if (pilotoServicio.listarPilotos().isEmpty()) {
            cargarDatosDePrueba(pilotoServicio, circuitoServicio, vehiculoServicio);
        }

        // Ventana principal con el ranking inyectado.
        VentanaPrincipal principal = new VentanaPrincipal(
                circuitoServicio, pilotoServicio, vehiculoServicio,
                simulacionService, rankingRepositorio);

        principal.setVisible(true);
    }

    private static void cargarDatosDePrueba(PilotoServicio pilotoServicio,
            CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio) {

        Piloto verstappen = registrarPiloto(pilotoServicio, "Max Verstappen", 97, 92);
        Piloto leclerc = registrarPiloto(pilotoServicio, "Charles Leclerc", 93, 92);
        Piloto norris = registrarPiloto(pilotoServicio, "Lando Norris", 91, 84);
        Piloto russell = registrarPiloto(pilotoServicio, "George Russell", 90, 80);
        Piloto alonso = registrarPiloto(pilotoServicio, "Fernando Alonso", 94, 90);
        Piloto albon = registrarPiloto(pilotoServicio, "Alexander Albon", 86, 78);
        Piloto gasly = registrarPiloto(pilotoServicio, "Pierre Gasly", 87, 76);
        Piloto bearman = registrarPiloto(pilotoServicio, "Oliver Bearman", 79, 72);
        Piloto tsunoda = registrarPiloto(pilotoServicio, "Yuki Tsunoda", 83, 70);
        Piloto hulkenberg = registrarPiloto(pilotoServicio, "Nico Hulkenberg", 88, 82);

        circuitoServicio.registrar("Gran Premio de Italia", 5.793, "Monza");
        circuitoServicio.registrar("Gran Premio de Mónaco", 3.337, "Mónaco");
        circuitoServicio.registrar("Gran Premio de Gran Bretaña", 5.891, "Silverstone");
        circuitoServicio.registrar("Gran Premio de Japón", 5.807, "Suzuka");
        circuitoServicio.registrar("Gran Premio de Brasil", 4.309, "Interlagos");
        circuitoServicio.registrar("Gran Premio de Bélgica", 7.004, "Spa");

        registrarVehiculo(vehiculoServicio, "Red Bull", 340, verstappen);
        registrarVehiculo(vehiculoServicio, "Ferrari", 338, leclerc);
        registrarVehiculo(vehiculoServicio, "McLaren", 339, norris);
        registrarVehiculo(vehiculoServicio, "Mercedes", 337, russell);
        registrarVehiculo(vehiculoServicio, "Aston Martin", 334, alonso);
        registrarVehiculo(vehiculoServicio, "Williams", 329, albon);
        registrarVehiculo(vehiculoServicio, "Alpine", 331, gasly);
        registrarVehiculo(vehiculoServicio, "Haas", 326, bearman);
        registrarVehiculo(vehiculoServicio, "Racing Bulls", 330, tsunoda);
        registrarVehiculo(vehiculoServicio, "Sauber", 325, hulkenberg);
    }

    private static Piloto registrarPiloto(PilotoServicio pilotoServicio,
            String nombre, int experiencia, int habilidadLluvia) {
        pilotoServicio.registrar(nombre, experiencia, habilidadLluvia);
        return pilotoServicio.buscarPorNombre(nombre);
    }

    private static void registrarVehiculo(VehiculoServicio vehiculoServicio,
            String escuderia, int velocidadMaxima, Piloto piloto) {
        vehiculoServicio.registrar(escuderia, velocidadMaxima, 0.0, piloto);
    }
}
