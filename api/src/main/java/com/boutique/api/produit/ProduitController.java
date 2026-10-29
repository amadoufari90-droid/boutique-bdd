package com.boutique.api.produit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.boutique.api.erreur.NotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitRepository repo;
    private final ProduitCache cache;

    public ProduitController(ProduitRepository repo, ProduitCache cache) {
        this.repo = repo;
        this.cache = cache;
    }

    @GetMapping
    public Page<Produit> lister(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return repo.findAll(pageable);
    }

    /** Endpoint croisé : classement dans Redis, détails dans PostgreSQL. */
    @GetMapping("/populaires")
    public List<Map<String, Object>> populaires(@RequestParam(defaultValue = "5") int n) {
        Map<Long, Long> top = cache.classement(Math.max(1, Math.min(n, 50)));
        Map<Long, Produit> produits = repo.findAllById(top.keySet()).stream()
                .collect(Collectors.toMap(Produit::getId, Function.identity()));
        List<Map<String, Object>> resultat = new ArrayList<>();
        top.forEach((id, vues) -> {
            Produit p = produits.get(id);
            if (p != null) {
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("id", id);
                ligne.put("nom", p.getNom());
                ligne.put("prix", p.getPrix());
                ligne.put("vues", vues);
                resultat.add(ligne);
            }
        });
        return resultat;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitView> lire(@PathVariable Long id) {
        ProduitView vue = cache.trouver(id);
        String origine = "HIT";
        if (vue == null) {
            vue = ProduitView.from(trouverEntite(id));
            cache.mettre(vue);
            origine = "MISS";
        }
        cache.compterVue(id);
        return ResponseEntity.ok().header("X-Cache", origine).body(vue);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produit creer(@Valid @RequestBody ProduitRequest req) {
        return repo.save(appliquer(new Produit(), req));
    }

    @PutMapping("/{id}")
    public Produit modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest req) {
        Produit p = repo.save(appliquer(trouverEntite(id), req));
        cache.invalider(id);
        return p;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Produit " + id + " introuvable");
        }
        repo.deleteById(id);
        cache.invalider(id);
        cache.retirerDuClassement(id);
    }

    private Produit trouverEntite(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Produit " + id + " introuvable"));
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