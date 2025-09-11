package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.model.notification.NotificationMessage;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
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

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSSenderTest {

    @Mock
    private SQSSenderProperties properties;

    @Mock
    private SqsAsyncClient client;

    @InjectMocks
    private SQSSender sender;

    private static final String QUEUE_URL = "http://queue-url";

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
    void sendNotification_successful() {
        when(properties.queueUrl()).thenReturn(QUEUE_URL);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        sender.sendNotification(message);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest captured = captor.getValue();
        assertThat(captured.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(captured.messageBody()).contains(EMAIL_TO);
        assertThat(captured.messageBody()).contains(SUBJECT);
        assertThat(captured.messageBody()).contains(BODY);
    }

}