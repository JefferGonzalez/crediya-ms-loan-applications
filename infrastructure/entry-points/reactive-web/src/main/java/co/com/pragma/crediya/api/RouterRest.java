package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.exceptions.ProblemDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/loan-applications",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "listenPOSTSaveLoanApplicationUserCase",
                    operation = @Operation(
                            operationId = "saveLoanApplication",
                            summary = "Create a new loan application",
                            description = "Registers a loan application in the system with the provided information.",
                            requestBody = @RequestBody(
                                    content = @Content(
                                            schema = @Schema(implementation = SaveLoanApplicationRequest.class)
                                    ),
                                    required = true,
                                    description = "Loan data required to create a new application"
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Loan application successfully created",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid request. The provided data is missing or has an invalid format.",
                                            content = @Content(schema = @Schema(implementation = ProblemDetails.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error. An unexpected error occurred while processing the request.",
                                            content = @Content(schema = @Schema(implementation = ProblemDetails.class))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(LoanApplicationHandler handler) {
        return RouterFunctions.route()
                .POST("/api/v1/loan-applications", handler::listenPOSTSaveLoanApplicationUserCase)
                .build();
    }

}
