package co.com.pragma.crediya.api.constants;

import co.com.pragma.crediya.model.loan.constants.ApplicationFieldNames;

public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String API_V1 = "/api/v1";

    public static final String LOAN_APPLICATIONS_PATH = API_V1 + "/loan-applications";

    public static final String UPDATE_LOAN_STATUS_PATH = LOAN_APPLICATIONS_PATH + "/{" + ApplicationFieldNames.ID + "}/status";

    public static final String[] PUBLIC_PATTERNS = {
            "/actuator",
            "/actuator/health",
            "/actuator/prometheus",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    public static final String BEARER_PREFIX = "Bearer ";

    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    public static final String PARAM_DELIMITER = ",";

    public static final int DEFAULT_LIMIT = 5;

    public static final int DEFAULT_PAGE = 1;

}
