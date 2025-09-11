package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.config.security.SecurityUtils;
import co.com.pragma.crediya.api.dto.CustomerApplicationsReport;
import co.com.pragma.crediya.api.dto.ReportMetadata;
import co.com.pragma.crediya.api.dto.SaveLoanApplicationRequest;
import co.com.pragma.crediya.api.dto.UpdateStatusRequest;
import co.com.pragma.crediya.api.exceptions.EmptyRequestBodyException;
import co.com.pragma.crediya.api.exceptions.InvalidPathVariableException;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestFilterMapper;
import co.com.pragma.crediya.api.mapper.LoanApplicationRestMapper;
import co.com.pragma.crediya.api.validator.ReactiveValidator;
import co.com.pragma.crediya.model.jwt.Jwt;
import co.com.pragma.crediya.model.loan.Application;
import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import co.com.pragma.crediya.usecase.loan.ApplicationUseCase;
import co.com.pragma.crediya.usecase.loan.report.ApplicationReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoanApplicationHandler {

    private final ApplicationUseCase applicationUseCase;

    private final ApplicationReportUseCase applicationReportUseCase;

    private final LoanApplicationRestMapper loanApplicationMapper;

    private final ReactiveValidator reactiveValidator;

    private final SecurityUtils securityUtils;

    public Mono<ServerResponse> getReport(ServerRequest request) {
        LoanApplicationFilter filter = LoanApplicationRestFilterMapper.fromServerRequest(request);

        return applicationReportUseCase.getLoanApplicationsReport(filter)
                .map(report -> {
                    long totalItems = report.totalItems();
                    long totalPages = Math.ceilDiv(totalItems, filter.limit());
                    int size = report.data().size();
                    int currentPage = size != 0 ? filter.page() : 0;

                    ReportMetadata metadata = new ReportMetadata(totalItems, totalPages, currentPage, size);
                    return CustomerApplicationsReport.builder()
                            .data(report.data())
                            .metadata(metadata)
                            .build();
                })
                .flatMap(report ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(report)
                );
    }

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(SaveLoanApplicationRequest.class)
                .switchIfEmpty(Mono.error(new EmptyRequestBodyException()))
                .flatMap(reactiveValidator::validate)
                .map(loanApplicationMapper::toDomain)
                .zipWith(securityUtils.getJwt())
                .flatMap(tuple -> {
                    Application application = tuple.getT1();
                    Jwt token = tuple.getT2();

                    return applicationUseCase.save(application, token);
                })
                .map(loanApplicationMapper::toResponse)
                .flatMap(storedLoanApplication ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(storedLoanApplication)
                );
    }

    public Mono<ServerResponse> updateStatus(ServerRequest request) {
        String id = request.pathVariable(ApplicationFieldNames.ID);
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception ex) {
            return Mono.error(new InvalidPathVariableException());
        }

        return request.bodyToMono(UpdateStatusRequest.class)
                .switchIfEmpty(Mono.error(new EmptyRequestBodyException()))
                .flatMap(reactiveValidator::validate)
                .flatMap(updateStatusRequest ->
                        applicationUseCase.processAndApproveOrReject(uuid, updateStatusRequest.getStatus())
                                .map(loanApplicationMapper::toResponse)
                                .flatMap(response ->
                                        ServerResponse.status(HttpStatus.OK)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(response)
                                )
                );
    }

}
