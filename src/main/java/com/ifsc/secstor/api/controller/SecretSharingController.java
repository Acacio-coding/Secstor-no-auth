package com.ifsc.secstor.api.controller;

import com.at.archistar.crypto.data.InvalidParametersException;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ifsc.secstor.api.config.SecstorConfig;
import com.ifsc.secstor.api.dto.ReconstructDTO;
import com.ifsc.secstor.api.dto.SplitDTO;
import com.ifsc.secstor.api.service.SecretSharingImplementation;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static com.ifsc.secstor.api.advice.paths.Paths.*;
import static com.ifsc.secstor.api.util.Constants.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(SECRET_SHARING_BASE)
@RequiredArgsConstructor
public class SecretSharingController {

    private final SecretSharingImplementation secretSharingService;

    @PostMapping(SECRET_SHARING_SPLIT)
    @Operation(summary = SPLIT_TITLE, description =  SPLIT_DESCRIPTION, tags = SECRET_SHARING_TAG)
    @ApiResponses(value = {
            @ApiResponse(responseCode = HTTP_200_CODE, description = HTTP_200_DESCRIPTION,
                    content = @Content(schema = @Schema(example = SPLIT_RESPONSE_SUCCESS))),
            @ApiResponse(responseCode = HTTP_400_CODE, description = HTTP_400_DESCRIPTION,
                    content = @Content(schema = @Schema(example = SPLIT_RESPONSE_ERROR)))
    })
    public ResponseEntity<Object> split(@RequestBody @Validated SplitDTO splitDTO, HttpServletRequest request) throws UnsupportedEncodingException,
            InvalidVSSScheme {
        return ResponseEntity.status(HttpStatus.OK).body(this.secretSharingService.split(splitDTO));
    }

    @PostMapping(SECRET_SHARING_RECONSTRUCT)
    @Operation(summary = RECONSTRUCT_TITLE, description = RECONSTRUCT_DESCRIPTION, tags = SECRET_SHARING_TAG)
    @ApiResponses(value = {
            @ApiResponse(responseCode = HTTP_200_CODE, description = HTTP_200_DESCRIPTION,
                    content = @Content(schema = @Schema(example = RECONSTRUCT_RESPONSE_SUCCESS))),
            @ApiResponse(responseCode = HTTP_400_CODE, description = HTTP_400_DESCRIPTION,
                    content = @Content(schema = @Schema(example = RECONSTRUCT_RESPONSE_ERROR)))
    })
    public ResponseEntity<Object> reconstruct(@RequestBody ReconstructDTO reconstructDTO)
            throws UnsupportedEncodingException, InvalidParametersException, InvalidVSSScheme, ReconstructionException,
            JsonProcessingException {
        return ResponseEntity.status(HttpStatus.OK).body(this.secretSharingService.reconstruct(reconstructDTO));
    }
}
