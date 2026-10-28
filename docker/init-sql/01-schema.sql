CREATE TABLE client (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom              VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE CHECK (email LIKE '%@%'),
    date_inscription TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE categorie (
    id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE produit (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom          VARCHAR(150) NOT NULL,
    description  TEXT,
    prix         NUMERIC(10,2) NOT NULL CHECK (prix >= 0),
    stock        INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    categorie_id BIGINT NOT NULL REFERENCES categorie(id)
);

CREATE TABLE commande (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id     BIGINT NOT NULL REFERENCES client(id),
    date_commande TIMESTAMP NOT NULL DEFAULT now(),
    statut        VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (statut IN ('EN_ATTENTE','PAYEE','EXPEDIEE','ANNULEE'))
);

CREATE TABLE ligne_commande (
    commande_id   BIGINT NOT NULL REFERENCES commande(id) ON DELETE CASCADE,
    produit_id    BIGINT NOT NULL REFERENCES produit(id),
    quantite      INTEGER NOT NULL CHECK (quantite > 0),
    prix_unitaire NUMERIC(10,2) NOT NULL CHECK (prix_unitaire >= 0),
    PRIMARY KEY (commande_id, produit_id)
);

CREATE TABLE paiement (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    commande_id   BIGINT NOT NULL REFERENCES commande(id),
    montant       NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    methode       VARCHAR(20) NOT NULL
        CHECK (methode IN ('CARTE','MOBILE_MONEY','VIREMENT','ESPECES')),
    statut        VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (statut IN ('EN_ATTENTE','VALIDE','REFUSE','REMBOURSE')),
    date_paiement TIMESTAMP NOT NULL DEFAULT now(),
    reference     VARCHAR(60) NOT NULL UNIQUE
);