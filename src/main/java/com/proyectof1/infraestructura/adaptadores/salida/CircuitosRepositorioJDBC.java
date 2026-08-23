package com.proyectof1.infraestructura.adaptadores.salida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.proyectof1.aplicacion.puertos.salida.CircuitosRepositorio;
import com.proyectof1.dominio.Circuito;

/**
 * Adaptador de salida que implementa CircuitosRepositorio con persistencia
 * en PostgreSQL. Cada operación abre y cierra su propia conexión.
 */
public class CircuitosRepositorioJDBC implements CircuitosRepositorio {

    private final ConexionJDBC conexion;

    public CircuitosRepositorioJDBC(ConexionJDBC conexion) {
        this.conexion = conexion;
    }

    @Override
    public void guardar(Circuito circuito) {

        String sql = "INSERT INTO circuitos (nombre, kilometros, ubicacion, num_curvas, tipo_circuito, vueltas_tipicas, record_vuelta) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (nombre) DO UPDATE SET kilometros = ?, ubicacion = ?, "
                + "num_curvas = ?, tipo_circuito = ?, vueltas_tipicas = ?, record_vuelta = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, circuito.getNombre());
            ps.setDouble(2, circuito.getKilometros());
            ps.setString(3, circuito.getUbicacion());
            ps.setInt(4, circuito.getNumCurvas());
            ps.setString(5, circuito.getTipoCircuito());
            ps.setInt(6, circuito.getVueltasTipicas());
            ps.setString(7, circuito.getRecordVuelta());
            ps.setDouble(8, circuito.getKilometros());
            ps.setString(9, circuito.getUbicacion());
            ps.setInt(10, circuito.getNumCurvas());
            ps.setString(11, circuito.getTipoCircuito());
            ps.setInt(12, circuito.getVueltasTipicas());
            ps.setString(13, circuito.getRecordVuelta());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar circuito: " + e.getMessage());
        }
    }

    @Override
    public List<Circuito> listarTodos() {

        List<Circuito> circuitos = new ArrayList<>();
        String sql = "SELECT nombre, kilometros, ubicacion, num_curvas, tipo_circuito, vueltas_tipicas, record_vuelta "
                + "FROM circuitos ORDER BY nombre";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                circuitos.add(new Circuito(
                        rs.getString("nombre"),
                        rs.getDouble("kilometros"),
                        rs.getString("ubicacion"),
                        rs.getInt("num_curvas"),
                        rs.getString("tipo_circuito"),
                        rs.getInt("vueltas_tipicas"),
                        rs.getString("record_vuelta")));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar circuitos: " + e.getMessage());
        }

        return circuitos;
    }

    @Override
    public Circuito buscarPorNombre(String nombre) {

        String sql = "SELECT nombre, kilometros, ubicacion, num_curvas, tipo_circuito, vueltas_tipicas, record_vuelta "
                + "FROM circuitos WHERE nombre = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Circuito(
                            rs.getString("nombre"),
                            rs.getDouble("kilometros"),
                            rs.getString("ubicacion"),
                            rs.getInt("num_curvas"),
                            rs.getString("tipo_circuito"),
                            rs.getInt("vueltas_tipicas"),
                            rs.getString("record_vuelta"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar circuito: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Circuito> buscarPorUbicacion(String ubicacion) {

        List<Circuito> circuitos = new ArrayList<>();
        String sql = "SELECT nombre, kilometros, ubicacion, num_curvas, tipo_circuito, vueltas_tipicas, record_vuelta "
                + "FROM circuitos WHERE UPPER(ubicacion) = UPPER(?) ORDER BY nombre";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ubicacion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    circuitos.add(new Circuito(
                            rs.getString("nombre"),
                            rs.getDouble("kilometros"),
                            rs.getString("ubicacion"),
                            rs.getInt("num_curvas"),
                            rs.getString("tipo_circuito"),
                            rs.getInt("vueltas_tipicas"),
                            rs.getString("record_vuelta")));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar circuitos por ubicación: " + e.getMessage());
        }

        return circuitos;
    }

    @Override
    public boolean eliminarPorNombre(String nombre) {

        String sql = "DELETE FROM circuitos WHERE nombre = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar circuito: " + e.getMessage());
        }

        return false;
    }
}
