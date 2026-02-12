package uk.gov.laa.springboot.auth;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@SpringBootApplication
public class TestAPI {

    @RestController
    @RequestMapping(value = "/resource1")
    public class TestResource1Controller {

        @GetMapping(path = "/unrestricted")
        public ResponseEntity<?> returnUnrestricted() {
            return noContent();
        }

        @GetMapping(path = "/restricted")
        public ResponseEntity<?> returnRestricted() {
            return noContent();
        }

        @GetMapping(path = "/requires-group1-role")
        public ResponseEntity<?> returnGroup1RoleRestricted() {
            return noContent();
        }

        @PostMapping(path = "/requires-group1-role")
        public ResponseEntity<?> returnGroup1RoleRestrictedPost() {
            return noContent();
        }

        @GetMapping(path = "/requires-group2-role")
        public ResponseEntity<?> returnGroup2RoleRestricted() {
            return noContent();
        }

        @GetMapping(path = "/method-specific")
        public ResponseEntity<?> returnMethodSpecificGet() {
            return noContent();
        }

        @PostMapping(path = "/method-specific")
        public ResponseEntity<?> returnMethodSpecificPost() {
            return noContent();
        }

        @GetMapping(path = "/method-array")
        public ResponseEntity<?> returnMethodArrayGet() {
            return noContent();
        }

        @PostMapping(path = "/method-array")
        public ResponseEntity<?> returnMethodArrayPost() {
            return noContent();
        }

        @PatchMapping(path = "/method-array")
        public ResponseEntity<?> returnMethodArrayPatch() {
            return noContent();
        }

        @GetMapping(path = "/stream-requires-group1-role", produces = "text/plain")
        public ResponseEntity<StreamingResponseBody> streamGroup1RoleRestricted() {
            StreamingResponseBody body =
                outputStream -> outputStream.write("stream-ok".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.ok(body);
        }

    }

    @RestController
    @RequestMapping(value = "/resource2")
    public class TestResource2Controller {

        @GetMapping(path = "/requires-group2-role")
        public ResponseEntity<?> returnGroup2RoleRestricted() {
            return noContent();
        }

    }

    @RestController
    @RequestMapping(value = "/resource3")
    public class TestResource3Controller {

        @GetMapping(path = "/specific")
        public ResponseEntity<?> returnSpecificRoleRestricted() {
            return noContent();
        }

    }

    private ResponseEntity<?> noContent() {
        return ResponseEntity.noContent().build();
    }
}
