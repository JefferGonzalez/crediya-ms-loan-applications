package co.com.pragma.crediya.model.notification.gateways;

import co.com.pragma.crediya.model.notification.NotificationMessage;

public interface NotificationPort {

    void sendNotification(NotificationMessage message);

}
