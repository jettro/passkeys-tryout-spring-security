package eu.luminis.passkeystryout.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void register_shouldCreateUserAndRedirect_whenUsernameIsAvailable() throws Exception {
        // Given
        String username = "newuser";
        String displayName = "New User";
        String password = "password123";
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // Verify interactions
        verify(userRepository).existsByUsername(username);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldReturnErrorAndStayOnPage_whenUsernameAlreadyExists() throws Exception {
        // Given
        String existingUsername = "existinguser";
        String displayName = "Display Name";
        String password = "password123";

        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", existingUsername)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username already exists"));

        // Verify interactions
        verify(userRepository).existsByUsername(existingUsername);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldEncodePassword_beforeSaving() throws Exception {
        // Given
        String username = "testuser";
        String displayName = "Test User";
        String rawPassword = "plainPassword";
        String encodedPassword = "$2a$10$hashedPassword";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify the saved user has the encoded password, not the raw one
            if (!savedUser.getPassword().equals(encodedPassword)) {
                throw new AssertionError("Password was not encoded before saving");
            }
            return savedUser;
        });

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", displayName)
                        .param("password", rawPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldCreateUserWithCorrectFields() throws Exception {
        // Given
        String username = "johndoe";
        String displayName = "John Doe";
        String password = "securePassword";
        String encodedPassword = "$2a$10$encoded";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify all fields are set correctly
            if (!savedUser.getUsername().equals(username)) {
                throw new AssertionError("Username not set correctly");
            }
            if (!savedUser.getDisplayName().equals(displayName)) {
                throw new AssertionError("Display name not set correctly");
            }
            if (!savedUser.getPassword().equals(encodedPassword)) {
                throw new AssertionError("Password not set correctly");
            }
            return savedUser;
        });

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void register_shouldHandleSpecialCharactersInUsername() throws Exception {
        // Given
        String username = "user_with-special.chars@123";
        String displayName = "Special User";
        String password = "password";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void register_shouldHandleEmptyDisplayName() throws Exception {
        // Given
        String username = "testuser";
        String displayName = "";
        String password = "password";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", username)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    // Note: CSRF test is not included because @AutoConfigureMockMvc(addFilters = false)
    // disables security filters including CSRF protection for easier testing.
    // CSRF protection is tested at the integration test level.

    @Test
    void register_shouldHandleLongUsername() throws Exception {
        // Given
        String longUsername = "a".repeat(100);
        String displayName = "Long Username User";
        String password = "password";

        when(userRepository.existsByUsername(longUsername)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", longUsername)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void register_shouldHandleCaseSensitiveUsernames() throws Exception {
        // Given
        String lowercaseUsername = "testuser";
        String uppercaseUsername = "TESTUSER";
        String displayName = "Test User";
        String password = "password";

        when(userRepository.existsByUsername(lowercaseUsername)).thenReturn(false);
        when(userRepository.existsByUsername(uppercaseUsername)).thenReturn(true);
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then - lowercase should succeed
        mockMvc.perform(post("/register")
                        .param("username", lowercaseUsername)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // When & Then - uppercase should fail
        mockMvc.perform(post("/register")
                        .param("username", uppercaseUsername)
                        .param("displayName", displayName)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username already exists"));
    }
}
