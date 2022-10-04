package com.ifsc.secstor.api.controller;

import com.ifsc.secstor.api.dto.AnonymizationDTO;
import com.ifsc.secstor.api.service.AnonymizationServiceImplementation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.ifsc.secstor.api.advice.paths.Paths.DATA_ANONYMIZATION_ANONYMIZE;
import static com.ifsc.secstor.api.advice.paths.Paths.DATA_ANONYMIZATION_BASE;
import static com.ifsc.secstor.api.util.Constants.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(DATA_ANONYMIZATION_BASE)
public class AnonymizationController {

    private final AnonymizationServiceImplementation anonymizationService;

    public AnonymizationController() {
        this.anonymizationService = new AnonymizationServiceImplementation();
    }

    @PostMapping(DATA_ANONYMIZATION_ANONYMIZE)
    @Operation(summary = ANONYMIZATION_TITLE, description = ANONYMIZATION_DESCRIPTION, tags = ANONYMIZATION_TAG)
    @ApiResponses(value = {
            @ApiResponse(responseCode = HTTP_200_CODE, description = HTTP_200_DESCRIPTION,
                    content = @Content(schema = @Schema(example = ANONYMIZATION_RESPONSE_SUCCESS))),
            @ApiResponse(responseCode = HTTP_400_CODE, description = HTTP_400_DESCRIPTION,
                    content = @Content(schema = @Schema(example = ANONYMIZATION_RESPONSE_ERROR)))
    })
    public ResponseEntity<Object> anonymize(@RequestBody AnonymizationDTO anonymizationDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(this.anonymizationService.anonymize(anonymizationDTO));
    }
}
