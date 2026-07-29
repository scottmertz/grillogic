package grillogic.controller.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catches our own "not found" / "invalid login" style RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (message != null) {
            if (message.contains("Invalid email or password")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (message.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("already registered")) {
                status = HttpStatus.CONFLICT;
            }
        }

        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    // Catches database-level constraint violations, like the ingredient FK delete issue
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "This action conflicts with existing data (e.g. still referenced elsewhere)."));
    }
}