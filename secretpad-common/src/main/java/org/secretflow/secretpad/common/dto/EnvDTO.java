package org.secretflow.secretpad.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.secretflow.secretpad.common.constant.PlatformType;

/**
 * environment dto
 */
@Setter
@Getter
@ToString
public class EnvDTO {
    PlatformType platformType;
    String platformNodeId;
}
