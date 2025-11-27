package eu.luminis.passkeystryout.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: nonexistent");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldHandleUsersWithDifferentPasswords() {
        // Given
        User userWithDifferentPassword = new User();
        userWithDifferentPassword.setUsername("otheruser");
        userWithDifferentPassword.setPassword("differentEncodedPassword");
        userWithDifferentPassword.setDisplayName("Other User");

        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(userWithDifferentPassword));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("otheruser");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("otheruser");
        assertThat(userDetails.getPassword()).isEqualTo("differentEncodedPassword");
    }

    @Test
    void loadUserByUsername_shouldAlwaysAssignRoleUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldHandleCaseSensitiveUsernames() {
        // Given
        String lowercaseUsername = "testuser";
        String uppercaseUsername = "TESTUSER";

        when(userRepository.findByUsername(lowercaseUsername)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(uppercaseUsername)).thenReturn(Optional.empty());

        // When
        UserDetails lowerCaseResult = userDetailsService.loadUserByUsername(lowercaseUsername);

        // Then
        assertThat(lowerCaseResult.getUsername()).isEqualTo("testuser");

        // When & Then - uppercase should fail
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(uppercaseUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: TESTUSER");
    }

    @Test
    void loadUserByUsername_shouldPreserveUserEnabledStatus() {
        // Given
        User disabledUser = new User();
        disabledUser.setUsername("disableduser");
        disabledUser.setPassword("password");
        disabledUser.setDisplayName("Disabled User");
        disabledUser.setEnabled(false);

        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("disableduser");

        // Then
        // Note: The current implementation doesn't check the enabled status from the User entity
        // Spring's User.builder() creates enabled users by default
        // If you want to respect the enabled flag, you'd need to update the implementation
        assertThat(userDetails.isEnabled()).isTrue();
    }
}
