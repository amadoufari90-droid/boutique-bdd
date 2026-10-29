-- Q1 : meilleurs clients (jointures internes, GROUP BY, HAVING)
SELECT c.id, c.nom,
       COUNT(DISTINCT cmd.id) AS nb_commandes,
       SUM(lc.quantite * lc.prix_unitaire) AS total_depense
FROM client c
JOIN commande cmd ON cmd.client_id = c.id
JOIN ligne_commande lc ON lc.commande_id = cmd.id
WHERE cmd.statut IN ('PAYEE', 'EXPEDIEE')
GROUP BY c.id, c.nom
HAVING SUM(lc.quantite * lc.prix_unitaire) > 3000
ORDER BY total_depense DESC
LIMIT 10;

-- Q2 : clients sans aucune commande (jointure externe LEFT JOIN)
SELECT c.id, c.nom, c.date_inscription
FROM client c
LEFT JOIN commande cmd ON cmd.client_id = c.id
WHERE cmd.id IS NULL
ORDER BY c.date_inscription
LIMIT 10;

-- Q3 : quantites vendues par categorie et par statut (agregats avec FILTER)
SELECT cat.nom AS categorie,
       SUM(lc.quantite) FILTER (WHERE cmd.statut = 'PAYEE')    AS payees,
       SUM(lc.quantite) FILTER (WHERE cmd.statut = 'EXPEDIEE') AS expediees,
       SUM(lc.quantite) FILTER (WHERE cmd.statut = 'ANNULEE')  AS annulees
FROM categorie cat
JOIN produit p ON p.categorie_id = cat.id
JOIN ligne_commande lc ON lc.produit_id = p.id
JOIN commande cmd ON cmd.id = lc.commande_id
GROUP BY cat.nom
ORDER BY cat.nom;

-- Q4 : chiffre d affaires mensuel et evolution (CTE WITH + fonction de fenetre LAG)
WITH ca_mensuel AS (
    SELECT date_trunc('month', cmd.date_commande) AS mois,
           SUM(lc.quantite * lc.prix_unitaire) AS ca
    FROM commande cmd
    JOIN ligne_commande lc ON lc.commande_id = cmd.id
    WHERE cmd.statut IN ('PAYEE', 'EXPEDIEE')
    GROUP BY 1
)
SELECT to_char(mois, 'YYYY-MM') AS mois,
       round(ca, 2) AS ca,
       round(100 * (ca - LAG(ca) OVER (ORDER BY mois)) / LAG(ca) OVER (ORDER BY mois), 1) AS evolution_pct
FROM ca_mensuel
ORDER BY mois;

-- Q5 : clients ayant eu une commande annulee ET une commande expediee (EXISTS)
SELECT c.id, c.nom
FROM client c
WHERE EXISTS (SELECT 1 FROM commande x WHERE x.client_id = c.id AND x.statut = 'ANNULEE')
  AND EXISTS (SELECT 1 FROM commande y WHERE y.client_id = c.id AND y.statut = 'EXPEDIEE')
ORDER BY c.id
LIMIT 10;

-- Q6 : produits plus chers que la moyenne de leur categorie (sous-requete correlee)
SELECT p.id, p.nom, cat.nom AS categorie, p.prix
FROM produit p
JOIN categorie cat ON cat.id = p.categorie_id
WHERE p.prix > (SELECT AVG(p2.prix) FROM produit p2 WHERE p2.categorie_id = p.categorie_id)
ORDER BY p.prix DESC
LIMIT 10;
