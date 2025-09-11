package co.com.pragma.crediya.model.notification;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record NotificationMessage(
        String to,
        String subject,
        String body) {

    private static final Map<String, String> ESCAPES = Map.of(
            "\"", "\\\"",
            "\n", "\\n",
            "\r", "\\r"
    );

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("[\"\\n\\r]");

    public String toJson() {
        return String.format("""
                {"to":"%s","subject":"%s","body":"%s"}
                """, to, subject, escapeJson(body));
    }

    private String escapeJson(String value) {
        Matcher m = ESCAPE_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            m.appendReplacement(sb, ESCAPES.get(m.group()));
        }

        m.appendTail(sb);

        return sb.toString();
    }

}
