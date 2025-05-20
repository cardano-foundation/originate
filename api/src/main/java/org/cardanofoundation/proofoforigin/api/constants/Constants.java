package org.cardanofoundation.proofoforigin.api.constants;

import java.util.Map;
import java.util.regex.Pattern;

public interface Constants {
    interface LOT_STATUS {
        int NOT_FINALIZED = 0;
        int FINALIZED = 1;
        int APPROVED = 2;
    }

    interface LOT_ID {
        int LENGTH = 11;
    }

    interface ERROR_CODE {
        int FILE_MISSING = 1;
        int INVALID_FILE_TYPE = 2;
        int INVALID_DATA = 3;
    }

    interface SCANTRUST {
        String DATE_FORMAT = "yyyy-MM-dd";

        int SYNC_BATCH_SIZE = 100;

        interface STATUS {
            int NOT_UPDATED = 0;
            int UPDATED = 1;
            int FAILED = 2;
        }
    }

    interface CERTIFICATE {
        String DATE_FORMAT = "yyyy-MM-dd";
    }

    interface LOT_STATUS_VALUE {
        String NOT_FINALISED = "NOT_FINALISED";
        String FINALISED = "FINALISED";
        String APPROVED = "APPROVED";
    }

    interface SEND_MAIL {
        String SEND_MAIL_WINERY_ID_SUCCESS = "User is successfully created and sent a password reset email";
        String SEND_MAIL_WINERY_ID_FAIL = "User is successfully created but we were unable to send a password reset email";

  }
  enum KEYCLOAK_ROLE_NAMES {
    WINERY
  }
  String emailRegexPattern = "^[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]{1,255}\\.[A-Za-z]{2,63}$";
  
  static boolean emailFormatCorrect(String email) {
    return Pattern.compile(emailRegexPattern)
            .matcher(email)
            .matches();
  }
  enum STATUS_SEND_EMAIL_KEYCLOAK{
    UPDATE_PASSWORD
  }

  interface LAT_LONG_RANGE {
    double MAX_LATITUDE = 90;
    double MAX_LONGITUDE = 180;
  }
  interface TERMS {
    String APP_TERMS = "app_terms";
    String WEB_TERMS = "web_terms";
  }

  interface DRIVE {
    String APP = "APP";
    String WEB = "WEB";
  }
}
