package org.cardanofoundation.proofoforigin.api.business;

import org.cardanofoundation.proofoforigin.api.constants.TaskState;

public interface ScantrustTaskService {
    void saveScantrustTask(String taskId, TaskState taskState, String lotId, int step);

}
