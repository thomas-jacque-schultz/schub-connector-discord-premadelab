package schultz.thomas.schub.connector.premadelab.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// content : 2 000 caractères au plus, la limite d'un message Discord.
public record DirectMessageRequest(@NotBlank String discordId, @NotBlank @Size(max = 2000) String content) {
}
