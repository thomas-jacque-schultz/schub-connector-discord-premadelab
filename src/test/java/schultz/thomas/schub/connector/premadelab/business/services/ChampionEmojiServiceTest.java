package schultz.thomas.schub.connector.premadelab.business.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import schultz.thomas.schub.connector.premadelab.api.dto.ChampionEmojiRequest.Champion;
import schultz.thomas.schub.connector.premadelab.config.DiscordProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ChampionEmojiServiceTest {

    private static final String BASE = "https://discord.test/api/v10";
    private static final String ICONE = "https://ddragon.leagueoflegends.com/cdn/15.1.1/img/champion/Gnar.png";

    private final RestClient.Builder discordBuilder = RestClient.builder().baseUrl(BASE);
    private final MockRestServiceServer discord = MockRestServiceServer.bindTo(discordBuilder).build();
    private final RestClient.Builder imagesBuilder = RestClient.builder();
    private final MockRestServiceServer images = MockRestServiceServer.bindTo(imagesBuilder).build();
    private final ChampionEmojiService service = new ChampionEmojiService(discordBuilder.build(),
            imagesBuilder.build(), new DiscordProperties("jeton", BASE, null, null));

    @Test
    @DisplayName("un emoji existant est repris, un manquant est créé depuis Data Dragon, puis gardé en mémoire")
    void ensure() {
        discord.expect(requestTo(BASE + "/applications/@me"))
                .andRespond(withSuccess("{\"id\":\"9\"}", MediaType.APPLICATION_JSON));
        discord.expect(requestTo(BASE + "/applications/9/emojis"))
                .andRespond(withSuccess("{\"items\":[{\"id\":\"1\",\"name\":\"Shen\"}]}", MediaType.APPLICATION_JSON));
        images.expect(requestTo(ICONE)).andRespond(withSuccess(new byte[]{1, 2, 3}, MediaType.IMAGE_PNG));
        discord.expect(requestTo(BASE + "/applications/9/emojis")).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.name").value("Gnar"))
                .andExpect(jsonPath("$.image").value("data:image/png;base64,AQID"))
                .andRespond(withSuccess("{\"id\":\"2\"}", MediaType.APPLICATION_JSON));

        assertThat(service.ensure(List.of(new Champion("Shen", ICONE), new Champion("Gnar", ICONE))))
                .containsEntry("Shen", "<:Shen:1>").containsEntry("Gnar", "<:Gnar:2>");
        assertThat(service.ensure(List.of(new Champion("Gnar", ICONE)))).containsEntry("Gnar", "<:Gnar:2>");
        discord.verify();
        images.verify();
    }

    @Test
    @DisplayName("une icône hors de Data Dragon n'est pas téléchargée")
    void horsDataDragon() {
        discord.expect(requestTo(BASE + "/applications/@me"))
                .andRespond(withSuccess("{\"id\":\"9\"}", MediaType.APPLICATION_JSON));
        discord.expect(requestTo(BASE + "/applications/9/emojis"))
                .andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));

        assertThat(service.ensure(List.of(new Champion("Gnar", "https://exemple.test/Gnar.png")))).isEmpty();
        images.verify();
    }
}
