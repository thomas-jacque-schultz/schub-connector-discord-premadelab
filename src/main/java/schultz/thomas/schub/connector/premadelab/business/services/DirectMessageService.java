package schultz.thomas.schub.connector.premadelab.business.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import schultz.thomas.schub.connector.premadelab.api.dto.DirectMessageResult;
import schultz.thomas.schub.connector.premadelab.api.dto.Embed;
import schultz.thomas.schub.connector.premadelab.config.DiscordProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

    // 50007 : MP fermés ou application bloquée ; 10013 : compte inconnu ; 50278 : relation requise (user install).
    static final Set<Integer> INJOIGNABLE = Set.of(50007, 10013, 50278);
    static final long ATTENTE_MAX_MS = 10_000;

    private final RestClient discordRestClient;
    private final DiscordProperties properties;
    private final ObjectMapper objectMapper;

    public DirectMessageResult send(String discordId, String content, List<Embed> embeds) {
        if (!properties.configured()) {
            return new DirectMessageResult(DirectMessageResult.Status.DISABLED, "Aucun jeton de bot configuré");
        }
        try {
            JsonNode salon = appel(() -> discordRestClient.post().uri("/users/@me/channels")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("recipient_id", discordId))
                    .retrieve().body(JsonNode.class));
            String salonId = salon == null ? null : salon.path("id").asText(null);
            if (salonId == null) {
                return new DirectMessageResult(DirectMessageResult.Status.FAILED, "Salon privé non ouvert");
            }
            appel(() -> discordRestClient.post().uri("/channels/{id}/messages", salonId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(message(content, embeds))
                    .retrieve().toBodilessEntity());
            return new DirectMessageResult(DirectMessageResult.Status.SENT, null);
        } catch (RestClientResponseException refus) {
            int code = codeDiscord(refus);
            if (INJOIGNABLE.contains(code)) {
                log.info("MP refusé par Discord pour {} (code {})", discordId, code);
                return new DirectMessageResult(DirectMessageResult.Status.UNREACHABLE, "Code Discord " + code);
            }
            log.warn("MP en échec pour {} : {} {}", discordId, refus.getStatusCode().value(), code);
            return new DirectMessageResult(DirectMessageResult.Status.FAILED,
                    "Discord a répondu " + refus.getStatusCode().value());
        } catch (RestClientException panne) {
            log.warn("Discord injoignable pour le MP de {} : {}", discordId, panne.getMessage());
            return new DirectMessageResult(DirectMessageResult.Status.FAILED, "Discord injoignable");
        }
    }

    // Un 429 dit combien attendre : on réessaie une fois, si l'attente reste raisonnable.
    private <T> T appel(java.util.function.Supplier<T> requete) {
        try {
            return requete.get();
        } catch (RestClientResponseException refus) {
            if (refus.getStatusCode().value() != 429) {
                throw refus;
            }
            long attente = (long) (lire(refus).path("retry_after").asDouble(1) * 1000);
            if (attente > ATTENTE_MAX_MS) {
                throw refus;
            }
            try {
                Thread.sleep(attente);
            } catch (InterruptedException interrompu) {
                Thread.currentThread().interrupt();
                throw refus;
            }
            return requete.get();
        }
    }

    private static Map<String, Object> message(String content, List<Embed> embeds) {
        Map<String, Object> message = new HashMap<>();
        if (content != null && !content.isBlank()) {
            message.put("content", content);
        }
        if (embeds != null && !embeds.isEmpty()) {
            message.put("embeds", embeds);
        }
        return message;
    }

    private int codeDiscord(RestClientResponseException refus) {
        return lire(refus).path("code").asInt(-1);
    }

    private JsonNode lire(RestClientResponseException refus) {
        try {
            return objectMapper.readTree(refus.getResponseBodyAsString());
        } catch (Exception illisible) {
            return objectMapper.createObjectNode();
        }
    }
}
