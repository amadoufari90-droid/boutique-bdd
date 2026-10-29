package com.boutique.api.produit;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class ProduitCache {

    private static final String CLASSEMENT = "classement:produits:vues";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public ProduitCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String cle(Long id) {
        return "produit:" + id;
    }

    /** Retourne le produit en cache, ou null s'il n'y est pas. */
    public ProduitView trouver(Long id) {
        Map<Object, Object> h = redis.opsForHash().entries(cle(id));
        if (h.isEmpty()) {
            return null;
        }
        return new ProduitView(id,
                (String) h.get("nom"),
                (String) h.get("description"),
                new BigDecimal((String) h.get("prix")),
                Integer.valueOf((String) h.get("stock")),
                Long.valueOf((String) h.get("categorieId")));
    }

    public void mettre(ProduitView v) {
        Map<String, String> h = new HashMap<>();
        h.put("nom", Objects.toString(v.nom(), ""));
        h.put("description", Objects.toString(v.description(), ""));
        h.put("prix", v.prix().toPlainString());
        h.put("stock", String.valueOf(v.stock()));
        h.put("categorieId", String.valueOf(v.categorieId()));
        redis.opsForHash().putAll(cle(v.id()), h);
        redis.expire(cle(v.id()), TTL);
    }

    /** Invalide le cache après une modification ou une suppression. */
    public void invalider(Long id) {
        redis.delete(cle(id));
    }

    public void retirerDuClassement(Long id) {
        redis.opsForZSet().remove(CLASSEMENT, id.toString());
    }

    public void compterVue(Long id) {
        redis.opsForZSet().incrementScore(CLASSEMENT, id.toString(), 1);
    }

    /** Les n produits les plus consultés : id -> nombre de vues. */
    public Map<Long, Long> classement(int n) {
        Set<ZSetOperations.TypedTuple<String>> set =
                redis.opsForZSet().reverseRangeWithScores(CLASSEMENT, 0, n - 1);
        Map<Long, Long> resultat = new LinkedHashMap<>();
        if (set != null) {
            for (ZSetOperations.TypedTuple<String> t : set) {
                resultat.put(Long.valueOf(t.getValue()), t.getScore().longValue());
            }
        }
        return resultat;
    }
}