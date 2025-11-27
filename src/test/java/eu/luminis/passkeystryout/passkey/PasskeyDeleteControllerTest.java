package eu.luminis.passkeystryout.passkey;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@NullMarked
class PasskeyDeleteControllerTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PasskeyDeleteController controller;

    private UserDetails testUserDetails;
    private PublicKeyCredentialUserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        // Create test user details
        testUserDetails = User.withUsername("testuser")
                .password("password")
                .roles("USER")
                .build();

        // Create test user entity (mock)
        testUserEntity = mock(PublicKeyCredentialUserEntity.class);
        lenient().when(testUserEntity.getName()).thenReturn("testuser");
    }

    @Test
    void deletePasskey_shouldReturnSuccess_whenPasskeyDeletedSuccessfully() {
        // Given
        String credentialId = "Y3JlZDFpZA";
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        doNothing().when(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("message", "Passkey deleted successfully");
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");
    }

    @Test
    void deletePasskey_shouldReturnError_whenPasskeyNotFound() {
        // Given
        String credentialId = "nonexistent";
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        doThrow(new PasskeyException("Credential not found or does not belong to user"))
                .when(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("error", "Failed to delete passkey: Credential not found or does not belong to user");
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");
    }

    @Test
    void deletePasskey_shouldReturnError_whenUserNotFound() {
        // Given
        String credentialId = "Y3JlZDFpZA";
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        doThrow(new PasskeyException("User entity not found"))
                .when(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("error", "Failed to delete passkey: User entity not found");
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");
    }

    @Test
    void deletePasskey_shouldHandleUserDetailsAuthentication() {
        // Given
        String credentialId = "Y3JlZDFpZA";
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        doNothing().when(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");
    }

    @Test
    void deletePasskey_shouldHandlePublicKeyCredentialUserEntityAuthentication() {
        // Given
        String credentialId = "Y3JlZDFpZA";
        when(authentication.getPrincipal()).thenReturn(testUserEntity);
        doNothing().when(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "testuser");
    }

    @Test
    void deletePasskey_shouldHandleStringPrincipal() {
        // Given
        String credentialId = "Y3JlZDFpZA";
        when(authentication.getPrincipal()).thenReturn("stringuser");
        doNothing().when(credentialRepository).deletePasskeyFromUser(credentialId, "stringuser");

        // When
        ResponseEntity<Map<String, String>> response = controller.deletePasskey(credentialId, authentication);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(credentialRepository).deletePasskeyFromUser(credentialId, "stringuser");
    }
}
