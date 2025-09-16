package co.com.pragma.crediya.mustachetemplate;


import co.com.pragma.crediya.model.StatusChange;
import co.com.pragma.crediya.model.notification.LoanApproval;
import co.com.pragma.crediya.model.notification.gateways.NotificationRendererPort;
import co.com.pragma.crediya.mustachetemplate.constants.MustacheTemplates;
import co.com.pragma.crediya.mustachetemplate.formatters.CurrencyFormatter;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MustacheTemplateAdapter implements NotificationRendererPort {

    private final MustacheFactory mf = new DefaultMustacheFactory("templates");

    private final CurrencyFormatter currencyFormatter;

    public MustacheTemplateAdapter() {
        this.currencyFormatter = new CurrencyFormatter();
    }

    @Override
    public Mono<String> processLoanApprovalTemplate(LoanApproval template) {
        return Mono.fromCallable(() -> {
            Mustache mustache = this.mf.compile(MustacheTemplates.LOAN_PAYMENT_PLAN);

            Map<String, Object> context = new HashMap<>();
            context.put("loanType", template.loanType());
            context.put("amount", currencyFormatter.format(template.amount()));
            context.put("term", template.term());
            context.put("interestRate", template.interestRate());
            context.put("monthlyPayment", currencyFormatter.format(template.monthlyPayment()));

            List<Map<String, Object>> paymentDetail = template.paymentDetail().stream()
                    .map(detail -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("month", detail.month());
                        map.put("installment", currencyFormatter.format(detail.installment()));
                        map.put("principal", currencyFormatter.format(detail.principal()));
                        map.put("interest", currencyFormatter.format(detail.interest()));
                        map.put("balance", currencyFormatter.format(detail.balance()));
                        return map;
                    })
                    .toList();

            context.put("paymentDetail", paymentDetail);

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return writer.toString();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> processStatusChangeTemplate(StatusChange statusChange) {
        return Mono.fromCallable(() -> {
            Mustache mustache = this.mf.compile(MustacheTemplates.STATUS_CHANGE);

            Map<String, Object> context = new HashMap<>();
            context.put("status", statusChange.status());
            context.put("loanType", statusChange.loanType());
            context.put("amount", currencyFormatter.format(statusChange.amount()));

            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return writer.toString();
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
