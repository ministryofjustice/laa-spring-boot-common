package uk.gov.laa.springboot.oauth2;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TestApi {

  @RestController
  @RequestMapping("/resource1")
  public class Resource1Controller {

    @GetMapping("/unrestricted")
    public ResponseEntity<Void> unrestricted() {
      return noContent();
    }

    @GetMapping("/requires-group1-role")
    public ResponseEntity<Void> group1RoleGet() {
      return noContent();
    }

    @GetMapping("/requires-scope")
    public ResponseEntity<Void> scopeGet() {
      return noContent();
    }

    @GetMapping("/method-scope")
    public ResponseEntity<Void> methodScopeGet() {
      return noContent();
    }

    @PostMapping("/method-scope")
    public ResponseEntity<Void> methodScopePost() {
      return noContent();
    }

    @PatchMapping("/method-scope")
    public ResponseEntity<Void> methodScopePatch() {
      return noContent();
    }
  }

  private ResponseEntity<Void> noContent() {
    return ResponseEntity.noContent().build();
  }
}
