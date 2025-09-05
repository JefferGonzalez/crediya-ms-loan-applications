package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.constants.ApiConstants;
import co.com.pragma.crediya.api.constants.FilterParams;
import co.com.pragma.crediya.api.exceptions.InvalidQueryParamException;
import co.com.pragma.crediya.model.loan.report.LoanApplicationFilter;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class LoanApplicationRestFilterMapper {

    private LoanApplicationRestFilterMapper() {
    }

    public static LoanApplicationFilter fromServerRequest(ServerRequest request) {
        Set<String> statuses = getQueryParam(request, FilterParams.STATUSES)
                .map(s -> Set.of(s.split(ApiConstants.PARAM_DELIMITER)))
                .orElse(Set.of());

        BigDecimal minAmount = getBigDecimalParam(request, FilterParams.MIN_AMOUNT, a -> a.compareTo(BigDecimal.ZERO) >= 0);
        BigDecimal maxAmount = getBigDecimalParam(request, FilterParams.MAX_AMOUNT, a -> a.compareTo(BigDecimal.ZERO) >= 0);

        Integer minTerm = getIntParam(request, FilterParams.MIN_TERM, null, t -> t > 0);
        Integer maxTerm = getIntParam(request, FilterParams.MAX_TERM, null, t -> t > 0);

        String email = getQueryParam(request, FilterParams.EMAIL).orElse(null);
        String loanType = getQueryParam(request, FilterParams.LOAN_TYPE).orElse(null);

        int limit = getIntParam(request, FilterParams.LIMIT, ApiConstants.DEFAULT_LIMIT, l -> l > 0);
        int page = getIntParam(request, FilterParams.PAGE, ApiConstants.DEFAULT_PAGE, o -> o > 0);

        return new LoanApplicationFilter(statuses, minAmount, maxAmount, minTerm, maxTerm, email, loanType, limit, page);
    }

    private static Optional<String> getQueryParam(ServerRequest request, String key) {
        return request.queryParam(key).filter(s -> !s.isBlank());
    }

    private static Integer getIntParam(ServerRequest request, String paramName, Integer defaultValue, IntPredicate validator) {
        return getQueryParam(request, paramName)
                .map(s -> {
                    try {
                        int value = Integer.parseInt(s);
                        if (!validator.test(value)) {
                            throw new InvalidQueryParamException("Query param out of valid range: " + paramName);
                        }
                        return value;
                    } catch (NumberFormatException e) {
                        throw new InvalidQueryParamException("Invalid integer for query param: " + paramName);
                    }
                })
                .orElse(defaultValue);
    }

    private static BigDecimal getBigDecimalParam(ServerRequest request, String paramName, Predicate<BigDecimal> validator) {
        return getQueryParam(request, paramName)
                .map(s -> {
                    try {
                        return new BigDecimal(s);
                    } catch (NumberFormatException e) {
                        throw new InvalidQueryParamException("Invalid decimal for query param: " + paramName);
                    }
                })
                .filter(value -> {
                    if (!validator.test(value)) {
                        throw new InvalidQueryParamException("Query param out of valid range: " + paramName);
                    }
                    return true;
                })
                .orElse(null);
    }


}

