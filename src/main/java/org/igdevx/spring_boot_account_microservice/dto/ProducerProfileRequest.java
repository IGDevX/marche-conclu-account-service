package org.igdevx.spring_boot_account_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProducerProfileRequest extends BaseProfileRequest {
    private String siret;
    private String organizationType;
    private Integer installationYear;
    private Integer employeesCount;
    // Profession ids selected by the producer
    private List<Long> professionIds;
}
