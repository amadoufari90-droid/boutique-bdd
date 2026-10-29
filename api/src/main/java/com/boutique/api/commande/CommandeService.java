package com.boutique.api.commande;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.boutique.api.erreur.NotFoundException;
import com.boutique.api.erreur.RequeteInvalideException;
import com.boutique.api.produit.ProduitCache;

@Service
public class CommandeService {

    private final JdbcTemplate jdbc;
    private final PanierService panier;
    private final ProduitCache cache;

    public CommandeService(JdbcTemplate jdbc, PanierService panier, ProduitCache cache) {
        this.jdbc = jdbc;
        this.panier = panier;
        this.cache = cache;
    }

    // ---------- Panier (Redis) enrichi avec PostgreSQL ----------

    public Map<String, Object> detailPanier(Long clientId) {
        verifierClient(clientId);
        List<Map<String, Object>> lignes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : panier.lire(clientId).entrySet()) {
            List<Map<String, Object>> r =
                    jdbc.queryForList("SELECT id, nom, prix FROM produit WHERE id = ?", e.getKey());
            if (r.isEmpty()) {
                continue;
            }
            BigDecimal prix = (BigDecimal) r.get(0).get("prix");
            BigDecimal sousTotal = prix.multiply(BigDecimal.valueOf(e.getValue()));
            total = total.add(sousTotal);
            Map<String, Object> ligne = new LinkedHashMap<>();
            ligne.put("produitId", e.getKey());
            ligne.put("nom", r.get(0).get("nom"));
            ligne.put("prix", prix);
            ligne.put("quantite", e.getValue());
            ligne.put("sousTotal", sousTotal);
            lignes.add(ligne);
        }
        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("clientId", clientId);
        resultat.put("lignes", lignes);
        resultat.put("total", total);
        return resultat;
    }

    public void definirQuantite(Long clientId, Long produitId, int quantite) {
        verifierClient(clientId);
        if (quantite < 0 || quantite > 100) {
            throw new RequeteInvalideException("La quantité doit être comprise entre 0 et 100");
        }
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM produit WHERE id = ?)", Boolean.class, produitId);
        if (!Boolean.TRUE.equals(existe)) {
            throw new NotFoundException("Produit " + produitId + " introuvable");
        }
        panier.definir(clientId, produitId, quantite);
    }

    public void viderPanier(Long clientId) {
        verifierClient(clientId);
        panier.vider(clientId);
    }

    // ---------- Validation : une seule transaction PostgreSQL ----------

    @Transactional
    public CommandeResponse passerCommande(Long clientId, String methode) {
        verifierClient(clientId);
        Map<Long, Integer> contenu = panier.lire(clientId);
        if (contenu.isEmpty()) {
            throw new RequeteInvalideException("Le panier est vide");
        }

        Long commandeId = jdbc.queryForObject(
                "INSERT INTO commande (client_id, statut) VALUES (?, 'EN_ATTENTE') RETURNING id",
                Long.class, clientId);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : contenu.entrySet()) {
            List<BigDecimal> prix = jdbc.queryForList(
                    "SELECT prix FROM produit WHERE id = ?", BigDecimal.class, e.getKey());
            if (prix.isEmpty()) {
                throw new NotFoundException("Produit " + e.getKey() + " introuvable");
            }
            // Le trigger décrémente le stock ; s'il devient négatif, le CHECK annule la transaction
            jdbc.update("INSERT INTO ligne_commande (commande_id, produit_id, quantite, prix_unitaire) "
                    + "VALUES (?, ?, ?, ?)", commandeId, e.getKey(), e.getValue(), prix.get(0));
            total = total.add(prix.get(0).multiply(BigDecimal.valueOf(e.getValue())));
        }

        String reference = "PAY-" + UUID.randomUUID();
        jdbc.update("INSERT INTO paiement (commande_id, montant, methode, statut, reference) "
                + "VALUES (?, ?, ?, 'VALIDE', ?)", commandeId, total, methode, reference);
        jdbc.update("UPDATE commande SET statut = 'PAYEE' WHERE id = ?", commandeId);

        // Redis n'est touché qu'après le commit : si PostgreSQL échoue, le panier reste intact
        List<Long> produits = new ArrayList<>(contenu.keySet());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                panier.vider(clientId);
                produits.forEach(cache::invalider); // le stock a changé : le cache est périmé
            }
        });

        return new CommandeResponse(commandeId, "PAYEE", total, reference, contenu.size());
    }

    private void verifierClient(Long clientId) {
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM client WHERE id = ?)", Boolean.class, clientId);
        if (!Boolean.TRUE.equals(existe)) {
            throw new NotFoundException("Client " + clientId + " introuvable");
        }
    }
}