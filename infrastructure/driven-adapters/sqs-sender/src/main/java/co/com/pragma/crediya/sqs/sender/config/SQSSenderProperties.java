package co.com.pragma.crediya.sqs.sender.config;

public record SQSSenderProperties(
        String region,
        String queueUrl,
        String loanNotificationQueueName,
        String loanAutoValidationQueueName,
        String loanApprovedEventsQueueName) {
}