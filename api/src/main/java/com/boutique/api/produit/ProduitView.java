package com.boutique.api.produit;

import java.math.BigDecimal;

public record ProduitView(Long id, String nom, String description,
                          BigDecimal prix, Integer stock, Long categorieId) {

    public static ProduitView from(Produit p) {
        return new ProduitView(p.getId(), p.getNom(), p.getDescription(),
                p.getPrix(), p.getStock(), p.getCategorieId());
    }
}
