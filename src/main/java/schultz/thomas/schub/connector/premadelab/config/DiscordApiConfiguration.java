package schultz.thomas.schub.connector.premadelab.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DiscordApiConfiguration {

    @Bean
    public RestClient discordRestClient(DiscordProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());
        return RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .baseUrl(properties.apiBaseUrl())
                .defaultHeader("Authorization", "Bot " + (properties.token() == null ? "" : properties.token()))
                .build();
    }
}
