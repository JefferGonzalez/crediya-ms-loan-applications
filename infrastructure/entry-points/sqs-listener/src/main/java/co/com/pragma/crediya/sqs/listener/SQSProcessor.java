package co.com.pragma.crediya.sqs.listener;

import co.com.pragma.crediya.sqs.listener.dto.LoanValidationResponse;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ObjectMapper objectMapper;

    private final ApplicationUseCase applicationUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        try {
            LoanValidationResponse response = objectMapper.readValue(message.body(), LoanValidationResponse.class);

            return applicationUseCase.processApplicationStatusUpdate(response.validation());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
