package org.cardanofoundation.proofoforigin.api.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OriginatePilotErrors {
    private OriginatePilotErrors() {
    }

    /**
     * 400
     */
    public static final OriginatePilotError INVALID_PARAMETERS =
            new OriginatePilotError(400000, "Invalid Parameter", HttpStatus.BAD_REQUEST);

    public static final OriginatePilotError FILE_MISSING =
            new OriginatePilotError(1, "File missing", HttpStatus.BAD_REQUEST);

    public static final OriginatePilotError INVALID_FILE_TYPE =
            new OriginatePilotError(2, "Invalid file type", HttpStatus.BAD_REQUEST);

    public static final OriginatePilotError INVALID_DATA =
            new OriginatePilotError(3, "Invalid data", HttpStatus.BAD_REQUEST);

    /**
     * 401
     */
    public static final OriginatePilotError UNAUTHORIZED =
            new OriginatePilotError(401, "Invalid or expired token. Please obtain a new token by the keycloak " +
                    "login api", HttpStatus.UNAUTHORIZED);

    /**
     * 403
     */
    public static final OriginatePilotError FORBIDDEN =
            new OriginatePilotError(403, "You do not have permission to access this resource.",
                    HttpStatus.FORBIDDEN);

    /**
     * 404
     */
    public static final OriginatePilotError NOT_FOUND =
            new OriginatePilotError(404, "Winery does not exist",
                    HttpStatus.NOT_FOUND);

    /**
     * 409
     */

    public static final OriginatePilotError CONFLICT =
            new OriginatePilotError(409, "Entity already exist", HttpStatus.CONFLICT);


    public static final OriginatePilotError REQUEST_FORMAT =
            new OriginatePilotError(400, "incorrect request format", HttpStatus.BAD_REQUEST);

    public static final OriginatePilotError METABUS_ERROR =
            new OriginatePilotError(500002, "Failed to call to Metabus system", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 404
     */
    public static final OriginatePilotError LOT_NOT_FOUND =
            new OriginatePilotError(404, "Lot does not exist",
                    HttpStatus.NOT_FOUND);

    public static final OriginatePilotError BOTTLE_NOT_FOUND =
            new OriginatePilotError(404, "bottleId does not exist",
                    HttpStatus.NOT_FOUND);

    public static final OriginatePilotError WINERY_NOT_FOUND =
            new OriginatePilotError(404, "Winery does not exist",
                    HttpStatus.NOT_FOUND);

    public static final OriginatePilotError WINERY_MISSING_RS_CODE =
            new OriginatePilotError(409, "Winery company RS code has not been set yet",
                    HttpStatus.CONFLICT);

    /**
     * 500
     */
    public static final OriginatePilotError INTERNAL_SERVER_ERROR =
            new OriginatePilotError(500, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    HttpStatus.INTERNAL_SERVER_ERROR);


    /**
     * Function Partial Scan
     */
    public static final OriginatePilotError PS_INVALID_PARAMETERS_LOTID_OR_CERTID =
            new OriginatePilotError(404, "Certificate does not contain bottles from that lot", HttpStatus.NOT_FOUND);

    public static final OriginatePilotError PS_NOT_ON_CHAIN_CERTID =
            new OriginatePilotError(404, "Certificate is not on chain", HttpStatus.NOT_FOUND);
    public static final OriginatePilotError PS_PAIR_LOTID_CERTID_APPROVE =
            new OriginatePilotError(409, "Bottles have already been scanned and approved for that certificate and lot pair", HttpStatus.CONFLICT);

    public static OriginatePilotError errorBottleIds(int errorCode, List<String> bottleIds, HttpStatus httpStatus, String errorMessage) {
        String formattedBottleIds = bottleIds.stream()
                .map(id -> String.format("%s", id))
                .collect(Collectors.joining(","));

        String msg = String.format("%s [%s]", errorMessage, formattedBottleIds);
        return new OriginatePilotError(errorCode, msg, httpStatus);
    }

    public static final String ERROR_MESSAGE_BOTTLE_WITH_LOT = "Bottles have not already been associated with lot:";
    public static final String ERROR_MESSAGE_BOTTLE_WITH_CERT = "Bottles have not already been associated with cert:";
    public static final String ERROR_MESSAGE_BOTTLE_HAVE_BEEN_ASSOCIATED= "Bottles have already been associated with cert:";

    public static final OriginatePilotError CERT_DOES_NOT_EXIST = new OriginatePilotError(404, "certificate does not exist",
            HttpStatus.NOT_FOUND);

    public static final OriginatePilotError CERT_HAD_ALREADY_BEEN_REVOKED = new OriginatePilotError(409, "certificate already revoked",
            HttpStatus.CONFLICT);

    public static final OriginatePilotError SIGNATURE_INVALID_OR_FAILED_VERIFICATION = new OriginatePilotError(400, "provided public key and signature pair is not valid for given data",
            HttpStatus.BAD_REQUEST);

    public static final OriginatePilotError ACCOUNT_NOT_TERMS =
            new OriginatePilotError(403, "Account has not accepted terms and conditions yet for this application",
                    HttpStatus.FORBIDDEN);

    public static final OriginatePilotError USER_NOT_FOUND_KEYCLOAK =
            new OriginatePilotError(404, "User not found",
                    HttpStatus.NOT_FOUND);

    public static final OriginatePilotError CALCULATED_WINERY_ID_NOT_UNIQUE =
            new OriginatePilotError(500, "Next calculated winery ID has already been taken, please contact an admin.", HttpStatus.INTERNAL_SERVER_ERROR);
}
