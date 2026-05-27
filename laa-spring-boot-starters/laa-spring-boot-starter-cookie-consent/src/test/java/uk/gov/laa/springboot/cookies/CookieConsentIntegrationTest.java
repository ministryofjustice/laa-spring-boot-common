//package uk.gov.laa.springboot.cookies;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = CookieConsentIntegrationTest.TestApplication.class, properties = { "laa.springboot.start.cookies-consent.enabled=true"})
//@AutoConfigureMockMvc
//public class CookieConsentIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void cookiesCanBeSaved() throws Exception {
//        mockMvc.perform(post("/cookies/preferences").param("analytics", "true")).andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    void cookiesPOlicyIsCreated() throws Exception {
//        mockMvc.perform(post("/cookies/preferences").param("analytics", "true")).andExpect(cookie().exists("cookies_policy"));
//    }
//
//    @SpringBootApplication
//    @Import(CookieConsentAutoConfiguration.class)
//    static class TestApplication {
//    }
//}
