package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.constants.ApiConstants;
import co.com.pragma.crediya.api.constants.FilterParams;
import co.com.pragma.crediya.api.dto.CustomerApplicationsReport;
import co.com.pragma.crediya.api.dto.LoanApplicationResponse;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.exceptions.ProblemDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
                    path = ApiConstants.LOAN_APPLICATIONS_PATH,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "getReport",
                    operation = @Operation(
                            operationId = "getLoanApplicationsReport",
                            summary = "Get loan applications report",
                            description = "Retrieves a paginated report of loan applications based on filter criteria.",
                            parameters = {
                                    @Parameter(name = FilterParams.STATUSES, description = "Filter by loan statuses", in = ParameterIn.QUERY, example = "UNDER REVIEW,MANUAL REVIEW"),
                                    @Parameter(name = FilterParams.MIN_AMOUNT, description = "Minimum loan amount", in = ParameterIn.QUERY),
                                    @Parameter(name = FilterParams.MAX_AMOUNT, description = "Maximum loan amount", in = ParameterIn.QUERY),
                                    @Parameter(name = FilterParams.MIN_TERM, description = "Minimum loan term", in = ParameterIn.QUERY, example = "1-360"),
                                    @Parameter(name = FilterParams.MAX_TERM, description = "Maximum loan term", in = ParameterIn.QUERY),
                                    @Parameter(name = FilterParams.EMAIL, description = "Filter by user email", in = ParameterIn.QUERY),
                                    @Parameter(name = FilterParams.LOAN_TYPE, description = "Filter by loan type", in = ParameterIn.QUERY, example = "EDUCATION LOAN,MICROCREDIT"),
                                    @Parameter(name = FilterParams.LIMIT, description = "Number of items per page", in = ParameterIn.QUERY, example = "5"),
                                    @Parameter(name = FilterParams.PAGE, description = "Page number", in = ParameterIn.QUERY, example = "1")
                            },
                            security = @SecurityRequirement(name = "bearerAuth"),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Report retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = CustomerApplicationsReport.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid request. The provided filter is missing or invalid.",
                                            content = @Content(schema = @Schema(implementation = ProblemDetails.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error while fetching the report.",
                                            content = @Content(schema = @Schema(implementation = ProblemDetails.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = ApiConstants.LOAN_APPLICATIONS_PATH,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = LoanApplicationHandler.class,
                    beanMethod = "createLoanApplication",
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
                            security = @SecurityRequirement(name = "bearerAuth"),
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
                .GET(ApiConstants.LOAN_APPLICATIONS_PATH, handler::getReport)
                .POST(ApiConstants.LOAN_APPLICATIONS_PATH, handler::createLoanApplication)
                .build();
    }

}
