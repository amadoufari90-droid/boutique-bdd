-- Catégories
INSERT INTO categorie (nom) VALUES
 ('Électronique'),('Vêtements'),('Maison'),('Sport'),
 ('Livres'),('Beauté'),('Jouets'),('Alimentation');

-- 500 produits répartis sur les 8 catégories
INSERT INTO produit (nom, description, prix, stock, categorie_id)
SELECT 'Produit ' || i,
       'Description du produit ' || i,
       round((5 + random() * 495)::numeric, 2),
       1000 + floor(random() * 1000)::int,
       1 + (i % 8)
FROM generate_series(1, 500) AS i;

-- 2000 clients
INSERT INTO client (nom, email, date_inscription)
SELECT 'Client ' || i,
       'client' || i || '@example.com',
       now() - (random() * 730) * interval '1 day'
FROM generate_series(1, 2000) AS i;

-- 5000 commandes sur la dernière année
INSERT INTO commande (client_id, date_commande, statut)
SELECT 1 + floor(random() * 2000)::int,
       now() - (random() * 365) * interval '1 day',
       (ARRAY['EN_ATTENTE','PAYEE','PAYEE','EXPEDIEE','EXPEDIEE','EXPEDIEE','ANNULEE'])
           [1 + floor(random() * 7)::int]
FROM generate_series(1, 5000);

-- 1 à 4 lignes par commande (produits distincts dans une même commande)
INSERT INTO ligne_commande (commande_id, produit_id, quantite, prix_unitaire)
SELECT c.id, p.id, 1 + ((c.id + k) % 5), p.prix
FROM commande c
CROSS JOIN generate_series(1, 4) AS k
JOIN produit p ON p.id = ((c.id * 7 + k * 13) % 500) + 1
WHERE k <= 1 + (c.id % 4);

-- Un paiement par commande non « en attente » (refusé si la commande est annulée)
INSERT INTO paiement (commande_id, montant, methode, statut, date_paiement, reference)
SELECT c.id,
       t.total,
       (ARRAY['CARTE','MOBILE_MONEY','VIREMENT','ESPECES'])[1 + floor(random() * 4)::int],
       CASE WHEN c.statut = 'ANNULEE' THEN 'REFUSE' ELSE 'VALIDE' END,
       c.date_commande + interval '5 minutes',
       'PAY-' || c.id
FROM commande c
JOIN (SELECT commande_id, SUM(quantite * prix_unitaire) AS total
      FROM ligne_commande GROUP BY commande_id) t ON t.commande_id = c.id
WHERE c.statut <> 'EN_ATTENTE';

ANALYZE;