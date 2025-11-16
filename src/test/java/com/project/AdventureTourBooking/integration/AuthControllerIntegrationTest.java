package com.project.AdventureTourBooking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.AdventureTourBooking.dto.LoginRequest;
import com.project.AdventureTourBooking.dto.RegisterRequest;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUser_persistsAndReturnsAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Smith",
                "alice@example.com",
                "password123",
                UserRole.CUSTOMER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        User persisted = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", persisted.getPassword())).isTrue();
    }

    @Test
    void loginUser_withValidCredentials_returnsAuthResponse() throws Exception {
        User user = new User();
        user.setName("Bob Operator");
        user.setEmail("bob@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole(UserRole.OPERATOR);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("bob@example.com", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Bob Operator"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.role").value("OPERATOR"));
    }
}
