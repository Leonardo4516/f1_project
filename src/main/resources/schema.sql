-- Schema de la base de datos del proyecto F1 (Formulemon)
-- Se ejecuta al iniciar la aplicación para crear las tablas si no existen.

CREATE TABLE IF NOT EXISTS circuitos (
    nombre VARCHAR(100) PRIMARY KEY,
    kilometros DOUBLE PRECISION NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    num_curvas INT NOT NULL DEFAULT 0,
    tipo_circuito VARCHAR(50) NOT NULL DEFAULT '',
    vueltas_tipicas INT NOT NULL DEFAULT 0,
    record_vuelta VARCHAR(200) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS pilotos (
    nombre VARCHAR(100) PRIMARY KEY,
    experiencia INT NOT NULL,
    habilidad_lluvia INT NOT NULL
);

CREATE TABLE IF NOT EXISTS vehiculos (
    marca_escuderia VARCHAR(100) PRIMARY KEY,
    velocidad_maxima INT NOT NULL,
    aceleracion INT NOT NULL,
    frenado INT NOT NULL,
    agarre INT NOT NULL,
    piloto_nombre VARCHAR(100) NOT NULL REFERENCES pilotos(nombre)
);

-- Migración para bases de datos existentes (creadas con la versión anterior
-- que usaban la columna desgaste_neumaticos). Se elimina la columna y se
-- añaden los nuevos atributos físicos del vehículo.
ALTER TABLE vehiculos DROP COLUMN IF EXISTS desgaste_neumaticos;
ALTER TABLE vehiculos ADD COLUMN IF NOT EXISTS aceleracion INT NOT NULL DEFAULT 50;
ALTER TABLE vehiculos ADD COLUMN IF NOT EXISTS frenado INT NOT NULL DEFAULT 50;
ALTER TABLE vehiculos ADD COLUMN IF NOT EXISTS agarre INT NOT NULL DEFAULT 50;

-- Migración: agregar características técnicas a circuitos existentes.
ALTER TABLE circuitos ADD COLUMN IF NOT EXISTS num_curvas INT NOT NULL DEFAULT 0;
ALTER TABLE circuitos ADD COLUMN IF NOT EXISTS tipo_circuito VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE circuitos ADD COLUMN IF NOT EXISTS vueltas_tipicas INT NOT NULL DEFAULT 0;
ALTER TABLE circuitos ADD COLUMN IF NOT EXISTS record_vuelta VARCHAR(200) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS ranking (
    id SERIAL PRIMARY KEY,
    jugador VARCHAR(100) NOT NULL,
    puntuacion INT NOT NULL,
    dificultad VARCHAR(30) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
