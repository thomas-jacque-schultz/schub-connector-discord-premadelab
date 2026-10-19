package schultz.thomas.schub.connector.premadelab.business.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import schultz.thomas.schub.connector.premadelab.api.dto.DirectMessageResult;
import schultz.thomas.schub.connector.premadelab.api.dto.Embed;

import java.util.List;
import schultz.thomas.schub.connector.premadelab.config.DiscordProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DirectMessageServiceTest {

    private static final String BASE = "https://discord.test/api/v10";

    private final RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
    private final MockRestServiceServer discord = MockRestServiceServer.bindTo(builder).build();
    private final DiscordProperties proprietes = new DiscordProperties("jeton", BASE, null, null);
    private final DirectMessageService service =
            new DirectMessageService(builder.build(), proprietes, new ObjectMapper());

    @Test
    @DisplayName("un MP part dans le salon privé ouvert pour le destinataire")
    void envoye() {
        discord.expect(requestTo(BASE + "/users/@me/channels"))
                .andRespond(withSuccess("{\"id\":\"42\"}", MediaType.APPLICATION_JSON));
        discord.expect(requestTo(BASE + "/channels/42/messages"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(service.send("123", "Bonjour", null).status()).isEqualTo(DirectMessageResult.Status.SENT);
        discord.verify();
    }

    @Test
    @DisplayName("des encadrés sans texte : seul embeds part, au format de Discord")
    void encadres() {
        Embed encadre = new Embed("Session", null, null, 0xC9A227,
                List.of(new Embed.Field("Partie 1", "Victoire", false)), new Embed.Footer("PremadeLab"));
        discord.expect(requestTo(BASE + "/users/@me/channels"))
                .andRespond(withSuccess("{\"id\":\"42\"}", MediaType.APPLICATION_JSON));
        discord.expect(requestTo(BASE + "/channels/42/messages"))
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(jsonPath("$.embeds[0].title").value("Session"))
                .andExpect(jsonPath("$.embeds[0].url").doesNotExist())
                .andExpect(jsonPath("$.embeds[0].fields[0].value").value("Victoire"))
                .andExpect(jsonPath("$.embeds[0].footer.text").value("PremadeLab"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(service.send("123", null, List.of(encadre)).status()).isEqualTo(DirectMessageResult.Status.SENT);
        discord.verify();
    }

    @Test
    @DisplayName("MP fermés (50007) : injoignable, sans erreur")
    void mpFermes() {
        discord.expect(requestTo(BASE + "/users/@me/channels"))
                .andRespond(withSuccess("{\"id\":\"42\"}", MediaType.APPLICATION_JSON));
        discord.expect(requestTo(BASE + "/channels/42/messages"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":50007,\"message\":\"Cannot send messages to this user\"}"));

        assertThat(service.send("123", "Bonjour", null).status()).isEqualTo(DirectMessageResult.Status.UNREACHABLE);
    }

    @Test
    @DisplayName("sans jeton, rien ne part")
    void desactive() {
        DirectMessageService sansJeton = new DirectMessageService(builder.build(),
                new DiscordProperties("", BASE, null, null), new ObjectMapper());

        assertThat(sansJeton.send("123", "Bonjour", null).status()).isEqualTo(DirectMessageResult.Status.DISABLED);
    }
}
