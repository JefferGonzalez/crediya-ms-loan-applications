package co.com.pragma.crediya.model.notification;

public record NotificationMessage(
        String to,
        String subject,
        String body) {
}
