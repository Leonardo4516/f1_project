package com.proyectof1.aplicacion.puertos.entrada;

import java.util.List;

import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

/**
 * Puerto de entrada para la gestión de vehículos.
 * Define las operaciones que la interfaz de usuario puede invocar.
 * La implementación concreta la proporciona VehiculoServicioImpl.
 */
public interface VehiculoServicio {

    /** Registra un nuevo vehículo asociado a un piloto. */
    void registrar(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, Piloto piloto);

    /** Devuelve la lista de todos los vehículos registrados. */
    List<Vehiculo> listarVehiculos();

    /** Busca un vehículo por el nombre de su escudería. Devuelve null si no existe. */
    Vehiculo buscarPorEscuderia(String marcaEscuderia);

    /** Elimina un vehículo por escudería. Devuelve true si se eliminó. */
    boolean eliminar(String marcaEscuderia);

}