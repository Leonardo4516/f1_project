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

public class Main {

    public static void main(String[] args) {

        ClimaHttpAdapter climaAdapter = new ClimaHttpAdapter();

        SimulacionService simulacionService = new SimulacionService(climaAdapter);

        CircuitoServicio circuitoServicio = new CircuitoServicioImpl(new CircuitosRepositorioEnMemoria());

        PilotoServicio pilotoServicio = new PilotoServicioImpl(new PilotosRepositorioEnMemoria());

        VehiculoServicio vehiculoServicio = new VehiculoServicioImpl(new VehiculosRepositorioEnMemoria());

        cargarDatosDePrueba(pilotoServicio, circuitoServicio, vehiculoServicio);

        VentanaPrincipal principal = new VentanaPrincipal(circuitoServicio, pilotoServicio, vehiculoServicio, simulacionService);

        principal.setVisible(true);

    }

    private static void cargarDatosDePrueba(PilotoServicio pilotoServicio, CircuitoServicio circuitoServicio, VehiculoServicio vehiculoServicio) {

        Piloto leonardo = new Piloto("Leonardo", 90, 90);

        Piloto maxVerstappen = new Piloto("Max Verstappen", 95, 88);

        Piloto charlesLeclerc = new Piloto("Charles Leclerc", 92, 85);

        pilotoServicio.registrar(leonardo.getNombre(), leonardo.getExperiencia(), leonardo.getHabilidadLluvia());

        pilotoServicio.registrar(maxVerstappen.getNombre(), maxVerstappen.getExperiencia(), maxVerstappen.getHabilidadLluvia());

        pilotoServicio.registrar(charlesLeclerc.getNombre(), charlesLeclerc.getExperiencia(), charlesLeclerc.getHabilidadLluvia());

        circuitoServicio.registrar("Gran Premio Especial", 5.793, "Monza");

        circuitoServicio.registrar("Circuito de Mónaco", 3.337, "Mónaco");

        circuitoServicio.registrar("Silverstone", 5.891, "Reino Unido");

        vehiculoServicio.registrar("Williams", 320, 0.0, leonardo);

        vehiculoServicio.registrar("Red Bull", 330, 0.0, maxVerstappen);

        vehiculoServicio.registrar("Ferrari", 325, 0.0, charlesLeclerc);

    }

}