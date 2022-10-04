package com.ifsc.secstor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static com.ifsc.secstor.api.util.Constants.*;

@Getter
@Setter
public class ReconstructDTO {

    @Schema(description = RECONSTRUCT_PARAMETERS_DTO_DESCRIPTION, example = RECONSTRUCT_PARAMETERS_DTO_EXAMPLE)
    private Object parameters;

    @Schema(description = RECONSTRUCT_SECRET_DTO_DESCRIPTION, example = RECONSTRUCT_SECRET_DTO_EXAMPLE, required = true)
    private Object secret;
}
