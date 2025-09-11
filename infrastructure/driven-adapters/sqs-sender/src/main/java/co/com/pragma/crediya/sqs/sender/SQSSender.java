package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationPort {

    private final SQSSenderProperties properties;

    private final SqsAsyncClient client;

    @Override
    public void sendNotification(NotificationMessage message) {
        String json = message.toJson();

        log.info("Sending email notification to SQS for recipient: [{}] : {}", message.to(), json);

        send(json)
                .subscribe(
                        messageId -> log.info("Email notification for {} queued successfully with Message ID: {}", message.to(), messageId),
                        error -> log.error("Failed to send email notification to SQS for recipient: {}. Error: {}", message.to(), error.getMessage())
                );
    }

    private Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

}
