package com.proyectof1.infraestructura.adaptadores.salida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.aplicacion.puertos.salida.VehiculosRepositorio;
import com.proyectof1.dominio.Piloto;
import com.proyectof1.dominio.Vehiculo;

public class VehiculosRepositorioJDBC implements VehiculosRepositorio {

    private final ConexionJDBC conexion;
    private final PilotosRepositorio pilotosRepositorio;

    public VehiculosRepositorioJDBC(ConexionJDBC conexion, PilotosRepositorio pilotosRepositorio) {
        this.conexion = conexion;
        this.pilotosRepositorio = Objects.requireNonNull(pilotosRepositorio,
                "El repositorio de pilotos no puede ser nulo.");
    }

    @Override
    public void guardar(Vehiculo vehiculo) {

        String sql = "INSERT INTO vehiculos (marca_escuderia, velocidad_maxima, desgaste_neumaticos, piloto_nombre) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (marca_escuderia) DO UPDATE SET "
                + "velocidad_maxima = ?, desgaste_neumaticos = ?, piloto_nombre = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehiculo.getMarcaEscuderia());
            ps.setInt(2, vehiculo.getVelocidadMaxima());
            ps.setDouble(3, vehiculo.getDesgasteNeumaticos());
            ps.setString(4, vehiculo.getPiloto().getNombre());
            ps.setInt(5, vehiculo.getVelocidadMaxima());
            ps.setDouble(6, vehiculo.getDesgasteNeumaticos());
            ps.setString(7, vehiculo.getPiloto().getNombre());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar vehículo: " + e.getMessage());
        }
    }

    @Override
    public List<Vehiculo> listarTodos() {

        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT v.marca_escuderia, v.velocidad_maxima, v.desgaste_neumaticos, "
                + "v.piloto_nombre, p.experiencia, p.habilidad_lluvia "
                + "FROM vehiculos v JOIN pilotos p ON v.piloto_nombre = p.nombre "
                + "ORDER BY v.marca_escuderia";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Piloto piloto = new Piloto(
                        rs.getString("piloto_nombre"),
                        rs.getInt("experiencia"),
                        rs.getInt("habilidad_lluvia"));

                vehiculos.add(new Vehiculo(
                        rs.getString("marca_escuderia"),
                        rs.getInt("velocidad_maxima"),
                        rs.getDouble("desgaste_neumaticos"),
                        piloto));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar vehículos: " + e.getMessage());
        }

        return vehiculos;
    }

    @Override
    public Vehiculo buscarPorEscuderia(String marcaEscuderia) {

        String sql = "SELECT v.marca_escuderia, v.velocidad_maxima, v.desgaste_neumaticos, "
                + "v.piloto_nombre, p.experiencia, p.habilidad_lluvia "
                + "FROM vehiculos v JOIN pilotos p ON v.piloto_nombre = p.nombre "
                + "WHERE v.marca_escuderia = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, marcaEscuderia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Piloto piloto = new Piloto(
                            rs.getString("piloto_nombre"),
                            rs.getInt("experiencia"),
                            rs.getInt("habilidad_lluvia"));

                    return new Vehiculo(
                            rs.getString("marca_escuderia"),
                            rs.getInt("velocidad_maxima"),
                            rs.getDouble("desgaste_neumaticos"),
                            piloto);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar vehículo: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean eliminarPorEscuderia(String marcaEscuderia) {

        String sql = "DELETE FROM vehiculos WHERE marca_escuderia = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, marcaEscuderia);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar vehículo: " + e.getMessage());
        }

        return false;
    }
}
