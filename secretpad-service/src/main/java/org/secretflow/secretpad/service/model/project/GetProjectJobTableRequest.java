package org.secretflow.secretpad.service.model.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chixian
 * @date 2023/10/30
 */
@Getter
@Setter
public class GetProjectJobTableRequest {
    /**
     * Project job csv data table name
     */
    @Schema(description = "csv data table name")
    private String tableName;

    /**
     * Check Project job csv data table
     */
    @Schema(description = "check csv data table exist")
    private boolean checkTableExist = false;
}
