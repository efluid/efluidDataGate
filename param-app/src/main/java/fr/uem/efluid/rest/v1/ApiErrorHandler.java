package fr.uem.efluid.rest.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackageClasses = ApplicationApiController.class)
public class ApiErrorHandler {

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ExceptionView> handleOtherException(Exception ex) {
        return new ResponseEntity<>(new ExceptionView(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class ExceptionView {
        private final String message;

        public ExceptionView(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
