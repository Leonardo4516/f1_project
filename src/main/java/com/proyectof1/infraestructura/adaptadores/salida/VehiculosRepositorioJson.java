package com.proyectof1.infraestructura.adaptadores.salida;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

/**
 * Adaptador de salida (infraestructura) que implementa VehiculosRepositorio
 * con persistencia en un archivo JSON. Como un vehículo va asociado a un
 * piloto, en el archivo solo se guarda el nombre del piloto y al cargar se
 * reconstruye la referencia consultando el repositorio de pilotos.
 */
public class VehiculosRepositorioJson implements VehiculosRepositorio {

    // Ubicación por defecto de los datos.
    private static final Path ARCHIVO_DEFAULT = Path.of("data", "vehiculos.json");

    // Archivo concreto donde se persiste la información.
    private final Path archivo;

    // Repositorio de pilotos para resolver la referencia al cargar.
    private final PilotosRepositorio pilotosRepositorio;

    // Estructura en memoria: nombre de escudería como clave, Vehiculo como valor.
    private final Map<String, Vehiculo> vehiculos = new HashMap<>();

    /**
     * Constructor por defecto. Resuelve los pilotos usando la implementación
     * en memoria; útil cuando los pilotos provienen de otra fuente.
     */
    public VehiculosRepositorioJson(PilotosRepositorio pilotosRepositorio) {
        this(ARCHIVO_DEFAULT, pilotosRepositorio);
    }

    /**
     * Permite indicar un archivo distinto (útil en pruebas unitarias).
     * Al cargar, cada vehículo recupera a su piloto por nombre; si el piloto
     * no existe, el vehículo se omite para no dejar referencias rotas.
     */
    VehiculosRepositorioJson(Path archivo, PilotosRepositorio pilotosRepositorio) {

        if (pilotosRepositorio == null) {

            throw new IllegalArgumentException("El repositorio de pilotos no puede ser nulo.");

        }

        this.archivo = archivo;
        this.pilotosRepositorio = pilotosRepositorio;

        for (VehiculoDto dto : UtilJson.leer(archivo, VehiculoDto.class)) {

            Piloto piloto = pilotosRepositorio.buscarPorNombre(dto.pilotoNombre());

            if (piloto == null) {

                System.out.println("Se ignora el vehículo '" + dto.marcaEscuderia() + "' porque su piloto no existe.");
                continue;

            }

            vehiculos.put(dto.marcaEscuderia(), dto.aDominio(piloto));

        }
    }

    /** Inserta o actualiza un vehículo y guarda el cambio en disco. */
    @Override
    public void guardar(Vehiculo vehiculo) {

        vehiculos.put(vehiculo.getMarcaEscuderia(), vehiculo);
        persistir();

    }

    /** Devuelve una copia de la lista de todos los vehículos. */
    @Override
    public List<Vehiculo> listarTodos() {

        return new ArrayList<>(vehiculos.values());

    }

    /** Obtiene un vehículo por escudería o null si no existe. */
    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        return vehiculos.get(marcaEscuderia);

    }

    /** Elimina un vehículo por escudería. Devuelve true si existía y se eliminó. */
    @Override
    public boolean eliminarPorEscuderia(String marcaEscuderia) {

        boolean eliminado = vehiculos.remove(marcaEscuderia) != null;

        if (eliminado) {

            persistir();

        }

        return eliminado;
    }

    /** Vuelca el estado actual de la memoria al archivo JSON. */
    private void persistir() {

        List<VehiculoDto> dtos = new ArrayList<>();

        for (Vehiculo vehiculo : vehiculos.values()) {

            dtos.add(VehiculoDto.desdeDominio(vehiculo));

        }

        UtilJson.escribir(archivo, dtos);
    }

    /**
     * Representación plana de Vehiculo para guardar en JSON.
     * Solo se persiste el nombre del piloto; la referencia se reconstruye al cargar.
     */
    private record VehiculoDto(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, String pilotoNombre) {

        Vehiculo aDominio(Piloto piloto) {
            return new Vehiculo(marcaEscuderia, velocidadMaxima, desgasteNeumaticos, piloto);
        }

        static VehiculoDto desdeDominio(Vehiculo vehiculo) {
            return new VehiculoDto(
                    vehiculo.getMarcaEscuderia(),
                    vehiculo.getVelocidadMaxima(),
                    vehiculo.getDesgasteNeumaticos(),
                    vehiculo.getPiloto().getNombre());
        }
    }
}