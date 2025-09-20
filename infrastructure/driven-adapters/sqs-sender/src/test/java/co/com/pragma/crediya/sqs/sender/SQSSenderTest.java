package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.ApplicationRiskEvaluation;
import co.com.pragma.crediya.model.loan.ApprovedApplication;
import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSSenderTest {

    @Mock
    private SQSSenderProperties properties;

    @Mock
    private SqsAsyncClient client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SQSSender sender;

    private static final String QUEUE_URL = "http://queue-url/";

    private static final String LOAN_NOTIFICATION_QUEUE_NAME = "LoanNotification";

    private static final String LOAN_AUTO_VALIDATION_QUEUE_NAME = "LoanAutoValidation";

    private static final String LOAN_APPROVED_EVENTS_QUEUE_NAME = "LoanApprovedEvents";

    private static final String EMAIL_TO = "jhondoe@example.com";

    private static final String SUBJECT = "Subject";

    private static final String BODY = "Body";

    private static final String MESSAGE_ID = "123";

    private NotificationMessage message;

    @BeforeEach
    void setUp() {
        message = new NotificationMessage(EMAIL_TO, SUBJECT, BODY);
    }

    @Test
    void sendNotificationSuccessful() throws JsonProcessingException {
        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(properties.loanNotificationQueueName()).thenReturn(LOAN_NOTIFICATION_QUEUE_NAME);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        when(objectMapper.writeValueAsString(message)).thenReturn("{\"to\":\"" + EMAIL_TO + "\",\"subject\":\"" + SUBJECT + "\",\"body\":\"" + BODY + "\"}");

        sender.sendNotification(message).block();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest captured = captor.getValue();
        assertThat(captured.queueUrl()).isEqualTo(QUEUE_URL + LOAN_NOTIFICATION_QUEUE_NAME);
        assertThat(captured.messageBody()).contains(EMAIL_TO);
        assertThat(captured.messageBody()).contains(SUBJECT);
        assertThat(captured.messageBody()).contains(BODY);
    }

    @Test
    void validateLoanAutomaticallySuccessful() throws JsonProcessingException {
        UUID applicationId = UUID.randomUUID();
        ApplicationRiskEvaluation evaluation = new ApplicationRiskEvaluation(
                applicationId,
                DomainConstants.MICROCREDIT,
                BigDecimal.valueOf(5000000),
                12,
                BigDecimal.valueOf(2.08),
                BigDecimal.valueOf(2000000),
                List.of()
        );

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(properties.loanAutoValidationQueueName()).thenReturn(LOAN_AUTO_VALIDATION_QUEUE_NAME);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        String expectedJson = "{\"id\":\"" + applicationId + "\",\"loanType\":\"" + DomainConstants.MICROCREDIT + "\"}";
        when(objectMapper.writeValueAsString(evaluation)).thenReturn(expectedJson);

        sender.validateLoanAutomatically(evaluation).block();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest captured = captor.getValue();
        assertThat(captured.queueUrl()).isEqualTo(QUEUE_URL + LOAN_AUTO_VALIDATION_QUEUE_NAME);
        assertThat(captured.messageBody()).isEqualTo(expectedJson);
    }

    @Test
    void sendLoanApprovedEventSuccessful() throws JsonProcessingException {
        ApprovedApplication approvedApplication = new ApprovedApplication(UUID.randomUUID(), BigDecimal.valueOf(1500), OffsetDateTime.now());

        when(properties.queueUrl()).thenReturn(QUEUE_URL);
        when(properties.loanApprovedEventsQueueName()).thenReturn(LOAN_APPROVED_EVENTS_QUEUE_NAME);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        String expectedJson = "{\"id\":\"" + approvedApplication.id() + ",\"amount\":" + approvedApplication.amount()
                + ",\"approvedAt\":\"" + approvedApplication.approvedAt().toString() + "\"}";

        when(objectMapper.writeValueAsString(approvedApplication)).thenReturn(expectedJson);

        sender.sendLoanApprovedEvent(approvedApplication).block();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest captured = captor.getValue();
        assertThat(captured.queueUrl()).isEqualTo(QUEUE_URL + LOAN_APPROVED_EVENTS_QUEUE_NAME);
        assertThat(captured.messageBody()).isEqualTo(expectedJson);
    }

}