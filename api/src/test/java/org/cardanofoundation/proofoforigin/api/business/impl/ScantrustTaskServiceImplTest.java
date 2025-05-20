package org.cardanofoundation.proofoforigin.api.business.impl;

import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.cardanofoundation.proofoforigin.api.repository.ScantrustTaskRepository;
import org.cardanofoundation.proofoforigin.api.repository.entities.ScantrustTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScantrustTaskServiceImplTest {

    @Mock
    ScantrustTaskRepository scantrustTaskRepository;
    ScantrustTaskServiceImpl scantrustTaskService;

    @BeforeEach
    void setUp() {
        scantrustTaskService = new ScantrustTaskServiceImpl(scantrustTaskRepository);
    }


    @Test
    void saveScantrustTask() {
        String taskId = "taskId";
        String lotId = "lotId";

        ScantrustTask scantrustTask = new ScantrustTask();
        scantrustTask.setTaskId(taskId);
        scantrustTask.setLotId(lotId);
        scantrustTask.setTaskState(TaskState.COMPLETED);

        when(scantrustTaskRepository.findById(taskId)).thenReturn(Optional.of(scantrustTask));
        when(scantrustTaskRepository.save(any(ScantrustTask.class))).thenAnswer(invocationOnMock -> {
            ScantrustTask scantrustTask1 = invocationOnMock.getArgument(0);
            assertEquals(taskId, scantrustTask1.getTaskId());
            assertEquals(lotId, scantrustTask1.getLotId());
            assertEquals(TaskState.COMPLETED, scantrustTask1.getTaskState());
            return scantrustTask1;
        });

        scantrustTaskService.saveScantrustTask(taskId, TaskState.COMPLETED, lotId, 2);
        verify(scantrustTaskRepository, times(1)).save(any(ScantrustTask.class));

    }

    @Test
    void saveScantrustTask_whenCannotFindTask() {
        String taskId = "taskId";
        String lotId = "lotId";

        when(scantrustTaskRepository.findById(taskId)).thenReturn(Optional.empty());
        when(scantrustTaskRepository.save(any(ScantrustTask.class))).thenAnswer(invocationOnMock -> {
            ScantrustTask scantrustTask1 = invocationOnMock.getArgument(0);
            assertEquals(taskId, scantrustTask1.getTaskId());
            assertEquals(lotId, scantrustTask1.getLotId());
            assertEquals(TaskState.COMPLETED, scantrustTask1.getTaskState());
            return scantrustTask1;
        });

        scantrustTaskService.saveScantrustTask(taskId, TaskState.COMPLETED, lotId, 2);
        verify(scantrustTaskRepository, times(1)).save(any(ScantrustTask.class));

    }
}