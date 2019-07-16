package fr.uem.efluid.rest.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.uem.efluid.utils.ApplicationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order
@RestControllerAdvice(basePackageClasses = ApplicationApiController.class)
public class ApiErrorHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ExceptionView> handleApplicationException(ApplicationException ex) {
        return new ResponseEntity<>(new ExceptionView(ex), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionView> handleAllException(Throwable ex) {
        return new ResponseEntity<>(new ExceptionView(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Rendering of exception on Rest API controllers
     */
    @JsonInclude(content = JsonInclude.Include.NON_NULL)
    public static class ExceptionView {

        private final String message;

        private final String payload;

        private final String errorType;

        private final long timestamp;


        ExceptionView(Throwable t) {
            this.message = t.getMessage();
            this.payload = "at " + t.getStackTrace()[0].toString();
            this.errorType = t.getClass().getSimpleName().toUpperCase();
            this.timestamp = System.currentTimeMillis();
        }

        ExceptionView(ApplicationException t) {
            this.message = t.getMessage();
            this.payload = t.getPayload();
            this.errorType = t.getError().name();
            this.timestamp = t.getTimestamp();
        }

        public String getMessage() {
            return message;
        }

        public String getPayload() {
            return payload;
        }

        public String getErrorType() {
            return errorType;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
