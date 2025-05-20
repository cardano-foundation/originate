package org.cardanofoundation.metabus.exceptions;

import org.springframework.http.HttpStatus;

public class MetabusErrors {
    private MetabusErrors() {
    }

    /**
     * 400
     */
    public static final MetabusError INVALID_PARAMETERS = new MetabusError(400000, "Invalid Parameter", HttpStatus.BAD_REQUEST);

    /**
     * 401
     */
    public static final MetabusError UNAUTHORIZED =
            new MetabusError(401, "Invalid or expired token. Please obtain a new token by the keycloak " +
                    "login api", HttpStatus.UNAUTHORIZED);

    /**
     * 403
     */
    public static final MetabusError FORBIDDEN =
            new MetabusError(403, "You do not have permission to access this resource.",
                    HttpStatus.FORBIDDEN);

    /**
     * 500
     */
    public static final MetabusError INVALID_JOB_TYPE = new MetabusError(500001, "Invalid Job Type, job type must be in format: <type>:<subType>", HttpStatus.INTERNAL_SERVER_ERROR);

    public static final MetabusError ERROR_MINIO_STORING =
            new MetabusError(
                    702,
                    "Error when storing object into storage",
                    HttpStatus.INTERNAL_SERVER_ERROR);

    public static final MetabusError ERROR_MINIO_GET_OBJECT =
            new MetabusError(
                    703,
                    "Error when getting object URL from storage",
                    HttpStatus.INTERNAL_SERVER_ERROR);
}
