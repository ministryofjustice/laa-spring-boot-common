package uk.gov.laa.ccms.springboot.auth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        @GetMapping(path = "/requires-group2-role")
        public ResponseEntity<?> returnGroup2RoleRestricted() {
            return noContent();
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

    private ResponseEntity<?> noContent() {
        return ResponseEntity.noContent().build();
    }
}
