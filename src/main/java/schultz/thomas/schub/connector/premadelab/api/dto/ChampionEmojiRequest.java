package schultz.thomas.schub.connector.premadelab.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChampionEmojiRequest(@Size(max = 50) List<@Valid Champion> champions) {

    // key : la clé Data Dragon du champion (« MonkeyKing »), qui sert de nom d'emoji.
    public record Champion(@NotBlank @Pattern(regexp = "[A-Za-z0-9_]{2,32}") String key, @NotBlank String iconUrl) {
    }
}
