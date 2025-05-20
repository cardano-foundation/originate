package org.cardanofoundation.proofoforigin.api.business.impl;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.proofoforigin.api.business.ScantrustTaskService;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.cardanofoundation.proofoforigin.api.repository.ScantrustTaskRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.ScantrustTask;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScantrustTaskServiceImpl implements ScantrustTaskService {

    private final ScantrustTaskRepository scantrustTaskRepository;

    @Override
    public void saveScantrustTask(String taskId, TaskState taskState, String lotId, int step) {
        ScantrustTask scantrustTask = scantrustTaskRepository.findById(taskId)
                .orElse(ScantrustTask.initWithId(taskId));
        scantrustTask.setLotId(lotId);
        scantrustTask.setTaskState(taskState);
        scantrustTask.setStep(step);
        scantrustTaskRepository.save(scantrustTask);
    }
}
