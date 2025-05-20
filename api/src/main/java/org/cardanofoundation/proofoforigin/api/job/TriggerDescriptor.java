package org.cardanofoundation.proofoforigin.api.job;

import lombok.*;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import static java.util.UUID.randomUUID;
import static org.quartz.TriggerBuilder.newTrigger;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDescriptor {

    private String name;
    private String group;
    private ScheduleBuilder<?> scheduleBuilder;

    /**
     * @param trigger the Trigger used to build this descriptor
     * @return the TriggerDescriptor
     */
    public static TriggerDescriptor buildDescriptor(Trigger trigger) {
        return TriggerDescriptor.builder()
                .name(trigger.getKey().getName())
                .group(trigger.getKey().getGroup())
                .scheduleBuilder(trigger.getScheduleBuilder())
                .build();
    }

    private String buildName() {
        return name.isEmpty() ? randomUUID().toString() : name;
    }

    /**
     * Convenience method for building a Trigger
     *
     * @return the Trigger associated with this descriptor
     */
    public Trigger buildTrigger() {
        if (scheduleBuilder != null) {
            return newTrigger()
                    .withIdentity(buildName(), group)
                    .withSchedule(scheduleBuilder)
                    .build();
        }
        throw new IllegalStateException("Unsupported trigger descriptor " + this);
    }
}
