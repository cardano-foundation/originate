package org.cardanofoundation.proofoforigin.api.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class BolnisiPilotErrors {
    private BolnisiPilotErrors() {
    }

    /**
     * 400
     */
    public static final BolnisiPilotError INVALID_PARAMETERS =
            new BolnisiPilotError(400000, "Invalid Parameter", HttpStatus.BAD_REQUEST);

    public static final BolnisiPilotError FILE_MISSING =
            new BolnisiPilotError(1, "File missing", HttpStatus.BAD_REQUEST);

    public static final BolnisiPilotError INVALID_FILE_TYPE =
            new BolnisiPilotError(2, "Invalid file type", HttpStatus.BAD_REQUEST);

    public static final BolnisiPilotError INVALID_DATA =
            new BolnisiPilotError(3, "Invalid data", HttpStatus.BAD_REQUEST);

    /**
     * 401
     */
    public static final BolnisiPilotError UNAUTHORIZED =
            new BolnisiPilotError(401, "Invalid or expired token. Please obtain a new token by the keycloak " +
                    "login api", HttpStatus.UNAUTHORIZED);

    /**
     * 403
     */
    public static final BolnisiPilotError FORBIDDEN =
            new BolnisiPilotError(403, "You do not have permission to access this resource.",
                    HttpStatus.FORBIDDEN);

    /**
     * 404
     */
    public static final BolnisiPilotError NOT_FOUND =
            new BolnisiPilotError(404, "Winery does not exist",
                    HttpStatus.NOT_FOUND);

    /**
     * 409
     */

    public static final BolnisiPilotError CONFLICT =
            new BolnisiPilotError(409, "Entity already exist", HttpStatus.CONFLICT);


    public static final BolnisiPilotError REQUEST_FORMAT =
            new BolnisiPilotError(400, "incorrect request format", HttpStatus.BAD_REQUEST);

    public static final BolnisiPilotError METABUS_ERROR =
            new BolnisiPilotError(500002, "Failed to call to Metabus system", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 404
     */
    public static final BolnisiPilotError LOT_NOT_FOUND =
            new BolnisiPilotError(404, "Lot does not exist",
                    HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError BOTTLE_NOT_FOUND =
            new BolnisiPilotError(404, "bottleId does not exist",
                    HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError WINERY_NOT_FOUND =
            new BolnisiPilotError(404, "Winery does not exist",
                    HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError WINERY_MISSING_RS_CODE =
            new BolnisiPilotError(409, "Winery company RS code has not been set yet",
                    HttpStatus.CONFLICT);

    /**
     * 500
     */
    public static final BolnisiPilotError INTERNAL_SERVER_ERROR =
            new BolnisiPilotError(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    HttpStatus.INTERNAL_SERVER_ERROR);


    /**
     * Function Partial Scan
     */
    public static final BolnisiPilotError PS_INVALID_PARAMETERS_LOTID_OR_CERTID =
            new BolnisiPilotError(404, "Certificate does not contain bottles from that lot", HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError PS_NOT_ON_CHAIN_CERTID =
            new BolnisiPilotError(404, "Certificate is not on chain", HttpStatus.NOT_FOUND);
    public static final BolnisiPilotError PS_PAIR_LOTID_CERTID_APPROVE =
            new BolnisiPilotError(409, "Bottles have already been scanned and approved for that certificate and lot pair", HttpStatus.CONFLICT);

    public static BolnisiPilotError errorBottleIds(int errorCode, List<String> bottleIds, HttpStatus httpStatus, String errorMessage) {
        String formattedBottleIds = bottleIds.stream()
                .map(id -> String.format("%s", id))
                .collect(Collectors.joining(","));

        String msg = String.format("%s [%s]", errorMessage, formattedBottleIds);
        return new BolnisiPilotError(errorCode, msg, httpStatus);
    }

    public static final String ERROR_MESSAGE_BOTTLE_WITH_LOT = "Bottles have not already been associated with lot:";
    public static final String ERROR_MESSAGE_BOTTLE_WITH_CERT = "Bottles have not already been associated with cert:";
    public static final String ERROR_MESSAGE_BOTTLE_HAVE_BEEN_ASSOCIATED= "Bottles have already been associated with cert:";

    public static final BolnisiPilotError CERT_DOES_NOT_EXIST = new BolnisiPilotError(404, "certificate does not exist",
            HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError CERT_HAD_ALREADY_BEEN_REVOKED = new BolnisiPilotError(409, "certificate already revoked",
            HttpStatus.CONFLICT);

    public static final BolnisiPilotError SIGNATURE_INVALID_OR_FAILED_VERIFICATION = new BolnisiPilotError(400, "provided public key and signature pair is not valid for given data",
            HttpStatus.BAD_REQUEST);

    public static final BolnisiPilotError ACCOUNT_NOT_TERMS =
            new BolnisiPilotError(403, "Account has not accepted terms and conditions yet for this application",
                    HttpStatus.FORBIDDEN);

    public static final BolnisiPilotError USER_NOT_FOUND_KEYCLOAK =
            new BolnisiPilotError(404, "User not found",
                    HttpStatus.NOT_FOUND);

    public static final BolnisiPilotError CALCULATED_WINERY_ID_NOT_UNIQUE =
            new BolnisiPilotError(500, "Next calculated winery ID has already been taken, please contact an admin.", HttpStatus.INTERNAL_SERVER_ERROR);
}
