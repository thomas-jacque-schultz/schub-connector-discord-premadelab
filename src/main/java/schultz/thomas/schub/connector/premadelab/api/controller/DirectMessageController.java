package schultz.thomas.schub.connector.premadelab.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import schultz.thomas.schub.connector.premadelab.api.dto.DirectMessageRequest;
import schultz.thomas.schub.connector.premadelab.api.dto.DirectMessageResult;
import schultz.thomas.schub.connector.premadelab.business.services.DirectMessageService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Messages privés", description = "Envoyés par le bot de l'application Discord PremadeLab.")
public class DirectMessageController {

    private final DirectMessageService messages;

    @Operation(summary = "Envoyer un message privé",
            description = "Toujours 200 : le refus de Discord est dans `status`, jamais une erreur 500.")
    @PostMapping("/dm")
    public DirectMessageResult send(@Valid @RequestBody DirectMessageRequest request) {
        return messages.send(request.discordId(), request.content(), request.embeds());
    }
}
