package org.cardanofoundation.proofoforigin.api.business;

import org.springframework.web.multipart.MultipartFile;

public interface UploadBottleBusiness {
    void uploadCsvFile(MultipartFile file, String wineryId);
}
