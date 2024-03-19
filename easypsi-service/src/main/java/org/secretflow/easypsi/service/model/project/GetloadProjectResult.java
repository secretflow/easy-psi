package org.secretflow.easypsi.service.model.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Download data request
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetloadProjectResult {

    /**
     * hash
     */
    @Schema(description = "hash")
    private String hash;
}
