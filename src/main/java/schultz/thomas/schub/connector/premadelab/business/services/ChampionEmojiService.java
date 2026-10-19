package schultz.thomas.schub.connector.premadelab.business.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import schultz.thomas.schub.connector.premadelab.api.dto.ChampionEmojiRequest;
import schultz.thomas.schub.connector.premadelab.config.DiscordProperties;

import java.net.URI;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Les icônes de champion, en emojis de l'application : le bot peut s'en servir dans un MP, hors de tout serveur.
@Slf4j
@Service
public class ChampionEmojiService {

    static final String DATA_DRAGON = "ddragon.leagueoflegends.com";

    private final RestClient discord;
    private final RestClient images;
    private final DiscordProperties properties;
    private final Map<String, String> parNom = new ConcurrentHashMap<>();
    private volatile String applicationId;

    @Autowired
    public ChampionEmojiService(RestClient discordRestClient, DiscordProperties properties) {
        this(discordRestClient, RestClient.create(), properties);
    }

    ChampionEmojiService(RestClient discordRestClient, RestClient images, DiscordProperties properties) {
        this.discord = discordRestClient;
        this.images = images;
        this.properties = properties;
    }

    // La forme <:Gnar:123> de chaque champion obtenu ; un champion manquant est simplement absent.
    public synchronized Map<String, String> ensure(List<ChampionEmojiRequest.Champion> champions) {
        Map<String, String> resultat = new LinkedHashMap<>();
        if (!properties.configured() || champions == null || champions.isEmpty()) {
            return resultat;
        }
        try {
            if (applicationId == null) {
                applicationId = discord.get().uri("/applications/@me").retrieve().body(JsonNode.class)
                        .path("id").asText();
                JsonNode existants = discord.get().uri("/applications/{id}/emojis", applicationId)
                        .retrieve().body(JsonNode.class);
                existants.path("items").forEach(e -> parNom.put(e.path("name").asText(), e.path("id").asText()));
            }
            for (ChampionEmojiRequest.Champion champion : champions) {
                String id = parNom.get(champion.key());
                if (id == null) {
                    try {
                        id = cree(champion);
                    } catch (RestClientException refus) {
                        log.warn("Emoji de {} non créé : {}", champion.key(), refus.getMessage());
                    }
                }
                if (id != null) {
                    resultat.put(champion.key(), "<:" + champion.key() + ":" + id + ">");
                }
            }
        } catch (RestClientException panne) {
            log.warn("Emojis de champion indisponibles : {}", panne.getMessage());
        }
        return resultat;
    }

    private String cree(ChampionEmojiRequest.Champion champion) {
        URI source = URI.create(champion.iconUrl());
        if (!"https".equals(source.getScheme()) || !DATA_DRAGON.equals(source.getHost())) {
            log.warn("Icône refusée pour {} : {}", champion.key(), champion.iconUrl());
            return null;
        }
        byte[] png = images.get().uri(source).retrieve().body(byte[].class);
        if (png == null || png.length == 0) {
            return null;
        }
        JsonNode cree = discord.post().uri("/applications/{id}/emojis", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", champion.key(),
                        "image", "data:image/png;base64," + Base64.getEncoder().encodeToString(png)))
                .retrieve().body(JsonNode.class);
        String id = cree == null ? null : cree.path("id").asText(null);
        if (id != null) {
            parNom.put(champion.key(), id);
        }
        return id;
    }
}
