package org.cardanofoundation.proofoforigin.api.job;

import org.cardanofoundation.proofoforigin.api.business.JobService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmTaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckTaskStateJobTest {


    @Mock
    ScanTrustService scanTrustService;
    @Mock
    JobService jobService;
    @Mock
    JobExecutionContext jobExecutionContext;
    CheckTaskStateJob checkTaskStateJob;
    JobDataMap jobDataMap;

    @BeforeEach
    void setUp() {
        jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", "taskId");
        jobDataMap.put("lotId", "lotId");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        checkTaskStateJob = new CheckTaskStateJob(scanTrustService, jobService);
    }


    @Test
    void execute_whenTaskComplete() {
        String taskId = jobDataMap.getString("taskId");
        String lotId = jobDataMap.getString("lotId");
        ScmTaskResponse scmTaskResponseMock = new ScmTaskResponse();
        scmTaskResponseMock.setState(TaskState.COMPLETED);
        when(scanTrustService.checkTaskState(taskId, lotId)).thenReturn(scmTaskResponseMock);

        JobDetail jobDetail = JobBuilder.newJob()
                .ofType(CheckTaskStateJob.class)
                .storeDurably()
                .usingJobData(jobDataMap)
                .withIdentity("Qrtz_Job_Detail_" + taskId)
                .withDescription("Job send check status send SCM data to Scan Trust")
                .build();
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        JobKey jobKey = jobDetail.getKey();
        jobService.deleteJob(jobKey.getGroup(), jobKey.getName());

        checkTaskStateJob.execute(jobExecutionContext);
        verify(scanTrustService, times(1)).checkTaskState(any(String.class), any(String.class));
    }

    @Test
    void execute_whenTaskIncomplete() {
        String taskId = jobDataMap.getString("taskId");
        String lotId = jobDataMap.getString("lotId");
        ScmTaskResponse scmTaskResponseMock = new ScmTaskResponse();
        scmTaskResponseMock.setState(TaskState.PENDING);
        when(scanTrustService.checkTaskState(taskId, lotId)).thenReturn(scmTaskResponseMock);

        checkTaskStateJob.execute(jobExecutionContext);
        verify(scanTrustService, times(1)).checkTaskState(any(String.class), any(String.class));
        verify(jobService, times(0)).deleteJob(any(String.class), any(String.class));
    }

    @Test
    void execute_whenResponseNull() {
        String taskId = jobDataMap.getString("taskId");
        String lotId = jobDataMap.getString("lotId");

        ScmTaskResponse scmTaskResponseMock = new ScmTaskResponse();
        scmTaskResponseMock.setState(TaskState.COMPLETED);

        when(scanTrustService.checkTaskState(taskId, lotId)).thenReturn(null);

        checkTaskStateJob.execute(jobExecutionContext);

        verify(scanTrustService, times(1)).checkTaskState(taskId, lotId);
    }
}