package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.proofoforigin.api.business.KeycloakCallService;
import org.cardanofoundation.proofoforigin.api.business.WineryService;
import org.cardanofoundation.proofoforigin.api.constants.Constants;
import org.cardanofoundation.proofoforigin.api.constants.Role;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.UserCreateDto;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.KeycloakUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUpdateBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.request.WineryUserBody;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BaseResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.BriefWineryResponse;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.response.WineryInfoUserResponse;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotError;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotErrors;
import org.cardanofoundation.proofoforigin.api.exceptions.BolnisiPilotException;
import org.cardanofoundation.proofoforigin.api.repository.WineryRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.Winery;
import org.cardanofoundation.proofoforigin.api.utils.SecurityContextHolderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class WineryServiceImpl implements WineryService {
    private WineryRepository wineryRepository;
    private KeycloakCallService keycloakCallService;
    private SecurityContextHolderUtil securityContextHolderUtil;


    private final static String emailRegexPattern = "^[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]{1,255}\\.[A-Za-z]{2,63}$";

    public synchronized BaseResponse<BriefWineryResponse> createWinery(WineryUserBody wineryUserBody) {
        validate(wineryUserBody);
        UserCreateDto userCreateDto = keycloakCallService.createUser(wineryUserBody, Role.WINERY);
        Winery winery = new Winery();
        winery.setWineryName(wineryUserBody.getName());
        winery.setWineryRsCode(wineryUserBody.getRsCode());
        winery.setKeycloakUserId(userCreateDto.getId());
        winery = saveWinery(winery);
        String sendMailWineryIdStatus = Boolean.TRUE.equals(userCreateDto.getIsSendMail()) ?
                Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_SUCCESS : Constants.SEND_MAIL.SEND_MAIL_WINERY_ID_FAIL;
        return BaseResponse.ofSucceededCreate(BriefWineryResponse.fromWinery(winery), sendMailWineryIdStatus);
    }

    @Override
    public void updateWinery(String wineryId, WineryUpdateBody wineryUpdateBody) {
        Winery winery = wineryRepository.findByWineryId(wineryId).orElseThrow(() -> new BolnisiPilotException(BolnisiPilotErrors.WINERY_NOT_FOUND));
        String name = wineryUpdateBody.getName();
        if (name != null) {
            winery.setWineryName(name);
        }
        String rsCode = wineryUpdateBody.getRsCode();
        if (rsCode != null) {
            // In reality this cannot change, but it's updatable in our system in case an existing winery
            // registers themselves officially, or there was a mistake in our system.
            winery.setWineryRsCode(rsCode);
        }
        wineryRepository.save(winery);
    }

    public byte[] getWineryPublicKey(String wineryId) {
        Optional<Winery> winery = wineryRepository.findByWineryId(wineryId);
        return winery.map(entity -> {
            String base64PKey = entity.getPublicKey();
            return base64PKey == null ? new byte[]{} : Base64.getUrlDecoder().decode(base64PKey);
        }).orElseThrow(() -> new BolnisiPilotException(BolnisiPilotErrors.NOT_FOUND));
    }

    @Override
    public List<WineryInfoUserResponse> getAllWinery() {
        List<String> roles = securityContextHolderUtil.getListRoles();
        Predicate<String> isAdminOrDataProviderOrNwaRole = role -> role.equals(Role.ADMIN.toString())
                || role.equals(Role.DATA_PROVIDER.toString()) || role.equals(Role.NWA.toString());
        if (roles.stream().anyMatch(isAdminOrDataProviderOrNwaRole)) {
            return wineryRepository.findByOrderByWineryIdLPad().stream()
                    .map(WineryInfoUserResponse::buildWineryUserInfoResponse).collect(Collectors.toList());
        }
        String keycloakUserId = securityContextHolderUtil.getKeyCloakUserId();
        Optional<Winery> winery = wineryRepository.findFirstByKeycloakUserId(keycloakUserId);
        return winery.map(value -> List.of(WineryInfoUserResponse.buildWineryUserInfoResponse(value))).orElseGet(ArrayList::new);
    }

    private void validate(KeycloakUserBody keycloakUserBody) {
        if (!emailFormatCorrect(keycloakUserBody.getEmail())) {
            throw new BolnisiPilotException(new BolnisiPilotError(HttpStatus.BAD_REQUEST.value(), "Invalid email.", HttpStatus.BAD_REQUEST));
        }
    }

    private static boolean emailFormatCorrect(String email) {
        return Pattern.compile(emailRegexPattern)
                .matcher(email)
                .matches();
    }

    @Override
    public Winery saveWinery(Winery winery) {
        String wineryId = winery.getWineryId();
        if (StringUtils.isEmpty(wineryId)) {
            String nextWineryId = wineryRepository.findTopByOrderByWineryIdLPadDesc()
                    .map(wineryEntity -> {
                        String maxWineryId = wineryEntity.getWineryId();
                        long maxWineryIdLong = Long.parseLong(maxWineryId, 16);
                        return Long.toHexString(++maxWineryIdLong);
                    })
                    .orElse(String.valueOf(1));

            // Sanity check in case there are bugs in the above logic.
            if (wineryRepository.existsById(nextWineryId)) {
                throw new BolnisiPilotException(BolnisiPilotErrors.CALCULATED_WINERY_ID_NOT_UNIQUE);
            }

            winery.setWineryId(nextWineryId);
        }
        return wineryRepository.save(winery);
    }
}
