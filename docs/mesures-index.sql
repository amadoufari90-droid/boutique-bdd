-- SANS index : on le supprime, puis on mesure deux fois (la 2e mesure est a chaud)
DROP INDEX IF EXISTS idx_ligne_commande_produit;
ANALYZE ligne_commande;
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM ligne_commande WHERE produit_id = 42;
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM ligne_commande WHERE produit_id = 42;

-- AVEC index
CREATE INDEX idx_ligne_commande_produit ON ligne_commande (produit_id);
ANALYZE ligne_commande;
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM ligne_commande WHERE produit_id = 42;
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM ligne_commande WHERE produit_id = 42;
