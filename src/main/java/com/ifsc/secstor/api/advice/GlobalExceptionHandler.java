package com.ifsc.secstor.api.advice;

import com.ifsc.secstor.api.advice.exception.*;
import com.ifsc.secstor.api.model.ErrorModel;
import com.ifsc.secstor.api.model.ReconstructErrorModel;
import com.ifsc.secstor.api.model.ValidationErrorModel;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        List<String> details = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> details.add(error.getDefaultMessage()));

        var error = new ValidationErrorModel(status.value(), "Validation Error",
                details.toString().replaceAll("[\\[\\]]", ""),
                request.getDescription(false).substring("uri=".length()));

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(ValidationException.class)
    private ResponseEntity<ErrorModel> handleValidationException(ValidationException exception) {
        var error = new ValidationErrorModel(exception.getStatus().value(), exception.getTitle(),
                exception.getMessage(), exception.getPath());
        return new ResponseEntity<>(error, exception.getStatus());
    }

    @ExceptionHandler(ReconstructException.class)
    private ResponseEntity<ReconstructErrorModel> handleReconstructException(ReconstructException exception) {
        var error = new ReconstructErrorModel(exception.getStatus().value(), exception.getTitle(),
                exception.getMessage(), exception.getPath(), exception.getKeyIndex(), exception.getKey(), exception.getType());
        return new ResponseEntity<>(error, exception.getStatus());
    }
}
