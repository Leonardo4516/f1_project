-- Schema de la base de datos del proyecto F1 (Formulemon)
-- Se ejecuta al iniciar la aplicación para crear las tablas si no existen.

CREATE TABLE IF NOT EXISTS circuitos (
    nombre VARCHAR(100) PRIMARY KEY,
    kilometros DOUBLE PRECISION NOT NULL,
    ubicacion VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS pilotos (
    nombre VARCHAR(100) PRIMARY KEY,
    experiencia INT NOT NULL,
    habilidad_lluvia INT NOT NULL
);

CREATE TABLE IF NOT EXISTS vehiculos (
    marca_escuderia VARCHAR(100) PRIMARY KEY,
    velocidad_maxima INT NOT NULL,
    desgaste_neumaticos DOUBLE PRECISION NOT NULL,
    piloto_nombre VARCHAR(100) NOT NULL REFERENCES pilotos(nombre)
);

CREATE TABLE IF NOT EXISTS ranking (
    id SERIAL PRIMARY KEY,
    jugador VARCHAR(100) NOT NULL,
    puntuacion INT NOT NULL,
    dificultad VARCHAR(30) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
