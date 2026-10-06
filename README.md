# schub-connector-discord-premadelab

Le connecteur Discord de **PremadeLab** : il parle au nom de l'application Discord PremadeLab, distincte de celle de
Schub (Schub#100). Il n'a aucune commande et ne vit sur aucun serveur Discord : il envoie des messages privés.

## API

`POST /dm` (`X-Internal-Secret`) — `{ "discordId": "...", "content": "..." }` → `{ "status": "SENT" | "UNREACHABLE" | "DISABLED" | "FAILED", "detail": ... }`.

Toujours 200 : un refus de Discord (MP fermés, application bloquée, compte inconnu) répond `UNREACHABLE`, et le cœur
marque l'utilisateur injoignable. Sans jeton, `DISABLED`.

## Configuration

| Variable | Rôle |
|---|---|
| `DISCORD_PREMADELAB_TOKEN` | jeton du bot de l'application PremadeLab (secret Docker en prod) |
| `SCHUB_INTERNAL_SECRET` | secret partagé avec le cœur |

## Limite à connaître

Un bot ne peut écrire en premier qu'à un utilisateur avec qui il partage un serveur, ou qui a installé l'application
sur son compte. Les deux voies sont décrites dans Schub#100.
