package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.loan.ApplicationRiskEvaluation;
import co.com.pragma.crediya.model.loan.ApprovedApplication;
import co.com.pragma.crediya.model.loan.gateways.LoanApprovedEventPort;
import co.com.pragma.crediya.model.loan.gateways.LoanValidationPort;
import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.model.notification.gateways.NotificationPort;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
import co.com.pragma.crediya.sqs.sender.exceptions.LoanApprovedEventSerializationException;
import co.com.pragma.crediya.sqs.sender.exceptions.LoanSerializationException;
import co.com.pragma.crediya.sqs.sender.exceptions.NotificationSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class SQSSender implements NotificationPort, LoanValidationPort, LoanApprovedEventPort {

    private final SQSSenderProperties properties;

    private final SqsAsyncClient client;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendNotification(NotificationMessage message) {
        log.info("Sending email notification to SQS for recipient: [{}] : {}", message.to(), message);

        String queueUrl = properties.queueUrl() + properties.loanNotificationQueueName();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .onErrorMap(JsonProcessingException.class, e -> new NotificationSerializationException())
                .flatMap(json -> send(json, queueUrl))
                .doOnSuccess(messageId -> log.info("Email notification for {} queued successfully with Message ID: {}", message.to(), messageId))
                .doOnError(error -> log.error("Failed to send email notification to SQS for recipient: {}. Error: {}", message.to(), error.getMessage()))
                .then();
    }

    @Override
    public Mono<Void> validateLoanAutomatically(ApplicationRiskEvaluation applicationRiskEvaluation) {
        log.info("Sending loan validation request to SQS for application [{}] : {}", applicationRiskEvaluation.id(), applicationRiskEvaluation);

        String queueUrl = properties.queueUrl() + properties.loanAutoValidationQueueName();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(applicationRiskEvaluation))
                .onErrorMap(JsonProcessingException.class, e -> new LoanSerializationException())
                .flatMap(json -> send(json, queueUrl))
                .doOnSuccess(messageId -> log.info("Loan validation for application [{}] queued successfully with Message ID: {}", applicationRiskEvaluation.id(), messageId))
                .doOnError(error -> log.error("Failed to send loan validation to SQS for application [{}]. Error: {}", applicationRiskEvaluation.id(), error.getMessage()))
                .then();
    }

    @Override
    public Mono<Void> sendLoanApprovedEvent(ApprovedApplication approvedApplication) {
        log.info("Sending loan approved event to SQS for application [{}] : {}", approvedApplication.id(), approvedApplication);

        String queueUrl = properties.queueUrl() + properties.loanApprovedEventsQueueName();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(approvedApplication))
                .onErrorMap(JsonProcessingException.class, e -> new LoanApprovedEventSerializationException())
                .flatMap(json -> send(json, queueUrl))
                .doOnSuccess(messageId -> log.info("Loan approved event for application [{}] queued successfully with Message ID: {}", approvedApplication.id(), messageId))
                .doOnError(error -> log.error("Failed to send loan approved event to SQS for application [{}]. Error: {}", approvedApplication.id(), error.getMessage()))
                .then();
    }

    private Mono<String> send(String message, String queueUrl) {
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }

}
