package com.proyectof1.infraestructura.adaptadores.salida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.proyectof1.aplicacion.puertos.salida.RankingRepositorio;
import com.proyectof1.dominio.EntradaRanking;

public class RankingRepositorioJDBC implements RankingRepositorio {

    private final ConexionJDBC conexion;

    public RankingRepositorioJDBC(ConexionJDBC conexion) {
        this.conexion = conexion;
    }

    @Override
    public void guardar(String jugador, int puntuacion, String dificultad) {

        String sql = "INSERT INTO ranking (jugador, puntuacion, dificultad) VALUES (?, ?, ?)";

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador);
            ps.setInt(2, puntuacion);
            ps.setString(3, dificultad);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar en ranking: " + e.getMessage());
        }
    }

    @Override
    public List<EntradaRanking> top5() {

        String sql = "SELECT jugador, puntuacion, dificultad, fecha FROM ranking "
                + "ORDER BY puntuacion DESC LIMIT 5";

        return ejecutarTop(sql);
    }

    @Override
    public List<EntradaRanking> top5PorDificultad(String dificultad) {

        String sql = "SELECT jugador, puntuacion, dificultad, fecha FROM ranking "
                + "WHERE dificultad = ? ORDER BY puntuacion DESC LIMIT 5";

        List<EntradaRanking> resultados = new ArrayList<>();

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dificultad);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearEntrada(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar ranking: " + e.getMessage());
        }

        return resultados;
    }

    private List<EntradaRanking> ejecutarTop(String sql) {

        List<EntradaRanking> resultados = new ArrayList<>();

        try (Connection conn = conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultados.add(mapearEntrada(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar ranking: " + e.getMessage());
        }

        return resultados;
    }

    private EntradaRanking mapearEntrada(ResultSet rs) throws SQLException {

        Timestamp fechaTs = rs.getTimestamp("fecha");
        LocalDateTime fecha = fechaTs != null ? fechaTs.toLocalDateTime() : LocalDateTime.now();

        return new EntradaRanking(
                rs.getString("jugador"),
                rs.getInt("puntuacion"),
                rs.getString("dificultad"),
                fecha);
    }
}
