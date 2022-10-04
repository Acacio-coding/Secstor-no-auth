package com.ifsc.secstor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.NULL_ALGORITHM;
import static com.ifsc.secstor.api.util.Constants.*;

@Getter
@Setter
public class SplitDTO {

    @Schema(description = SPLIT_DATA_DTO_DESCRIPTION, example = SPLIT_DATA_DTO_EXAMPLE, required = true)
    private Object data;

    @NotBlank(message = NULL_ALGORITHM)
    @Schema(description = SPLIT_ALGORITHM_DESCRIPTION, example = SHAMIR , required = true)
    private String algorithm;
}
