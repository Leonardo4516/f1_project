package com.proyectof1.infraestructura.adaptadores.salida;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexión a PostgreSQL. Lee la configuración de variables de entorno
 * y crea las tablas al iniciar. Proporciona conexiones a los repositorios JDBC.
 *
 * <p>Variables de entorno esperadas: PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD.
 * Si alguna no está definida, se usa un valor por defecto razonable.</p>
 */
public class ConexionJDBC {

    private final String url;
    private final String usuario;
    private final String password;

    /**
     * Crea la conexión leyendo variables de entorno y ejecuta el schema SQL
     * para crear las tablas si no existen.
     */
    public ConexionJDBC() {

        String host = System.getenv().getOrDefault("PGHOST", "localhost");
        String port = System.getenv().getOrDefault("PGPORT", "5432");
        String database = System.getenv().getOrDefault("PGDATABASE", "proyecto_f1");
        this.usuario = System.getenv().getOrDefault("PGUSER", "postgres");
        this.password = System.getenv().getOrDefault("PGPASSWORD", "postgres");

        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de PostgreSQL.", e);
        }

        crearTablas();
    }

    /**
     * Permite crear una conexión con parámetros explícitos (útil en pruebas).
     */
    public ConexionJDBC(String host, int port, String database, String usuario, String password) {

        this.usuario = usuario;
        this.password = password;
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de PostgreSQL.", e);
        }

        crearTablas();
    }

    /** Devuelve una nueva conexión a la base de datos. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    /** Ejecuta el script schema.sql para crear las tablas si no existen. */
    private void crearTablas() {

        String[] sql = {
            "CREATE TABLE IF NOT EXISTS circuitos ("
                + "nombre VARCHAR(100) PRIMARY KEY, "
                + "kilometros DOUBLE PRECISION NOT NULL, "
                + "ubicacion VARCHAR(200) NOT NULL, "
                + "num_curvas INT NOT NULL DEFAULT 0, "
                + "tipo_circuito VARCHAR(50) NOT NULL DEFAULT '', "
                + "vueltas_tipicas INT NOT NULL DEFAULT 0, "
                + "record_vuelta VARCHAR(200) NOT NULL DEFAULT '')",

            "CREATE TABLE IF NOT EXISTS pilotos ("
                + "nombre VARCHAR(100) PRIMARY KEY, "
                + "experiencia INT NOT NULL, "
                + "habilidad_lluvia INT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS vehiculos ("
                + "marca_escuderia VARCHAR(100) PRIMARY KEY, "
                + "velocidad_maxima INT NOT NULL, "
                + "aceleracion INT NOT NULL, "
                + "frenado INT NOT NULL, "
                + "agarre INT NOT NULL, "
                + "piloto_nombre VARCHAR(100) NOT NULL REFERENCES pilotos(nombre))",

            "CREATE TABLE IF NOT EXISTS ranking ("
                + "id SERIAL PRIMARY KEY, "
                + "jugador VARCHAR(100) NOT NULL, "
                + "puntuacion INT NOT NULL, "
                + "dificultad VARCHAR(30) NOT NULL, "
                + "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sentencia : sql) {
                stmt.executeUpdate(sentencia);
            }
        } catch (SQLException e) {
            System.out.println("No se pudieron crear las tablas: " + e.getMessage());
        }
    }
}
