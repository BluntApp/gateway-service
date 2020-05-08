package com.blunt.gateway.error;

import com.blunt.gateway.util.BluntConstant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class BluntExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(BluntExceptionHandler.class);
  @ExceptionHandler(value = BluntException.class)
  public final ResponseEntity<Object> handleBluntException(BluntException exception) {
    LOGGER.error(exception.getMessage());
    BluntError bluntError = prepareBluntError(exception);
    return new ResponseEntity<Object>(bluntError, exception.getStatus());
  }

  @ExceptionHandler(value = Exception.class)
  public final ResponseEntity<Object> handleException(Exception exception) {
    LOGGER.error(exception.getMessage());
    BluntError bluntError = prepareBluntError(exception);
    return new ResponseEntity<Object>(bluntError, HttpStatus.UNAUTHORIZED);
  }

  private BluntError prepareBluntError(Exception ex) {
    BluntError bluntError = new BluntError();
    bluntError.setTimestamp(LocalDateTime.now());
    bluntError.setMessage(ex.getMessage());
    if (ex instanceof MethodArgumentNotValidException) {
      fillValidationError((MethodArgumentNotValidException) ex, bluntError);
    }
    return bluntError;
  }

  private void fillValidationError(MethodArgumentNotValidException ex, BluntError bluntError) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(x -> x.getDefaultMessage())
        .collect(Collectors.toList());
    bluntError.setMessage(BluntConstant.VALIDATION_ERROR);
    bluntError.setErrors(errors);
  }

}
