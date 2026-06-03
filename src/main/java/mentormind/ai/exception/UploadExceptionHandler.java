package mentormind.ai.exception;

import jakarta.validation.ConstraintViolationException;
import mentormind.ai.rag.MentorMindRagService.EmptyFileNameException;
import mentormind.ai.rag.MentorMindRagService.IngestionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> tipoInvalido(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> tamanhoExcedido(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Arquivo excede o tamanho permitido");
    }

    @ExceptionHandler(EmptyFileNameException.class)
    public ResponseEntity<String> nomeVazio(EmptyFileNameException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IngestionException.class)
    public ResponseEntity<String> falhaIngestion(IngestionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

}
