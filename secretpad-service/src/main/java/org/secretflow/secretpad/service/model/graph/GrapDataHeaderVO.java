package org.secretflow.secretpad.service.model.graph;

import lombok.*;
import java.util.List;

/**
 * @author chixian
 * @date 2023/10/24
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GrapDataHeaderVO {

    /**
     * csv data table name
     */
    private String tableName;

    /**
     * csv data table header
     */
    private List<String> dataHeader;

    /**
     * check date header exist
     */
    private boolean result;

    /**
     * csv data exist header
     */
    private List<String> existHeader;

    /**
     * csv data absent header
     */
    private List<String> absentHeader;

}
