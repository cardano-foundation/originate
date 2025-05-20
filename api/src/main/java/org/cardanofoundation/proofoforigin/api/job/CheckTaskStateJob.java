package org.cardanofoundation.proofoforigin.api.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.proofoforigin.api.business.JobService;
import org.cardanofoundation.proofoforigin.api.business.ScanTrustService;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;
import org.cardanofoundation.proofoforigin.api.controllers.dtos.scan_trust.ScmTaskResponse;
import org.quartz.*;

@Slf4j
@RequiredArgsConstructor
public class CheckTaskStateJob implements Job {

    private final ScanTrustService scanTrustService;
    private final JobService jobService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Job triggered to check status");
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String taskId = map.getString("taskId");
        String lotId = map.getString("lotId");
        ScmTaskResponse scmTaskResponse = scanTrustService.checkTaskState(taskId, lotId);
        if (scmTaskResponse == null) {
            return;
        }
        TaskState taskState = scmTaskResponse.getState();
        if (TaskState.COMPLETED == taskState) {
            JobDetail jobDetail = jobExecutionContext.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            jobService.deleteJob(jobKey.getGroup(), jobKey.getName());
            log.info("Delete job with name {} group {} after completed!", jobKey.getName(), jobKey.getGroup());
        } else {
            log.info("Task status return {}", taskState);
        }
    }
}
