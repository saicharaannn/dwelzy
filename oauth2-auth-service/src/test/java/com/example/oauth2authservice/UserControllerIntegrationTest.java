package com.example.oauth2authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPublicHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.message", containsString("Welcome! This is the public home page.")));
    }

    @Test
    public void testApiUserEndpoint_unauthenticated_shouldRedirectToLogin() throws Exception {
        // For Spring Boot 3.x, unauthenticated access to a secured endpoint typically results
        // in a redirect to the OAuth2 provider's login page or a 401 if configured differently.
        // The default behavior with oauth2Login() is a 302 redirect to the provider.
        // We will check for a 302 status.
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isFound()); // Expect redirect (302) to login
    }

    @Test
    public void testApiUserEndpoint_authenticated_shouldReturnUserData() throws Exception {
        mockMvc.perform(get("/api/user")
                .with(oauth2Login()
                    .attributes(attrs -> {
                        attrs.put("name", "Test User");
                        attrs.put("email", "test.user@example.com");
                    })
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.name", is("Test User")))
            .andExpect(jsonPath("$.email", is("test.user@example.com")));
    }

    // Example of using @WithMockUser for a simple role-based test if you had them
    // For OAuth2, .with(oauth2Login()) is more appropriate for testing user attributes
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testApiUserEndpoint_withMockUser_shouldBeOkButNoOAuthAttributes() throws Exception {
        // This test uses @WithMockUser, which is a simpler form of mock authentication.
        // It won't populate OAuth2User attributes like 'name' or 'email' from the provider.
        // The principal will be a standard UserDetails object.
        // The endpoint /api/user expects an OAuth2User, so this might not directly map.
        // Depending on how SecurityConfig and UserController are set up,
        // this might return a generic user or fail to cast.
        // For our UserController which directly uses @AuthenticationPrincipal OAuth2User,
        // this test would likely result in the "User not authenticated" or a cast error if principal isn't OAuth2User.
        // Let's adjust to what our endpoint actually does.
        // Our endpoint returns specific attributes. If principal is not OAuth2User, it returns an error map.
         mockMvc.perform(get("/api/user"))
            .andExpect(status().isOk()) // The request is authenticated due to @WithMockUser
            .andExpect(jsonPath("$.error").value("User not authenticated")); // Because principal is not OAuth2User
    }
}
