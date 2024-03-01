package org.secretflow.secretpad.service.model.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import java.util.List;

/**
 * @author chixian
 * @date 2023/11/7
 */
@Data
@AllArgsConstructor
public class GraphNodeJobLogsVO {
    /**
     * Graph node task status
     */
    private GraphJobStatus status;
    /**
     * Task logs
     */
    private List<String> logs;
}
