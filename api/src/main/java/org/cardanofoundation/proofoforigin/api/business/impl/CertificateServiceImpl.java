package org.cardanofoundation.proofoforigin.api.business.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.CertificateService;
import org.cardanofoundation.proofoforigin.api.business.MetabusCallerService;
import org.cardanofoundation.proofoforigin.api.configuration.AppConfig;
import org.cardanofoundation.proofoforigin.api.configuration.UploadScmDataSync;
import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.constants.CertUpdateStatus;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.CertificateDataDTO;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.CertificateRevokeDTO;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertRequest;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.CertLotEntryBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.CertsResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.CertsResponseLotEntry;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryCertsResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.OriginatePilotException;
import org.cardanofoundation.proofoforigin.api.repository.BottleRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateLotEntryRepository;
import org.cardanofoundation.proofoforigin.api.repository.CertificateRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.*;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final AppConfig.CertificateVerificationConfig certificateVerificationConfig;

    private final CertificateLotEntryRepository certificateLotEntryEntityRepository;

    private final CertificateRepository certificateRepository;

    private final MetabusCallerService metabusCallerService;

    private final WineryRepository wineryRepository;

    private final SecurityContextHolderUtil securityContextHolderUtil;

    private final BottleRepository bottleRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public List<CertsResponse> getByWineryId(String wineryId) {
        if (hasPermission(wineryId)) {
            List<CertificateLotEntryEntity> certificateLotEntryEntityList = certificateLotEntryEntityRepository
                    .findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus(wineryId, CertStatus.ACTIVE);
            return convertLotEntriesToResponse(certificateLotEntryEntityList);
        } else {
            throw new OriginatePilotException(OriginatePilotErrors.FORBIDDEN);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createCertificate(String certId, String wineryId, CertRequest certRequest) {
        if (isCertificateExist(certId)) {
            throw new OriginatePilotException(OriginatePilotErrors.CONFLICT);
        }

        Winery winery = wineryRepository.findByWineryId(wineryId)
                .orElseThrow(() -> new OriginatePilotException(OriginatePilotErrors.WINERY_NOT_FOUND));

        if (winery.getWineryRsCode() == null) {
            throw new OriginatePilotException(OriginatePilotErrors.WINERY_MISSING_RS_CODE);
        }

        CertificateDataDTO certDto = CertificateDataDTO.toCertificateDataDTO(winery, certRequest);
        if (!signatureIsValid(certDto, certRequest.getSignature(), certRequest.getPublicKeyBase64Url())) {
            throw new OriginatePilotException(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION);
        }

        CertificateEntity certificateEntity = saveCertificate(certId, wineryId, certRequest);
        saveCertificateLotEntries(certId, wineryId, certRequest);

        JobResponse jobResponse = metabusCallerService.createJob(
                certDto, Unit.MetabusJobType.CERT,
                certRequest.getSignature(), certRequest.getPublicKeyBase64Url());
        if (Objects.isNull(jobResponse.getId())) {
            throw new OriginatePilotException(OriginatePilotErrors.METABUS_ERROR);
        }

        certificateEntity.setJobId(jobResponse.getId());
        certificateRepository.save(certificateEntity);
    }

    @Override
    public boolean isCertificateExist(String certId) {
        return certificateRepository.existsById(certId);
    }

    @Override
    public List<WineryCertsResponse> getAllCertAll() {
        List<CertificateLotEntryEntity> entryDtos = certificateLotEntryEntityRepository
                .findAllByCertificateCertStatus(CertStatus.ACTIVE);
        Map<String, List<CertificateLotEntryEntity>> mapWineryIdToList = entryDtos.stream()
                .collect(Collectors.groupingBy(CertificateLotEntryEntity::getWineryId));
        List<WineryCertsResponse> wineryCertsResponses = new ArrayList<>();
        mapWineryIdToList.forEach((s, lotEntryDtos) -> {
            WineryCertsResponse response = new WineryCertsResponse();
            response.setWineryId(s);
            List<CertsResponse> certsResponses = convertLotEntriesToResponse(lotEntryDtos);
            response.setListCerts(certsResponses);
            wineryCertsResponses.add(response);
        });
        return wineryCertsResponses;
    }

    @Override
    public CertificateEntity updateTxIdAndJobIndexForCertificate(Long jobId, String txId, String jobIndex) {
        Optional<CertificateEntity> certificate = certificateRepository.findByJobId(jobId);
        if (certificate.isEmpty()) {
            return null;
        }
        CertificateEntity certificateEntity = certificate.get();
        certificateEntity.setTxId(txId);
        certificateEntity.setJobIndex(jobIndex);
        return certificateRepository.save(certificateEntity);
    }

    @Override
    public CertificateEntity updateTxIdAndJobIndexForCertificateRevoke(Long revokeJobId, String txId, String jobIndex) {
        Optional<CertificateEntity> certificate = certificateRepository.findByRevokeJobId(revokeJobId);
        if (certificate.isEmpty()) {
            return null;
        }
        CertificateEntity certificateEntity = certificate.get();
        certificateEntity.setRevokeTxId(txId);
        certificateEntity.setRevokeJobIndex(jobIndex);
        return certificateRepository.save(certificateEntity);
    }

    private void saveCertificateLotEntries(String certId, String wineryId,
                                           CertRequest certRequest) {
        CertBody certBody = certRequest.getCert();
        List<CertLotEntryBody> certLotEntryBodyList = certBody.getProducts();
        List<CertificateLotEntryEntity> listEntity = new ArrayList<>();
        for (CertLotEntryBody certLotEntryBody : certLotEntryBodyList) {
            CertificateLotEntryEntity certificateLotEntryEntity = new CertificateLotEntryEntity();
            CertificateLotEntryPK certificateLotEntryPK = new CertificateLotEntryPK();
            certificateLotEntryPK.setLotId(certLotEntryBody.getLotNumber());
            certificateLotEntryPK.setCertificateId(certId);
            certificateLotEntryEntity.setCertificateLotEntryPk(certificateLotEntryPK);

            certificateLotEntryEntity.setWineName(certLotEntryBody.getWineName());
            certificateLotEntryEntity.setWineDescription(certLotEntryBody.getWineDescription());
            certificateLotEntryEntity.setSerialName(certLotEntryBody.getSerialName());
            certificateLotEntryEntity.setOrigin(certLotEntryBody.getOrigin());
            certificateLotEntryEntity.setViticultureArea(certLotEntryBody.getViticultureArea());
            certificateLotEntryEntity.setType(certLotEntryBody.getType());
            certificateLotEntryEntity.setColor(certLotEntryBody.getColor());
            certificateLotEntryEntity.setSugarContentCategory(certLotEntryBody.getSugarContentCategory());
            certificateLotEntryEntity.setGrapeVariety(certLotEntryBody.getGrapeVariety());
            certificateLotEntryEntity.setHarvestYear(certLotEntryBody.getHarvestYear());
            certificateLotEntryEntity.setDelayedOnChacha(certLotEntryBody.getDelayedOnChacha());
            certificateLotEntryEntity.setBottleType(certLotEntryBody.getBottleType());
            certificateLotEntryEntity.setBottlingDate(certLotEntryBody.getBottlingDate());
            certificateLotEntryEntity.setBottleVolume(certLotEntryBody.getBottleVolume());
            certificateLotEntryEntity.setBottleCountInLot(certLotEntryBody.getBottleCountInLot());
            certificateLotEntryEntity.setWineryId(wineryId);
            listEntity.add(certificateLotEntryEntity);
        }
        certificateLotEntryEntityRepository.saveAll(listEntity);
    }

    private CertificateEntity saveCertificate(String certId, String wineryId, CertRequest certRequest) {
        CertBody certBody = certRequest.getCert();
        CertificateEntity entity = CertificateEntity.builder()
                .certificateId(certId)
                .certificateNumber(certBody.getCertificateNumber())
                .certificateType(certBody.getCertificateType())
                .exportCountry(certBody.getExportCountry())
                .examProtocolNumber(certBody.getExamProtocolNumber())
                .tastingProtocolNumber(certBody.getTastingProtocolNumber())
                .signature(certRequest.getSignature())
                .pubKey(certRequest.getPublicKeyBase64Url())
                .wineryId(wineryId)
                .build();
        return certificateRepository.save(entity);
    }

    private List<CertsResponse> convertLotEntriesToResponse(List<CertificateLotEntryEntity> list) {
        Map<CertificateEntity, List<CertificateLotEntryEntity>> map = list.stream()
                .collect(Collectors.groupingBy(CertificateLotEntryEntity::getCertificate));
        List<CertsResponse> response = new ArrayList<>();
        map.forEach((certificate, certificateLotEntryDtos) -> {
            CertsResponse dto = new CertsResponse();
            dto.setId(certificate.getCertificateId());
            dto.setCertificateNumber(certificate.getCertificateNumber());
            dto.setCertificateType(certificate.getCertificateType());
            Set<CertsResponseLotEntry> lotEntries = certificateLotEntryDtos.stream().map(certificateLotEntryDto -> {
                CertsResponseLotEntry entry = new CertsResponseLotEntry();
                entry.setLotId(certificateLotEntryDto.getCertificateLotEntryPk().getLotId());
                entry.setScanningStatus(certificateLotEntryDto.getScanningStatus().toString());
                return entry;
            }).collect(Collectors.toSet());
            dto.setLotEntries(lotEntries);
            response.add(dto);
        });
        return response;
    }

    private boolean hasPermission(String wineryId) {
        Winery winery = wineryRepository.findByWineryId(wineryId)
                .orElseThrow(() -> new OriginatePilotException(OriginatePilotErrors.NOT_FOUND));
        List<String> roles = securityContextHolderUtil.getListRoles();
        Predicate<String> isAdminOrNwaRole = role -> role.equals(Role.ADMIN.toString())
                || role.equals(Role.NWA.toString());
        if (roles.stream().anyMatch(isAdminOrNwaRole)) {
            return true;
        }
        return (roles.contains(Role.WINERY.toString())
                && winery.getKeycloakUserId().equals(securityContextHolderUtil.getKeyCloakUserId()));
    }

    private boolean signatureIsValid(Object object, String signature, String pubKeyStr) {

        if (certificateVerificationConfig.signatureVerificationDisabled) {
            return true;
        }

        try {
            String[] sigParts = signature.split("\\.");
            String payload = Base64URL
                    .encode(new JsonCanonicalizer(CertificateServiceImpl.objectMapper.writeValueAsString(object))
                    .getEncodedString())
                    .toString();
            OctetKeyPair jwk = (new OctetKeyPair.Builder(Curve.Ed25519, new Base64URL(pubKeyStr))).build();
            JWSVerifier verifier = new Ed25519Verifier(jwk.toPublicJWK());

            JWSObject jwsObject = JWSObject.parse(sigParts[0] + "." + payload + "." + sigParts[1]);
            return jwsObject.verify(verifier);
        } catch (Exception e) {
            // foconnor: There are a bunch of unchecked exceptions that can be thrown here,
            // I think catching the generic Exception is OK for safety - unlikely that something else went wrong other than bad inputs.
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void revokeCertificate(final String certId, final String signature, final String publicKey) {
        if (!hasPermissionOnlyForNWA()) {
            throw new OriginatePilotException(OriginatePilotErrors.FORBIDDEN);
        }

        final CertificateEntity certificateEntity = certificateRepository.findById(certId).orElseThrow(
                () -> new OriginatePilotException(OriginatePilotErrors.CERT_DOES_NOT_EXIST));
        if (CertStatus.REVOKED.equals(certificateEntity.getCertStatus())) {
            throw new OriginatePilotException(OriginatePilotErrors.CERT_HAD_ALREADY_BEEN_REVOKED);
        }

        final CertificateRevokeDTO certRevokeDto = CertificateRevokeDTO.builder()
                .certificateNumber(certificateEntity.getCertificateNumber())
                .certificateType(certificateEntity.getCertificateType())
                .build();
        if (!signatureIsValid(certRevokeDto, signature, publicKey)) {
            throw new OriginatePilotException(OriginatePilotErrors.SIGNATURE_INVALID_OR_FAILED_VERIFICATION);
        }

        certificateEntity.setCertStatus(CertStatus.REVOKED);
        certificateEntity.setRevokeSignature(signature);
        certificateEntity.setRevokePubKey(publicKey);
        certificateRepository.save(certificateEntity);

        final List<Bottle> listOfRevokedBottle = bottleRepository.findAllByCertificateId(certId);

        ((CertificateServiceImpl) AopContext.currentProxy()).syncCertRevocation(certId, certRevokeDto, signature, publicKey,
                listOfRevokedBottle);
    }

    @UploadScmDataSync(doSync = false, uploadType = UploadType.CERT_REVOCATION, inputCustomClassTypes = {
            List.class })
    public void syncCertRevocation(final String certId, final CertificateRevokeDTO certRevokeDto, final String signature, final String pubKey,
            final List<Bottle> listOfRevokedBottle) {

        // Reset bottles' cert to null
        listOfRevokedBottle.forEach(bottle -> {
            bottle.setCertificateId(null);
            bottle.setCertUpdateStatus(CertUpdateStatus.NOT_UPDATED);
        });
        bottleRepository.saveAll(listOfRevokedBottle);

        // Send cert revocation job to metabus
        final JobResponse jobResponse = metabusCallerService.createJob(certRevokeDto, Unit.MetabusJobType.CERT_REVOCATION,
                signature, pubKey);
        if (Objects.isNull(jobResponse.getId())) {
            throw new OriginatePilotException(OriginatePilotErrors.METABUS_ERROR);
        }

        // Update jobId to cert
        final CertificateEntity certificateEntity = certificateRepository.findById(certId)
                .orElseThrow(()-> new OriginatePilotException(OriginatePilotErrors.CERT_DOES_NOT_EXIST));
        certificateEntity.setRevokeJobId(jobResponse.getId());
        certificateRepository.save(certificateEntity);

        log.info(">>> List of bottles that are revoked: {}", listOfRevokedBottle);
    }

    /**
     * <p>
     * Checking that the current user has a permission to revoke the certificate.
     * </p>
     * <b>This checking for the service that only used by NWA role user. </b>
     *
     * @return is the current user has a NWA role or not ?
     */
    public boolean hasPermissionOnlyForNWA() {
        return securityContextHolderUtil.getListRoles().stream().anyMatch(role -> Role.NWA.toString().equals(role));
    }
}
