package org.secretflow.secretpad.service.model.node;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author chixian
 * @date 2023/10/18
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DeleteNodeIdRequest {

    /**
     * routerId
     */
    @NotBlank
    private String routerId;

}
