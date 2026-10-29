package com.boutique.api.commande;

import java.math.BigDecimal;

public record CommandeResponse
(Long commandeId, String statut, BigDecimal total,
                               String referencePaiement, int nombreLignes) {
}