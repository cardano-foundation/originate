package org.cardanofoundation.metabus.integrationtest.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class NotJob implements Serializable {
    private String name;
}