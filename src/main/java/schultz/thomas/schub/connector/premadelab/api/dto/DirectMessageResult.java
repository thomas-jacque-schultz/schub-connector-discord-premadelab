package schultz.thomas.schub.connector.premadelab.api.dto;

// UNREACHABLE : MP fermés, application bloquée ou compte inconnu — le cœur marque l'utilisateur injoignable.
// DISABLED : aucun jeton de bot configuré. FAILED : panne passagère, à réessayer plus tard.
public record DirectMessageResult(Status status, String detail) {

    public enum Status {
        SENT,
        UNREACHABLE,
        DISABLED,
        FAILED
    }
}
