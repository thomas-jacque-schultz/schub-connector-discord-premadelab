package schultz.thomas.schub.connector.premadelab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Le bot de l'application Discord PremadeLab, distincte de celle de Schub (Schub#100).
@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(String token, String apiBaseUrl, Duration connectTimeout, Duration readTimeout) {

    public DiscordProperties {
        apiBaseUrl = apiBaseUrl == null || apiBaseUrl.isBlank() ? "https://discord.com/api/v10" : apiBaseUrl;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(10) : readTimeout;
    }

    public boolean configured() {
        return token != null && !token.isBlank();
    }
}
