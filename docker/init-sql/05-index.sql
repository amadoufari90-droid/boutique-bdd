-- Index sur la cle etrangere produit_id : la cle primaire (commande_id, produit_id)
-- ne sert pas les recherches par produit seul.

CREATE INDEX IF NOT EXISTS idx_ligne_commande_produit ON ligne_commande (produit_id);
ANALYZE;