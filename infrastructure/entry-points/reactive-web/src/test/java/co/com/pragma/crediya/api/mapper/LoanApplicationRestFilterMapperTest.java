package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.constants.ApiConstants;
import co.com.pragma.crediya.api.constants.FilterParams;
import co.com.pragma.crediya.api.exceptions.InvalidQueryParamException;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LoanApplicationRestFilterMapperTest {

    @Mock
    private ServerRequest request;

    @Test
    void fromServerRequest_shouldMapAllQueryParams() {
        when(request.queryParam(FilterParams.STATUSES)).thenReturn(Optional.of("APPROVED,PENDING"));

        when(request.queryParam(FilterParams.MIN_AMOUNT)).thenReturn(Optional.of("1000.50"));

        when(request.queryParam(FilterParams.MAX_AMOUNT)).thenReturn(Optional.of("5000.75"));

        when(request.queryParam(FilterParams.MIN_TERM)).thenReturn(Optional.of("6"));

        when(request.queryParam(FilterParams.MAX_TERM)).thenReturn(Optional.of("24"));

        when(request.queryParam(FilterParams.EMAIL)).thenReturn(Optional.of("johndoe@example.com"));

        when(request.queryParam(FilterParams.LOAN_TYPE)).thenReturn(Optional.of("MICROCREDIT"));

        when(request.queryParam(FilterParams.LIMIT)).thenReturn(Optional.of("10"));

        when(request.queryParam(FilterParams.PAGE)).thenReturn(Optional.of("2"));

        LoanApplicationFilter filter = LoanApplicationRestFilterMapper.fromServerRequest(request);

        assertThat(filter.statuses()).isEqualTo(Set.of("APPROVED", "PENDING"));
        assertThat(filter.minAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.50));
        assertThat(filter.maxAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.75));
        assertThat(filter.minTerm()).isEqualTo(6);
        assertThat(filter.maxTerm()).isEqualTo(24);
        assertThat(filter.email()).isEqualTo("johndoe@example.com");
        assertThat(filter.loanType()).isEqualTo("MICROCREDIT");
        assertThat(filter.limit()).isEqualTo(10);
        assertThat(filter.page()).isEqualTo(2);
    }

    @Test
    void fromServerRequest_shouldUseDefaultsWhenQueryParamsMissing() {
        when(request.queryParam(Mockito.anyString())).thenReturn(Optional.empty());

        LoanApplicationFilter filter = LoanApplicationRestFilterMapper.fromServerRequest(request);

        assertThat(filter.statuses()).isEmpty();
        assertThat(filter.minAmount()).isNull();
        assertThat(filter.maxAmount()).isNull();
        assertThat(filter.minTerm()).isNull();
        assertThat(filter.maxTerm()).isNull();
        assertThat(filter.email()).isNull();
        assertThat(filter.loanType()).isNull();
        assertThat(filter.limit()).isEqualTo(ApiConstants.DEFAULT_LIMIT);
        assertThat(filter.page()).isEqualTo(ApiConstants.DEFAULT_PAGE);
    }

    @Test
    void fromServerRequest_shouldThrowInvalidQueryParamException_forInvalidInteger() {
        when(request.queryParam(FilterParams.PAGE)).thenReturn(Optional.of("abc"));

        assertThatThrownBy(() -> LoanApplicationRestFilterMapper.fromServerRequest(request))
                .isInstanceOf(InvalidQueryParamException.class)
                .hasMessageContaining("Invalid integer for query param");
    }

    @Test
    void fromServerRequest_shouldThrowInvalidQueryParamException_forInvalidDecimal() {
        when(request.queryParam(FilterParams.MIN_AMOUNT)).thenReturn(Optional.of("xyz"));

        assertThatThrownBy(() -> LoanApplicationRestFilterMapper.fromServerRequest(request))
                .isInstanceOf(InvalidQueryParamException.class)
                .hasMessageContaining("Invalid decimal for query param");
    }

}
