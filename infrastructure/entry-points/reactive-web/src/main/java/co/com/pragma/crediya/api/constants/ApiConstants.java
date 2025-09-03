package co.com.pragma.crediya.api.constants;

public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String API_V1 = "/api/v1";

    public static final String LOAN_APPLICATIONS_PATH = API_V1 + "/loan-applications";

    public static final String[] PUBLIC_PATTERNS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    public static final String[] PRIVATE_PATTERNS = {
            LOAN_APPLICATIONS_PATH + "/**"
    };

    public static final String BEARER_PREFIX = "Bearer ";

    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

}
