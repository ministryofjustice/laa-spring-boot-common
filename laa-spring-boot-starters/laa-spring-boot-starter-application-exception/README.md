# LAA Spring Boot Starter – Application Exception

This starter provides a reusable base exception (`ApplicationException`) and a `@ControllerAdvice`
(`GlobalExceptionHandler`) that turn those exceptions into consistent HTTP responses containing the
`error_message` and `http_status` fields.

## Usage

## Declare the dependency

To enable this in your application, declare the following:

```groovy
   dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-application-exception"
}
```

## Usage

Throw `ApplicationException` (or a subclass) from your controllers or services when you want to
return a specific HTTP status and message to the client. The starter will automatically translate
the exception into a JSON response body:

   ```json
   {
     "errorMessage": "Something went wrong",
     "httpStatus": 400
   }
   ```

## Creating Specific Exceptions

You can extend `ApplicationException` to model domain-specific errors while keeping the shared
behaviour:

```java
package uk.gov.justice.laa.some.other.project.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import uk.gov.laa.springboot.exception.ApplicationException;

public class SomeException extends ApplicationException {
  public SomeException(String message) {
    super(message, BAD_REQUEST);
  }
}
```

Throwing `SomeException` from your application will produce a `400 Bad Request`
response with the supplied message and the standard error payload. Create additional subclasses for
other HTTP statuses as required.