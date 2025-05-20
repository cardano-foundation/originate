package org.cardanofoundation.proofoforigin.api.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.quartz.JobBuilder.newJob;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptor {
    private String name;
    private String group;
    private JobDataMap jobDataMap;
    private List<TriggerDescriptor> triggerDescriptors;
    private Class<? extends Job> jobClass;

    /**
     * Convenience method that builds a descriptor from JobDetail and Trigger(s)
     *
     * @param jobDetail     the JobDetail instance
     * @param triggersOfJob the Trigger(s) to associate with the Job
     * @return the JobDescriptor
     */
    public static JobDescriptor buildDescriptor(JobDetail jobDetail, List<? extends Trigger> triggersOfJob) {
        List<TriggerDescriptor> triggerDescriptors = new ArrayList<>();

        for (Trigger trigger : triggersOfJob) {
            triggerDescriptors.add(TriggerDescriptor.buildDescriptor(trigger));
        }

        return JobDescriptor.builder()
                .jobClass(jobDetail.getJobClass())
                .name(jobDetail.getKey().getName())
                .group(jobDetail.getKey().getGroup())
                .jobDataMap(jobDetail.getJobDataMap())
                .triggerDescriptors(triggerDescriptors)
                .build();
    }

    /**
     * Convenience method for building Triggers of Job
     *
     * @return Triggers for this JobDetail
     */
    @JsonIgnore
    public Set<Trigger> buildTriggers() {
        Set<Trigger> triggers = new LinkedHashSet<>();
        for (TriggerDescriptor triggerDescriptor : triggerDescriptors) {
            triggers.add(triggerDescriptor.buildTrigger());
        }
        return triggers;
    }

    /**
     * Convenience method that builds a JobDetail
     *
     * @return the JobDetail built from this descriptor
     */
    public JobDetail buildJobDetail(JobDataMap jobDataMap) {
        return newJob(jobClass)
                .withIdentity(getName(), getGroup())
                .usingJobData(jobDataMap)
                .build();
    }
}
