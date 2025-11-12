package com.project.AdventureTourBooking.service;

import com.project.AdventureTourBooking.dto.AuthResponse;
import com.project.AdventureTourBooking.dto.LoginRequest;
import com.project.AdventureTourBooking.dto.RegisterRequest;
import com.project.AdventureTourBooking.model.User;
import com.project.AdventureTourBooking.model.UserRole;
import com.project.AdventureTourBooking.repository.UserRepository;
import com.project.AdventureTourBooking.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_whenEmailAlreadyExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest(
                "Alice",
                "alice@example.com",
                "password",
                UserRole.CUSTOMER
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_withValidRequest_savesEncodedPassword() {
        RegisterRequest request = new RegisterRequest(
                "Bob",
                "bob@example.com",
                "plain-password",
                UserRole.OPERATOR
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        AuthResponse response = authService.register(request);

        assertEquals(7L, response.id());
        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertEquals(request.role(), response.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();
        assertEquals("encoded-password", persisted.getPassword());
        assertEquals(UserRole.OPERATOR, persisted.getRole());
    }

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("eve@example.com", "secret");

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails principal = new CustomUserDetails(11L, request.email(), "encoded", UserRole.CUSTOMER);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = new User();
        user.setId(11L);
        user.setName("Eve");
        user.setEmail(request.email());
        user.setRole(UserRole.CUSTOMER);

        when(userRepository.findById(principal.getId())).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isEqualTo(authentication);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getName(), response.name());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getRole(), response.role());
    }
}
