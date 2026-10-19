package schultz.thomas.schub.connector.premadelab.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Le format de l'API Discord, relayé tel quel. Limites : https://discord.com/developers/docs/resources/message#embed-object-embed-limits
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Embed(@Size(max = 256) String title,
                    String url,
                    @Size(max = 4096) String description,
                    Integer color,
                    @Size(max = 25) List<@Valid Field> fields,
                    @Valid Footer footer) {

    public record Field(@NotBlank @Size(max = 256) String name,
                        @NotBlank @Size(max = 1024) String value,
                        boolean inline) {
    }

    public record Footer(@NotBlank @Size(max = 2048) String text) {
    }
}
