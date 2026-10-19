package schultz.thomas.schub.connector.premadelab.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Limites d'un message Discord : 2 000 caractères de texte, 10 encadrés.
public record DirectMessageRequest(@NotBlank String discordId,
                                   @Size(max = 2000) String content,
                                   @Size(max = 10) List<@Valid Embed> embeds) {

    @AssertTrue(message = "content ou embeds requis")
    public boolean isNonVide() {
        return (content != null && !content.isBlank()) || (embeds != null && !embeds.isEmpty());
    }
}
