package info.agrueneberg.fhir.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import info.agrueneberg.fhir.exceptions.AccessDeniedException;
import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler({MissingServletRequestParameterException.class, JsonMappingException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<String> handle400(Exception ex) {
        return new ResponseEntity<String>("Bad Request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handle403(Exception ex) {
        return new ResponseEntity<String>("Forbidden", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handle404(Exception ex) {
        return new ResponseEntity<String>("Not Found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> handle406(Exception ex) {
        return new ResponseEntity<String>("Not Acceptable", HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(DeletedException.class)
    public ResponseEntity<String> handle410(Exception ex) {
        return new ResponseEntity<String>("Gone", HttpStatus.GONE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle500(Exception ex) {
        logger.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<String>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}