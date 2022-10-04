package com.ifsc.secstor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static com.ifsc.secstor.api.util.Constants.*;

@Getter
@Setter
public class AnonymizationDTO {

    @Schema(description = ANONYMIZATION_DTO_GEN_LEVEL_DESC, example = ANONYMIZATION_DTO_GEN_LEVEL_EXAMPLE)
    private int generalization_level;

    @Schema(description = ANONYMIZATION_DTO_ATTR_CONFIG_DESC, example = ANONYMIZATION_DTO_ATTR_CONFIG_EXAMPLE,
            required = true)
    private Object attribute_config;

    @Schema(description = ANONYMIZATION_DTO_DATA_DESCRIPTION, example = ANONYMIZATION_DATA_LEVEL_EXAMPLE,
            required = true)
    private Object[] data;
}
