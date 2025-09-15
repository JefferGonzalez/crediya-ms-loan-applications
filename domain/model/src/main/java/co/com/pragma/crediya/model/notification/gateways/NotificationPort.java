package co.com.pragma.crediya.model.notification.gateways;

import co.com.pragma.crediya.model.notification.NotificationMessage;
import reactor.core.publisher.Mono;

public interface NotificationPort {

    Mono<Void> sendNotification(NotificationMessage message);

}
