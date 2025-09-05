package co.com.pragma.crediya.consumer.dto;

import java.util.List;

public record EmailsRequest(
        List<String> emails) {
}
