package org.secretflow.easypsi.service.model.graph;

import lombok.*;
import java.util.List;

/**
 * @author chixian
 * @date 2023/10/30
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GrapDataTableVO {

    /**
     * csv data Table
     */
    private List<String> dataTable;

    /**
     * check date exist
     */
    private boolean result;
}
