package org.cardanofoundation.metabus.testhelper;

import lombok.SneakyThrows;
import org.apache.commons.validator.routines.UrlValidator;
import org.cardanofoundation.metabus.constants.TestConstants;
import org.cardanofoundation.metabus.util.MinioUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class MinioHelper {

    @SneakyThrows
    public static void putNormalJsonOnStorage(MinioUtil minioUtil) {
        byte[] jsonBytes = TestConstants.CANONICALIZED_NORMAL.getBytes();
        InputStream inputStream = new ByteArrayInputStream(jsonBytes);
        minioUtil.makeBucket(TestConstants.BUCKET_NAME);
        minioUtil.storeObject(TestConstants.BUCKET_NAME, TestConstants.CID_NORMAL, inputStream);
    }

    @SneakyThrows
    public static void putCrazyJsonOnStorage(MinioUtil minioUtil) {
        byte[] jsonBytes = TestConstants.CANONICALIZED_CRAZY.getBytes();
        InputStream inputStream = new ByteArrayInputStream(jsonBytes);
        minioUtil.makeBucket(TestConstants.BUCKET_NAME);
        minioUtil.storeObject(TestConstants.BUCKET_NAME, TestConstants.CID_CRAZY, inputStream);
    }
}
