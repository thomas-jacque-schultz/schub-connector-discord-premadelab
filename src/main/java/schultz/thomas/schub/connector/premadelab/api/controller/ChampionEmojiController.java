package schultz.thomas.schub.connector.premadelab.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.schub.connector.premadelab.api.dto.ChampionEmojiRequest;
import schultz.thomas.schub.connector.premadelab.business.services.ChampionEmojiService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Emojis", description = "Les icônes de champion, en emojis de l'application Discord PremadeLab.")
public class ChampionEmojiController {

    private final ChampionEmojiService emojis;

    @Operation(summary = "Obtenir les emojis de ces champions",
            description = "Crée ceux qui manquent. Un champion absent de la réponse s'écrit en toutes lettres.")
    @PostMapping("/emojis/champions")
    public Map<String, String> ensure(@Valid @RequestBody ChampionEmojiRequest request) {
        return emojis.ensure(request.champions());
    }
}
