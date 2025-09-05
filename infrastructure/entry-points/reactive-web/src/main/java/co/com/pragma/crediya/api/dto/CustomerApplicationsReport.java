package co.com.pragma.crediya.api.dto;

import co.com.pragma.crediya.model.loan.report.CustomerApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerApplicationsReport {

    List<CustomerApplication> data;

    ReportMetadata metadata;

}
