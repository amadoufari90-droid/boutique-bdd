package com.boutique.api.commande;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clients/{clientId}")
public class PanierController {

    private final CommandeService service;

    public PanierController(CommandeService service) {
        this.service = service;
    }

    @GetMapping("/panier")
    public Map<String, Object> voir(@PathVariable Long clientId) {
        return service.detailPanier(clientId);
    }

    @PutMapping("/panier/{produitId}")
    public Map<String, Object> definir(@PathVariable Long clientId, @PathVariable Long produitId,
                                       @RequestParam int quantite) {
        service.definirQuantite(clientId, produitId, quantite);
        return service.detailPanier(clientId);
    }

    @DeleteMapping("/panier")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vider(@PathVariable Long clientId) {
        service.viderPanier(clientId);
    }

    @PostMapping("/commandes")
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeResponse commander(@PathVariable Long clientId,
                                      @Valid @RequestBody CommandeRequest req) {
        return service.passerCommande(clientId, req.methode());
    }
}