CREATE DATABASE IF NOT EXISTS cooperative_reservation;
USE cooperative_reservation;

CREATE TABLE IF NOT EXISTS voiture (
    idvoit VARCHAR(20) PRIMARY KEY,
    design VARCHAR(120) NOT NULL,
    type ENUM('Simple', 'Premium', 'VIP') NOT NULL,
    nbrplace INT NOT NULL CHECK (nbrplace > 0),
    frais INT NOT NULL CHECK (frais >= 0)
);

CREATE TABLE IF NOT EXISTS client (
    idcli VARCHAR(30) PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    numtel VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS place (
    idvoit VARCHAR(20) NOT NULL,
    place INT NOT NULL,
    occupation ENUM('Oui', 'Non') DEFAULT 'Non',
    PRIMARY KEY (idvoit, place),
    CONSTRAINT fk_place_voiture FOREIGN KEY (idvoit) REFERENCES voiture (idvoit) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reserver (
    idreserv VARCHAR(30) PRIMARY KEY,
    idvoit VARCHAR(20) NOT NULL,
    idcli VARCHAR(30) NOT NULL,
    place INT NOT NULL,
    date_reserv DATETIME NOT NULL,
    date_voyage DATE NOT NULL,
    paiement ENUM('Sans avance', 'Avec avance', 'Tout payé') NOT NULL,
    montant_avance INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_reserver_voiture FOREIGN KEY (idvoit) REFERENCES voiture (idvoit),
    CONSTRAINT fk_reserver_client FOREIGN KEY (idcli) REFERENCES client (idcli)
);

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(60) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_users_username (username)
);
