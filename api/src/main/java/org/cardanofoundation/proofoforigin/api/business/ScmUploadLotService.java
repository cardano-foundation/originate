package org.cardanofoundation.proofoforigin.api.business;

import org.springframework.web.multipart.MultipartFile;

public interface ScmUploadLotService
{
    void uploadCsvFile(MultipartFile file, String wineryId);
}
