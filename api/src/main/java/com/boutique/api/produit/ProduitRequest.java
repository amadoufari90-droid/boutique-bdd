package com.boutique.api.produit;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProduitRequest(
        @NotBlank @Size(max = 150) String nom,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal prix,
        @NotNull @Min(0) Integer stock,
        @NotNull Long categorieId) {
}