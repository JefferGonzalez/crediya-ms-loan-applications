package co.com.pragma.crediya.mustachetemplate.formatters;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private final NumberFormat formatter;

    public CurrencyFormatter(Locale locale) {
        this.formatter = NumberFormat.getCurrencyInstance(locale);
    }

    public CurrencyFormatter() {
        this(Locale.forLanguageTag("es-CO"));
    }

    public String format(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        synchronized (formatter) {
            return formatter.format(value);
        }
    }
}