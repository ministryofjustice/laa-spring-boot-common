package uk.gov.laa.ccms.springboot.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiEndpointAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testNonExistentEndpointUnauthorized() throws Exception {
        mockMvc.perform(get("/resource1/restricted/invalid"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testInvalidUnrestrictedEndpointNotFound() throws Exception {
        mockMvc.perform(get("/resource1/unrestricted/invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testRestrictedEndpointInvalidTokenUnauthorized() throws Exception {
        mockMvc.perform(get("/resource1/restricted").header(HttpHeaders.AUTHORIZATION, "invalid-token"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testRestrictedEndpointInvalidRoleForbidden() throws Exception {
        mockMvc.perform(get("/resource1/restricted").header(HttpHeaders.AUTHORIZATION, "b7bbdb3d-d0b9-4632-b752-b2e0f9486baf"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testGroup1EndpointValidTokenAuthorized() throws Exception {
        mockMvc.perform(get("/resource1/requires-group1-role").header(HttpHeaders.AUTHORIZATION, "b7bbdb3d-d0b9-4632-b752-b2e0f9486baf"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testGroup1EndpointInvalidRoleForbidden() throws Exception {
        mockMvc.perform(get("/resource1/requires-group1-role").header(HttpHeaders.AUTHORIZATION, "1fd84ad9-760d-401f-8cf0-7a80aa42566c"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testGroup2EndpointValidTokenAuthorized() throws Exception {
        mockMvc.perform(get("/resource1/requires-group2-role").header(HttpHeaders.AUTHORIZATION, "1fd84ad9-760d-401f-8cf0-7a80aa42566c"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testGroup2EndpointInvalidRoleForbidden() throws Exception {
        mockMvc.perform(get("/resource1/requires-group2-role").header(HttpHeaders.AUTHORIZATION, "b7bbdb3d-d0b9-4632-b752-b2e0f9486baf"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testUnrestrictedEndpointNoTokenAuthorized() throws Exception {
        mockMvc.perform(get("/resource1/unrestricted"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testClientWithMultipleRolesAuthorized() throws Exception {
        mockMvc.perform(get("/resource1/requires-group1-role").header(HttpHeaders.AUTHORIZATION, "5d925478-a8a2-4b76-863a-3fb87dcbcb95"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/resource2/requires-group2-role").header(HttpHeaders.AUTHORIZATION, "5d925478-a8a2-4b76-863a-3fb87dcbcb95"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testRoleWithMultipleEndpointsAuthorized() throws Exception {
        mockMvc.perform(get("/resource1/requires-group2-role").header(HttpHeaders.AUTHORIZATION, "1fd84ad9-760d-401f-8cf0-7a80aa42566c"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/resource2/requires-group2-role").header(HttpHeaders.AUTHORIZATION, "1fd84ad9-760d-401f-8cf0-7a80aa42566c"))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test void testNonExistentRestrictedEndpointNotFound() throws Exception {
        mockMvc.perform(get("/resource1/requires-group1-role/does-not-exist").header(HttpHeaders.AUTHORIZATION, "b7bbdb3d-d0b9-4632-b752-b2e0f9486baf"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

}