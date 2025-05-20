package org.cardanofoundation.proofoforigin.api.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.proofoforigin.api.constants.TaskState;

@Entity
@Table(name = "scantrusttask")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScantrustTask {

    @Id
    @Column(name = "task_id", nullable = false)
    private String taskId;


    @Column(name = "lot_id", nullable = false)
    private String lotId;

    @Column(name = "task_state", nullable = false)
    private TaskState taskState;

    @Column(name = "step", nullable = false)
    private int step;

    public static ScantrustTask initWithId(String taskId) {
        return ScantrustTask.builder()
                .taskId(taskId)
                .build();
    }

}
