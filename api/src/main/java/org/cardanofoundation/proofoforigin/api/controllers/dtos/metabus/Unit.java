package org.cardanofoundation.proofoforigin.api.controllers.dtos.metabus;

public interface Unit {
    interface ConfigKeycloak {
        String GRANT_TYPE = "grant_type";
        String CLIENT_ID = "client_id";
        String CLIENT_SECRET = "client_secret";
        String SCOPE = "scope";
    }

    enum GroupType {
        SINGLE_GROUP, MULTI_GROUP
    }

    interface MetabusConstants {
        String METABUS_TYPE_CERT = "conformityCert:georgianWine";
        String METABUS_TYPE_SCM = "scm:georgianWine";
        String METABUS_TYPE_CERT_REVOCATION = "conformityCertRevoke:georgianWine";
    }

    enum MetabusJobType {
        CERT, LOT, CERT_REVOCATION
    }
}
