package org.cardanofoundation.proofoforigin.api.business.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Sets;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.util.Base64URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.cardanofoundation.proofoforigin.api.business.MetabusCallerService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.business.ScmApproveLotService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.Unit;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus.response.JobResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.ScmApproveResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmData;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.LotRepository;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Lot;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.EncryptUtil;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScmApproveLotServiceImpl implements ScmApproveLotService {
    @Value("${cardano-bolnisipilot-api.encrypt-password}")
    private String encryptPassword;
    @Required
    private final LotRepository lotRepository;
    @Required
    private final WineryRepository wineryRepository;
    @Required
    private final SecurityContextHolderUtil securityContextHolderUtil;
    @Required
    private final MetabusCallerService metabusCallerService;
    @Required
    private final ScanTrustService scanTrustService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public synchronized ScmApproveResponse approveLots(String wineryId, List<String> lotIds) {
        // Note - synchronized in case many concurrent requests to approve the same lot arrive.
        // A lot may be sent to the Metabus but not yet set to approved in DB and we can end up
        // with duplicate versions of the lot on-chain.
        // A better approach is to just approve in the DB with the signature and have another single process
        // send items to the Metabus in the background - see the TODO in the catch clause.
        ScmApproveResponse scmApproveResponse = new ScmApproveResponse();

        try {
            Winery winery = validateWinery(wineryId);
            List<Lot> lots = validateLotIds(lotIds, wineryId, scmApproveResponse);

            String privateKeyStr = winery.getPrivateKey();

            OctetKeyPair jwk;
            if (privateKeyStr == null || privateKeyStr.isBlank()) {

                jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                        .generate();


                byte[] salt = EncryptUtil.generateSalt();

                winery.setPublicKey(jwk.getX().toString());
                winery.setPrivateKey(Base64.getEncoder().encodeToString(
                        EncryptUtil.encrypt(jwk.getDecodedD(), encryptPassword, salt)));
                winery.setSalt(Base64.getEncoder().encodeToString(salt));
                wineryRepository.save(winery);
            } else {
                Base64URL publicKey = new Base64URL(winery.getPublicKey());
                Base64URL privateKey = Base64URL.encode(EncryptUtil.decrypt(Base64.getDecoder().decode(privateKeyStr), encryptPassword, Base64.getDecoder().decode(winery.getSalt())));

                jwk = (new OctetKeyPair.Builder(Curve.Ed25519, publicKey)).d(privateKey).build();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            for (Lot lot : lots) {
                String signature = null;
                try {
                    ScmData data = ScmData.fromEntity(lot);
                    scanTrustService.sendScmDataWhenApproved(data); // Wait for call to scan trust done

                    // Don't product this in the data signed and hashed
                    data.setProduct(null);
                    signature = signLotData(jwk, objectMapper.writeValueAsString(data));
                    JobResponse jobResponse = metabusCallerService.createJob(data, Unit.MetabusJobType.LOT, signature, winery.getPublicKey(), wineryId);
                    if (Objects.isNull(jobResponse.getId())) {
                        throw new BolnisiPilotException(BolnisiPilotErrors.METABUS_ERROR);
                    }
                    lot.setJobId(jobResponse.getId());
                    lot.setStatus(Constants.LOT_STATUS.APPROVED);
                    scmApproveResponse.getSucceed().add(lot.getLotId());
                } catch (Exception e) {
                    log.error("Error when approve lot: " + lot.getLotId(), e);
                    scmApproveResponse.getFailJobsNotScheduled().add(lot.getLotId());

                    // @TODO - foconnor: winerySignature is intended to be picked up by a sync background task if call to Metabus fails
                    //   Or even to completely remove the call to the API and only do it as a background job.
                    //   For now this isn't implemented, so we keep the lot status as finalised for re-approval.
                    lot.setWinerySignature(signature);
                    // Process to next lot
                }
                finally {
                    // Save lot immediately
                    lotRepository.save(lot);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            if (e instanceof BolnisiPilotException) throw (BolnisiPilotException) e;
            throw new BolnisiPilotException(BolnisiPilotErrors.INTERNAL_SERVER_ERROR);
        }
        return scmApproveResponse;
    }

    private String signLotData(OctetKeyPair jwk, String lotJson) throws JOSEException, IOException {
        JsonCanonicalizer jc = new JsonCanonicalizer(lotJson);
        Payload payload = new Payload(jc.getEncodedString());

        JWSObject jwsObject = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.EdDSA).build(),
                payload);
        jwsObject.sign(new Ed25519Signer(jwk));

        return jwsObject.getHeader().toBase64URL() + "." + jwsObject.getSignature();
    }

    private List<Lot> validateLotIds(List<String> lotIds, String wineryId, ScmApproveResponse scmApproveResponse) {
        List<Lot> lots = lotRepository.findByWineryIdAndLotIdIn(wineryId, Sets.newHashSet(lotIds));
        if (lots.size() != lotIds.size()) {
            List<String> dbLotId = lots.stream().map(Lot::getLotId).toList();
            List<String> notExistLot = lotIds.stream().filter(lot -> !dbLotId.contains(lot)).toList();
            scmApproveResponse.getFailLotsNotFound().addAll(notExistLot);
        }

        // This may need to change in the future if we have more lot status
        scmApproveResponse.getFailLotsAlreadyApproved().addAll(
                lots.stream().filter(lot -> lot.getStatus() == Constants.LOT_STATUS.APPROVED).map(Lot::getLotId).toList());

        scmApproveResponse.getFailLotsNotFinalised().addAll(
                lots.stream().filter(lot -> lot.getStatus() == Constants.LOT_STATUS.NOT_FINALIZED).map(Lot::getLotId).toList());

        return lots.stream().filter(lot -> lot.getStatus() == Constants.LOT_STATUS.FINALIZED).toList();
    }

    private Winery validateWinery(String wineryId) {
        Optional<Winery> wineryOptional = wineryRepository.findById(wineryId);
        if (wineryOptional.isEmpty()) {
            throw new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND);
        }

        Winery winery = wineryOptional.get();
        if (!winery.getKeycloakUserId().equals(securityContextHolderUtil.getKeyCloakUserId())) {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }

        return winery;
    }
}
