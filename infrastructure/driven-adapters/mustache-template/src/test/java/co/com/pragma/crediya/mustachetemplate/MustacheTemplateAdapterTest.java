package co.com.pragma.crediya.mustachetemplate;

import co.com.pragma.crediya.model.StatusChange;
import co.com.pragma.crediya.model.common.constants.DomainConstants;
import co.com.pragma.crediya.model.loan.PaymentDetail;
import co.com.pragma.crediya.model.notification.LoanApproval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MustacheTemplateAdapterTest {

    private MustacheTemplateAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MustacheTemplateAdapter();
    }

    @Test
    void processStatusChangeTemplateSuccessfully() {
        StatusChange statusChange = new StatusChange(DomainConstants.APPROVED_STATUS, DomainConstants.MICROCREDIT, BigDecimal.valueOf(5000000));

        StepVerifier.create(adapter.processStatusChangeTemplate(statusChange))
                .assertNext(html -> {
                    assertThat(html).isNotNull();
                    assertThat(html).isNotEmpty();
                    assertThat(html).contains(DomainConstants.APPROVED_STATUS);
                    assertThat(html).contains(DomainConstants.MICROCREDIT);
                })
                .verifyComplete();
    }

    @Test
    void processLoanApprovalTemplateSuccessfully() {
        PaymentDetail payment1 = new PaymentDetail(1, BigDecimal.valueOf(500000), BigDecimal.valueOf(400000), BigDecimal.valueOf(100000), BigDecimal.valueOf(4500000));
        PaymentDetail payment2 = new PaymentDetail(2, BigDecimal.valueOf(500000), BigDecimal.valueOf(420000), BigDecimal.valueOf(80000), BigDecimal.valueOf(4080000));

        LoanApproval loanApproval = new LoanApproval(
                DomainConstants.MICROCREDIT,
                BigDecimal.valueOf(5000000),
                12,
                BigDecimal.valueOf(2.08),
                BigDecimal.valueOf(500000),
                List.of(payment1, payment2)
        );

        StepVerifier.create(adapter.processLoanApprovalTemplate(loanApproval))
                .assertNext(html -> {
                    assertThat(html).isNotNull();
                    assertThat(html).isNotEmpty();
                    assertThat(html).contains(DomainConstants.MICROCREDIT);
                    assertThat(html).contains("12");
                    assertThat(html).contains("2.08");
                })
                .verifyComplete();
    }

}