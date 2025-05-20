package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.CertsResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryCertsResponse;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;

import java.util.List;


public interface CertificateService {
    List<CertsResponse> getByWineryId(String wineryId);

    void createCertificate(String certId, String wineryId, CertRequest certRequest);

    boolean isCertificateExist(String certId);

    List<WineryCertsResponse> getAllCertAll();

    /**
     * <p>
     * Revoke certification by cert id 
     * </p>
     * 
     * @param certId The target cert id
     * @param signature The signature of the NWA
     * @param publicKey the public key of the NWA
     */
    void revokeCertificate(final String certId, final String signature, final String publicKey);

    CertificateEntity updateTxIdAndJobIndexForCertificate(Long jobId, String txId, String jobIndex);

    CertificateEntity updateTxIdAndJobIndexForCertificateRevoke(Long revokeJobId, String txId, String jobIndex);
}
