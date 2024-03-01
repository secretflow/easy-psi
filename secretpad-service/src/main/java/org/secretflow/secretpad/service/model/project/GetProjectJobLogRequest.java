package org.secretflow.secretpad.service.model.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chixian
 * @date 2023/11/6
 */
@Setter
@Getter
public class GetProjectJobLogRequest {
    /**
     * Job id, it can not be blank
     */
    @Schema(description = "job id")
    @NotBlank
    private String jobId;
}
