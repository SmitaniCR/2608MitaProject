package _Project.Mita.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + escaped + "\",\"fieldErrors\":null}");
    }
}
