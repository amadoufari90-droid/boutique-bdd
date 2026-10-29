package com.boutique.api.commande;

import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PanierService {

    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    public PanierService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String cle(Long clientId) {
        return "panier:" + clientId;
    }

    /** Contenu du panier, trié par id produit. */
    public Map<Long, Integer> lire(Long clientId) {
        Map<Long, Integer> contenu = new TreeMap<>();
        redis.opsForHash().entries(cle(clientId)).forEach((produit, quantite) ->
                contenu.put(Long.valueOf((String) produit), Integer.valueOf((String) quantite)));
        return contenu;
    }

    public void definir(Long clientId, Long produitId, int quantite) {
        if (quantite <= 0) {
            redis.opsForHash().delete(cle(clientId), produitId.toString());
        } else {
            redis.opsForHash().put(cle(clientId), produitId.toString(), String.valueOf(quantite));
            redis.expire(cle(clientId), TTL);
        }
    }

    public void vider(Long clientId) {
        redis.delete(cle(clientId));
    }
}