package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "laa.springboot.starter.oauth2.additional-security-filters=")
@AutoConfigureMockMvc
class NoAdditionalSecurityFiltersTest {

  @Autowired
  private SecurityFilterChain securityFilterChain;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void doesNotAddFiltersOrPreventRequestsWhenAdditionalSecurityFiltersIsNotConfigured()
      throws Exception {
    assertThat(securityFilterChain.getFilters())
        .anyMatch(BearerTokenAuthenticationFilter.class::isInstance)
        .noneMatch(XAuthorizationFilter.class::isInstance);

    mockMvc.perform(get("/resource1/unrestricted"))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }
}
