package com.boutique.api.produit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.boutique.api.erreur.NotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitRepository repo;

    public ProduitController(ProduitRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Page<Produit> lister(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return repo.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Produit lire(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Produit " + id + " introuvable"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produit creer(@Valid @RequestBody ProduitRequest req) {
        return repo.save(appliquer(new Produit(), req));
    }

    @PutMapping("/{id}")
    public Produit modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest req) {
        Produit p = lire(id);
        return repo.save(appliquer(p, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Produit " + id + " introuvable");
        }
        repo.deleteById(id);
    }

    private Produit appliquer(Produit p, ProduitRequest req) {
        p.setNom(req.nom());
        p.setDescription(req.description());
        p.setPrix(req.prix());
        p.setStock(req.stock());
        p.setCategorieId(req.categorieId());
        return p;
    }
}