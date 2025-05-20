package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertRequest;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;

import java.util.List;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateDataDTO {
    String companyName;
    String companyRsCode;
    String certificateNumber;
    String certificateType;
    String exportCountry;
    String examProtocolNumber;
    String tastingProtocolNumber;
    List<CertificateLotEntryDataDto> products;

    public static CertificateDataDTO toCertificateDataDTO(Winery winery, CertRequest certRequest) {
        CertBody certificate = certRequest.getCert();
        return CertificateDataDTO.builder()
                .companyName(winery.getWineryName())
                .companyRsCode(winery.getWineryRsCode())
                .certificateNumber(certificate.getCertificateNumber())
                .certificateType(certificate.getCertificateType())
                .exportCountry(certificate.getExportCountry())
                .examProtocolNumber(certificate.getExamProtocolNumber())
                .tastingProtocolNumber(certificate.getTastingProtocolNumber())
                .products(certificate.getProducts().stream()
                        .map(CertificateLotEntryDataDto::toLotEntriesDto)
                        .toList())
                .build();
    }
}
