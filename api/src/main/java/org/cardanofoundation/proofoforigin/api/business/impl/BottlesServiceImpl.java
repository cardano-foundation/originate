package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.proofoforigin.api.business.BottlesService;
import org.cardanofoundation.proofoforigin.api.configuration.UploadScmDataSync;
import org.cardanofoundation.proofoforigin.api.constants.CertStatus;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.constants.ScanningStatus;
import org.cardanofoundation.proofoforigin.api.constants.UploadType;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleIdBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.BottleRangeBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottleResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BottlesInformation;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.*;
import org.cardanofoundation.proofoforigin.api.repository.entities.Bottle;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateEntity;
import org.cardanofoundation.proofoforigin.api.repository.entities.CertificateLotEntryEntity;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class BottlesServiceImpl implements BottlesService {
    BottleRepository bottleRepository;
    WineryRepository wineryRepository;
    CertificateRepository certificateRepository;
    SecurityContextHolderUtil securityContextHolderUtil;
    CertificateLotEntryRepository certificateLotEntryEntityRepository;

    @Override
    public BottleResponse getBottlesByWineryId(String wineryId) {
        if (!hasPermission(wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }

        return BottleResponse.toBottleResponse(bottleRepository.getBottlesByWineryId(wineryId));
    }

    @Override
    public List<BottleDto> getBottlesByLotId(String wineryId, String lotId) {
        if (!hasPermission(wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }

        if (!bottleRepository.existsByLotIdAndWineryId(lotId, wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.LOT_NOT_FOUND);
        }

        return bottleRepository.getBottlesByWineryIdAndLotId(wineryId, lotId).stream()
                .map(BottleDto::toBottleDto)
                .toList();
    }

    private Winery getWineryById(String wineryId) {
        return wineryRepository.findByWineryId(wineryId)
                .orElseThrow(() -> new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND));
    }

    private boolean hasPermission(String wineryId) {
        Winery winery = getWineryById(wineryId);
        List<String> roles = securityContextHolderUtil.getListRoles();

        Predicate<String> isAdminOrDataProviderRole = role -> role.equals(Role.ADMIN.toString())
                || role.equals(Role.DATA_PROVIDER.toString());
        if (roles.stream().anyMatch(isAdminOrDataProviderRole)) {
            return true;
        }
        return (roles.contains(Role.WINERY.toString())
                && winery.getKeycloakUserId().equals(securityContextHolderUtil.getKeyCloakUserId()));
    }

    @Override
    public BottlesInformation getBottlesInformation(String wineryId, String bottleId) {
        if (!hasPermission(wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }

        Bottle bottle = bottleRepository.findByWineryIdAndId(wineryId, bottleId)
                .orElseThrow(() -> new BolnisiPilotException(BolnisiPilotErrors.BOTTLE_NOT_FOUND));

        CertificateLotEntryEntity certProduct = new CertificateLotEntryEntity();
        CertificateEntity cert;
        if (bottle.getCertificateId() != null) {
            certProduct = certificateLotEntryEntityRepository.findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndCertificateCertStatus(bottle.getCertificateId()
                    , bottle.getLotId(), CertStatus.ACTIVE);
            cert = certProduct.getCertificate();
        } else {
            cert = new CertificateEntity();
        }

        return BottlesInformation.builder()
                .lotId(bottle.getLotId())
                .certId(bottle.getCertificateId())
                .certNumber(cert.getCertificateNumber())
                .certType(cert.getCertificateType())
                .sequentialNumber(bottle.getSequentialNumber())
                .reelNumber(bottle.getReelNumber())
                .scanningStatus(certProduct.getScanningStatus())
                .build();
    }

    @Override
    @Transactional
    @UploadScmDataSync(doSync = false, uploadType = UploadType.BOTTLE_SYNC, inputCustomClassTypes = {
            BottleIdBody.class })
    public void updateCertificateAssociations(String wineryId, String certId, String lotId, BottleIdBody bottlesBody) {
        if (!hasPermission(wineryId)) {
            throw new BolnisiPilotException(BolnisiPilotErrors.FORBIDDEN);
        }

        //check certID, lotID is exits with wineryID
        Long certificateLotEntryByWineryId = certificateLotEntryEntityRepository
                .countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus(certId, lotId, wineryId, CertStatus.ACTIVE);
        if (certificateLotEntryByWineryId == 0L) {
            throw new BolnisiPilotException(BolnisiPilotErrors.PS_INVALID_PARAMETERS_LOTID_OR_CERTID);
        }

        //check certID is on chain
        List<CertificateEntity> certs = certificateRepository.findByCertificateIdAndTxIdIsNotNullAndCertStatus(certId, CertStatus.ACTIVE);
        if(certs.isEmpty()){
            throw new BolnisiPilotException(BolnisiPilotErrors.PS_NOT_ON_CHAIN_CERTID);
        }

        Set<String> bottleIdsAddSet = new HashSet<>(bottlesBody.getAdd());
        Set<String> bottleIdsRemoveSet = new HashSet<>(bottlesBody.getRemove());

            List<Bottle> associateBottle = bottleRepository.findByIdInAndCertificateIdNotNull(bottleIdsAddSet);
            if (!associateBottle.isEmpty()) {
                List<String> listBottleIds = associateBottle.stream().map(Bottle::getId).toList();
                throw new BolnisiPilotException(BolnisiPilotErrors
                        .errorBottleIds(HttpStatus.CONFLICT.value()
                                , new ArrayList<>(listBottleIds)
                                , HttpStatus.CONFLICT
                                , BolnisiPilotErrors.ERROR_MESSAGE_BOTTLE_HAVE_BEEN_ASSOCIATED));
            }

        //check CertificateLotEntryEntity has certID and lotID corresponding to param input
        Optional<CertificateLotEntryEntity> entities = certificateLotEntryEntityRepository
                .findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus(certId
                        , lotId
                        , ScanningStatus.APPROVED
                        , CertStatus.ACTIVE);
        if (entities.isEmpty()) {
            throw new BolnisiPilotException(BolnisiPilotErrors.PS_PAIR_LOTID_CERTID_APPROVE);
        }

        //check Bottle has been scan duplicate


        //check that each bottle is associated with this cert ID before remove
        if (!bottleIdsRemoveSet.isEmpty()){
            List<Bottle> bottleRemoveIsExits = bottleRepository.findByCertificateIdAndIdIn(certId, bottleIdsRemoveSet);
            if (bottleRemoveIsExits.size() != bottleIdsRemoveSet.size()){
                List<String> bottleRemoveInDB = bottleRemoveIsExits.stream().map(Bottle::getId).toList();
                bottleRemoveInDB.forEach(bottleIdsRemoveSet::remove);
                throw new BolnisiPilotException(BolnisiPilotErrors
                        .errorBottleIds(HttpStatus.CONFLICT.value()
                                , new ArrayList<>(bottleIdsRemoveSet)
                                , HttpStatus.CONFLICT
                                , BolnisiPilotErrors.ERROR_MESSAGE_BOTTLE_WITH_CERT));
            }
        }

        //check bottle can only be in 1 of 2 lists
        bottleIdsAddSet.forEach(
                add -> {
                    if (bottleIdsRemoveSet.contains(add)){
                        throw new BolnisiPilotException(BolnisiPilotErrors.REQUEST_FORMAT);
                    }
                }
        );

        //bottleID has been associated with lot
        List<Bottle> bottleExist = checkBottlesAssociatedWithLot(lotId, bottleIdsAddSet);
        List<Bottle> bottleRemoveExist = checkBottlesAssociatedWithLot(lotId, bottleIdsRemoveSet);

        //add certId for list bottle
        bottleExist.forEach(item -> item.setCertificateId(certId));

        //remove certId for list bottle remove
        bottleRemoveExist.forEach(item -> item.setCertificateId(null));

        List<Bottle> bottlesUpdateCertId = new ArrayList<>();
        bottlesUpdateCertId.addAll(bottleExist);
        bottlesUpdateCertId.addAll(bottleRemoveExist);
        bottleRepository.saveAll(bottlesUpdateCertId);

        CertificateLotEntryEntity certificateLotEntry = entities.get();
        if (bottlesBody.getFinalise().equals(Boolean.FALSE)) {
            if (!certificateLotEntry.getScanningStatus().equals(ScanningStatus.SCANNING)) {
                certificateLotEntry.setScanningStatus(ScanningStatus.SCANNING);
            }
        } else {
            certificateLotEntry.setScanningStatus(ScanningStatus.APPROVED);
        }
        certificateLotEntry.setWineryId(wineryId);
        certificateLotEntryEntityRepository.save(certificateLotEntry);
    }

    private List<Bottle> checkBottlesAssociatedWithLot(String lotId, Set<String> bottleIdsSet) {
        List<Bottle> bottleExist = bottleRepository.findByLotIdAndIdIn(lotId, bottleIdsSet);
        if (!(bottleExist.size() == bottleIdsSet.size())) {
            List<String> bottleInDB = bottleExist.stream().map(Bottle::getId).toList();
            bottleInDB.forEach(bottleIdsSet::remove);
            throw new BolnisiPilotException(BolnisiPilotErrors
                    .errorBottleIds(HttpStatus.CONFLICT.value()
                            , new ArrayList<>(bottleIdsSet)
                            , HttpStatus.CONFLICT
                            , BolnisiPilotErrors.ERROR_MESSAGE_BOTTLE_WITH_LOT));
        }
        return bottleExist;
    }

    @Override
    public BottleIdBody convertBottleRangeBodyToBottleIdBody(BottleRangeBody bottleRangeBody) {
        /**
         * bottlesInRange: is list range Object Bottle corresponding with param range input
         * bottleIdsFromSNRange: is list String bottleIds mapped from bottlesInRange
         * lstSequentialNumber: is list Integer converted from param input:
         * - parse sequential_number input or extendedId query with DB so get it
         * bottleBeginEnd:
         * - is 1 Object Bottle corresponding with 1 value startRange or endRange (case startRange equals endRange)
         * - is 2 Object Bottle corresponding with 2 value start - range (case startRange different endRange)
         */

        List<Bottle> bottlesInRange = new ArrayList<>();
        List<String> bottleIdsFromSNRange = new ArrayList<>();
        List<Integer> lstSequentialNumber = new ArrayList<>();
        List<Bottle> bottleBeginEnd = new ArrayList<>();

        if (bottleRangeBody.getIsSequentialNumber()) {
            try {
                lstSequentialNumber.add(Integer.parseInt(bottleRangeBody.getStartRange()));
                lstSequentialNumber.add(Integer.parseInt(bottleRangeBody.getEndRange()));
            }catch (NumberFormatException e){
                throw new BolnisiPilotException(BolnisiPilotErrors.INVALID_PARAMETERS);
            }
        } else {
            /**
             * <p> 2 case with range is ExtendedId:
             *  -----------------------
             *      case 1: startRange different endRange
             *      ex: range is ["abc","xyz"]
             *      @Param startRange = "abc"
             *      @Param endRange = "xyz"
             *      add them to List<String>: {"abc","xyz"}
             *      query in DB so get 2 Object Bottle
             *      map to List lstSequentialNumber include 2 value sequential_number. ex: List<Integer>: { 1,2 }
             *  -----------------------
             *      case2: startRange equals endRange
             *      ex: range is ["abc","abc"]
             *      @Param startRange = "abc"
             *      @Param endRange = "abc"
             *      query in DB so get 1 Object Bottle
             *      map to List lstSequentialNumber include 1 value sequential_number. ex: List<Integer>: { 1 }
             * </p>
             */
            List<String> bottleIdsFromRange = new ArrayList<>();
            bottleIdsFromRange.add(bottleRangeBody.getStartRange());
            bottleIdsFromRange.add(bottleRangeBody.getEndRange());

            bottleBeginEnd = bottleRepository.findByIdIn(bottleIdsFromRange);
            lstSequentialNumber = bottleBeginEnd.stream().map(Bottle::getSequentialNumber).collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(lstSequentialNumber) && lstSequentialNumber.size() > 1) {
            /**
             * <p>
             *  handle case startRange different endRange (lstSequentialNumber have 2 value)
             * </p>
             */
            if (lstSequentialNumber.get(0) != null && lstSequentialNumber.get(1) != null) {
                lstSequentialNumber.sort(Comparator.naturalOrder());
                bottlesInRange = bottleRepository.findBySequentialNumberBetween(lstSequentialNumber.get(0), lstSequentialNumber.get(1));
            }
        } else {
            /**
             * <p>
             *  handle case startRange equals endRange
             * </p>
             */
            bottlesInRange = bottleBeginEnd;
        }

        if (!CollectionUtils.isEmpty(bottlesInRange)) {
            bottleIdsFromSNRange = bottlesInRange.stream()
                    .map(Bottle::getId)
                    .collect(Collectors.toList());
        }

        if (bottleIdsFromSNRange.isEmpty()) {
            throw new BolnisiPilotException(BolnisiPilotErrors.PS_INVALID_PARAMETERS_LOTID_OR_CERTID);
        }

        BottleIdBody bottlesBody = new BottleIdBody();
        bottlesBody.setAdd(bottleIdsFromSNRange);
        bottlesBody.setRemove(new ArrayList<>());
        bottlesBody.setFinalise(bottleRangeBody.getFinalise());

        return bottlesBody;
    }
}
