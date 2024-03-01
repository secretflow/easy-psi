package org.secretflow.secretpad.service.model.noderoute;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.secretflow.secretpad.service.constant.Constants;

/**
 * @author chixian
 * @date 2023/10/19
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RouterAddressRequest {

    /**
     * address
     */
    @Schema(description = "netAddress")
    @NotBlank
    @Pattern(regexp = Constants.IP_PORT_PATTERN, message = "address not support rule")
    private String netAddress;
}
