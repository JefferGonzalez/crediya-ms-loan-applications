package co.com.pragma.crediya.sqs.sender.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SQSSenderConfigTest {

    private static final String REGION = "us-east-1";
    private static final String QUEUE_URL = "http://queue-url";

    @Mock
    private MetricPublisher publisher;

    @Test
    void configSqs_shouldCreateClient() {
        SQSSenderProperties properties = new SQSSenderProperties(REGION, QUEUE_URL);
        SQSSenderConfig config = new SQSSenderConfig();

        SqsAsyncClient client = config.configSqs(properties, publisher);

        assertThat(client).isNotNull();
    }

}
