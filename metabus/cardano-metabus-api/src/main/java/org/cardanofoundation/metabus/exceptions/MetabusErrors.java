package org.cardanofoundation.metabus.exceptions;

import org.springframework.http.HttpStatus;

public class MetabusErrors {
    private MetabusErrors() {
    }

    /**
     * 400
     */
    public static final MetabusError INVALID_PARAMETERS = new MetabusError(400000, "Invalid Parameter", HttpStatus.BAD_REQUEST);
    public static final MetabusError INVALID_JOB_TYPE = new MetabusError(400001, "Invalid Job Type, job type must be in format: <type>:<subType>", HttpStatus.BAD_REQUEST);
    public static final MetabusError INVALID_SIGNATURE = new MetabusError(400002, "Invalid Signature, signature must be in format: <jwsHeader>.<signature>", HttpStatus.BAD_REQUEST);

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
    public static final MetabusError ERROR_CREATING_JOB = new MetabusError(500000, "Error when creating job", HttpStatus.INTERNAL_SERVER_ERROR);
    public static final MetabusError ERROR_GETTING_JOB = new MetabusError(500001, "Error when getting job from database", HttpStatus.INTERNAL_SERVER_ERROR);
}
