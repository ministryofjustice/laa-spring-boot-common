package uk.gov.laa.springboot.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleApplicationExceptionReturnsExpectedResponseBodyAndStatus() {
    ApplicationException exception =
        new ApplicationException("Something went wrong", HttpStatus.BAD_REQUEST);

    ResponseEntity<ApplicationException> response = handler.handleApplicationException(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isInstanceOf(ApplicationException.class);
  }
}