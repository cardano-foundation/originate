package org.cardanofoundation.proofoforigin.api.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@UtilityClass
public class BatchUtil {
    /**
     * Consume collection data per batch size
     */
    public static <T> void doBatching(int batchSize,
                                      List<T> collection,
                                      Consumer<List<T>> consumer) {
        if (collection == null || collection.isEmpty()) {
            return;
        }

        log.info("Start batch processing with collection size: [{}], batchSize: [{}]", collection.size(), batchSize);

        final int COLLECTION_SIZE = collection.size();
        Date startTime = new Date();
        for (int startBatchIdx = 0; startBatchIdx < COLLECTION_SIZE; startBatchIdx += batchSize) {
            int endBatchIdx = Math.min(startBatchIdx + batchSize, COLLECTION_SIZE);
            log.info("batch processing from element number: {} - {}", startBatchIdx, endBatchIdx - 1);
            final List<T> batchList = collection.subList(startBatchIdx, endBatchIdx);
            consumer.accept(batchList);
        }
        Date endTime = new Date();
        log.info("Completed batch processing in: {} ms", (endTime.getTime() - startTime.getTime()));
    }
}
