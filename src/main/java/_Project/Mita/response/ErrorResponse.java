package _Project.Mita.response;

import java.util.Map;

public record ErrorResponse(String message, Map<String, String> fieldErrors) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
