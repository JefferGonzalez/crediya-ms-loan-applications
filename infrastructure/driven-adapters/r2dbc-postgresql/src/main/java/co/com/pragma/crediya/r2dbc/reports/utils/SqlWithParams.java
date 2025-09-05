package co.com.pragma.crediya.r2dbc.reports.utils;

import java.util.Map;

public record SqlWithParams(String sql, Map<String, Object> params) {
}
