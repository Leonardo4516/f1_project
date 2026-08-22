package com.proyectof1.infraestructura.adaptadores.salida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.proyectof1.aplicacion.puertos.salida.PilotosRepositorio;
import com.proyectof1.dominio.Piloto;

public class PilotosRepositorioJDBC implements PilotosRepositorio {

    private final ConexionJDBC conexion;

    public PilotosRepositorioJDBC(ConexionJDBC conexion) {
        this.conexion = conexion;
    }

    @Override
    public void guardar(Piloto piloto) {

        String sql = "INSERT INTO pilotos (nombre, experiencia, habilidad_lluvia) "
                + "VALUES (?, ?, ?) "
                + "ON CONFLICT (nombre) DO UPDATE SET experiencia = ?, habilidad_lluvia = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, piloto.getNombre());
            ps.setInt(2, piloto.getExperiencia());
            ps.setInt(3, piloto.getHabilidadLluvia());
            ps.setInt(4, piloto.getExperiencia());
            ps.setInt(5, piloto.getHabilidadLluvia());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar piloto: " + e.getMessage());
        }
    }

    @Override
    public List<Piloto> listarTodos() {

        List<Piloto> pilotos = new ArrayList<>();
        String sql = "SELECT nombre, experiencia, habilidad_lluvia FROM pilotos ORDER BY nombre";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pilotos.add(new Piloto(
                        rs.getString("nombre"),
                        rs.getInt("experiencia"),
                        rs.getInt("habilidad_lluvia")));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pilotos: " + e.getMessage());
        }

        return pilotos;
    }

    @Override
    public Piloto buscarPorNombre(String nombre) {

        String sql = "SELECT nombre, experiencia, habilidad_lluvia FROM pilotos WHERE nombre = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Piloto(
                            rs.getString("nombre"),
                            rs.getInt("experiencia"),
                            rs.getInt("habilidad_lluvia"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar piloto: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean eliminarPorNombre(String nombre) {

        String sql = "DELETE FROM pilotos WHERE nombre = ?";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar piloto: " + e.getMessage());
        }

        return false;
    }
}
