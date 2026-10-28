-- Vue utilisée par l'API : chiffre d'affaires par catégorie
CREATE VIEW v_ventes_par_categorie AS
SELECT c.id AS categorie_id,
       c.nom AS categorie,
       SUM(lc.quantite) AS quantite_vendue,
       SUM(lc.quantite * lc.prix_unitaire) AS chiffre_affaires
FROM categorie c
JOIN produit p          ON p.categorie_id = c.id
JOIN ligne_commande lc  ON lc.produit_id = p.id
JOIN commande cmd       ON cmd.id = lc.commande_id
WHERE cmd.statut IN ('PAYEE','EXPEDIEE')
GROUP BY c.id, c.nom;

-- Logique serveur : décrémenter le stock à chaque ligne de commande.
-- Si le stock est insuffisant, le CHECK (stock >= 0) fait échouer l'insertion.
CREATE FUNCTION fn_decrementer_stock() RETURNS trigger AS $$
BEGIN
    UPDATE produit SET stock = stock - NEW.quantite WHERE id = NEW.produit_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrementer_stock
AFTER INSERT ON ligne_commande
FOR EACH ROW EXECUTE FUNCTION fn_decrementer_stock();