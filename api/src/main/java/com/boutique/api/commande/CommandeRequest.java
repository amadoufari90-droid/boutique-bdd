package com.boutique.api.commande;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CommandeRequest(
        @NotBlank
        @Pattern(regexp = "CARTE|MOBILE_MONEY|VIREMENT|ESPECES",
                 message = "doit valoir CARTE, MOBILE_MONEY, VIREMENT ou ESPECES")
        String methode) {
}