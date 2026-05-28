package uk.gov.laa.springboot.cookies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class CookieConsentControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setuUp(){
        CookieConsentProperties properties = new CookieConsentProperties();
        CookieConsentController consentController = new CookieConsentController(properties);
        mockMvc = MockMvcBuilders.standaloneSetup(consentController).build();
    }

    @Test
    void cookiesPageLoads() throws Exception {
        mockMvc.perform(get("/cookies")).andExpect(status().isOk()).andExpect(view().name("pages/cookies"));
    }


    @Test
    void cookiesPageLoadsWithSuccessParam() throws Exception {
        mockMvc.perform(get("/cookies").param("analytics", "true")).andExpect(status().isOk()).andExpect(view().name("pages/cookies"));
    }

    @Test
    void rejectAnalyticsCookies() throws Exception {
        mockMvc.perform(post("/cookies/preferences").param("analytics", "false")).andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/cookies?success=true"));
    }

    @Test
    void setCookiesPolicyCookie() throws Exception {
        mockMvc.perform(post("/cookies/preferences").param("analytics", "true")).andExpect(cookie().exists("cookies_policy"));
    }

    @Test
    void redirectsAfterSavingPreferences() throws Exception {
        mockMvc.perform(post("/cookies/preferences").param("analytics", "true")).andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/cookies?success=true"));
    }

    @Test
    void redirectstoRefererWhenPresent() throws Exception {
        mockMvc.perform(post("/cookies/preferences").header("Referer", "/home").param("analytics", "true")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cookies?success=true"));
    }

    @Test
    void fallsBackWhenNoRefererPresent() throws Exception {
        mockMvc.perform(post("/cookies/preferences").param("analytics", "true")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cookies?success=true"));
    }

}
