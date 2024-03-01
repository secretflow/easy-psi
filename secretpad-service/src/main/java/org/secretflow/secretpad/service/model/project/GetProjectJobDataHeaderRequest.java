package org.secretflow.secretpad.service.model.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * @author chixian
 * @date 2023/10/31
 */
@Getter
@Setter
public class GetProjectJobDataHeaderRequest {
    /**
     * Project job csv data table name
     */
    @Schema(description = "check csv data table header")
    private List<String> checkTableHeader;

    /**
     * Project job csv data table name
     */
    @Schema(description = "csv data table name")
    @NotBlank
    private String tableName;

    /**
     * Check project job csv data table header
     */
    @Schema(description = "check csv data data header exist")
    private boolean checkDataHeaderExist= false;
}
