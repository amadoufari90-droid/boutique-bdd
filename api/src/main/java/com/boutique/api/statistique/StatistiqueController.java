package com.boutique.api.statistique;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiqueController {

    private final JdbcTemplate jdbc;

    public StatistiqueController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lit la vue v_ventes_par_categorie (chiffre d'affaires par catégorie). */
    @GetMapping("/categories")
    public List<Map<String, Object>> parCategorie() {
        return jdbc.queryForList(
                "SELECT categorie_id, categorie, quantite_vendue, chiffre_affaires "
                + "FROM v_ventes_par_categorie ORDER BY chiffre_affaires DESC");
    }
}