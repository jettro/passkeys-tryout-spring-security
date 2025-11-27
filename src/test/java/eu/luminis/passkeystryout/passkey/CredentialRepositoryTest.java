package eu.luminis.passkeystryout.passkey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CredentialRepositoryTest {

    @Mock
    private JdbcPublicKeyCredentialUserEntityRepository userEntityRepository;

    @Mock
    private JdbcUserCredentialRepository userCredentialRepository;

    @InjectMocks
    private CredentialRepository credentialRepository;

    private PublicKeyCredentialUserEntity testUserEntity;
    private CredentialRecord testCredentialRecord1;
    private CredentialRecord testCredentialRecord2;

    @BeforeEach
    void setUp() {
        // Create test user entity (mock)
        testUserEntity = mock(PublicKeyCredentialUserEntity.class);
        Bytes userId = Bytes.fromBase64("dGVzdHVzZXJpZA");  // "testuserid" in base64
        lenient().when(testUserEntity.getName()).thenReturn("testuser");
        lenient().when(testUserEntity.getId()).thenReturn(userId);
        lenient().when(testUserEntity.getDisplayName()).thenReturn("Test User");

        // Create test credential records (mocks)
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(3600);
        Bytes credId1 = Bytes.fromBase64("Y3JlZDFpZA");
        Bytes credId2 = Bytes.fromBase64("Y3JlZDJpZA");

        testCredentialRecord1 = mock(CredentialRecord.class);
        lenient().when(testCredentialRecord1.getCredentialId()).thenReturn(credId1);
        lenient().when(testCredentialRecord1.getUserEntityUserId()).thenReturn(userId);
        lenient().when(testCredentialRecord1.getSignatureCount()).thenReturn(5L);
        lenient().when(testCredentialRecord1.getLabel()).thenReturn("My MacBook");
        lenient().when(testCredentialRecord1.getCreated()).thenReturn(earlier);
        lenient().when(testCredentialRecord1.getLastUsed()).thenReturn(now);
        lenient().when(testCredentialRecord1.isBackupState()).thenReturn(true);

        testCredentialRecord2 = mock(CredentialRecord.class);
        lenient().when(testCredentialRecord2.getCredentialId()).thenReturn(credId2);
        lenient().when(testCredentialRecord2.getUserEntityUserId()).thenReturn(userId);
        lenient().when(testCredentialRecord2.getSignatureCount()).thenReturn(0L);
        lenient().when(testCredentialRecord2.getLabel()).thenReturn("YubiKey");
        lenient().when(testCredentialRecord2.getCreated()).thenReturn(earlier);
        lenient().when(testCredentialRecord2.getLastUsed()).thenReturn(null);
        lenient().when(testCredentialRecord2.isBackupState()).thenReturn(false);
    }

    @Test
    void findPasskeysInfoByUsername_shouldReturnPasskeyInfo_whenUserHasPasskeys() {
        // Given
        String username = "testuser";
        when(userEntityRepository.findByUsername(username)).thenReturn(testUserEntity);
        when(userCredentialRepository.findByUserId(testUserEntity.getId()))
                .thenReturn(List.of(testCredentialRecord1, testCredentialRecord2));

        // When
        List<Map<String, Object>> result = credentialRepository.findPasskeysInfoByUsername(username);

        // Then
        assertThat(result).hasSize(2);
        
        // Check first credential
        Map<String, Object> passkey1 = result.get(0);
        assertThat(passkey1.get("credential_id")).isEqualTo("Y3JlZDFpZA");
        assertThat(passkey1.get("label")).isEqualTo("My MacBook");
        assertThat(passkey1.get("created")).isEqualTo(testCredentialRecord1.getCreated());
        assertThat(passkey1.get("last_used")).isEqualTo(testCredentialRecord1.getLastUsed());
        assertThat(passkey1.get("signature_count")).isEqualTo(5L);
        assertThat(passkey1.get("backup_state")).isEqualTo(true);

        // Check second credential
        Map<String, Object> passkey2 = result.get(1);
        assertThat(passkey2.get("credential_id")).isEqualTo("Y3JlZDJpZA");
        assertThat(passkey2.get("label")).isEqualTo("YubiKey");
        assertThat(passkey2.get("created")).isEqualTo(testCredentialRecord2.getCreated());
        assertThat(passkey2.get("last_used")).isNull();
        assertThat(passkey2.get("signature_count")).isEqualTo(0L);
        assertThat(passkey2.get("backup_state")).isEqualTo(false);

        // Verify interactions
        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository).findByUserId(testUserEntity.getId());
    }

    @Test
    void findPasskeysInfoByUsername_shouldReturnEmptyList_whenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userEntityRepository.findByUsername(username)).thenReturn(null);

        // When
        List<Map<String, Object>> result = credentialRepository.findPasskeysInfoByUsername(username);

        // Then
        assertThat(result).isEmpty();
        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository, never()).findByUserId(any());
    }

    @Test
    void findPasskeysInfoByUsername_shouldReturnEmptyList_whenUserHasNoPasskeys() {
        // Given
        String username = "testuser";
        when(userEntityRepository.findByUsername(username)).thenReturn(testUserEntity);
        when(userCredentialRepository.findByUserId(testUserEntity.getId())).thenReturn(List.of());

        // When
        List<Map<String, Object>> result = credentialRepository.findPasskeysInfoByUsername(username);

        // Then
        assertThat(result).isEmpty();
        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository).findByUserId(testUserEntity.getId());
    }

    @Test
    void deletePasskeyFromUser_shouldDeleteCredential_whenCredentialBelongsToUser() {
        // Given
        String username = "testuser";
        String credentialId = "Y3JlZDFpZA";  // base64 of "cred1id"
        
        when(userEntityRepository.findByUsername(username)).thenReturn(testUserEntity);
        when(userCredentialRepository.findByCredentialId(any(Bytes.class)))
                .thenReturn(testCredentialRecord1);

        // When
        credentialRepository.deletePasskeyFromUser(credentialId, username);

        // Then
        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository).findByCredentialId(Bytes.fromBase64(credentialId));
        verify(userCredentialRepository).delete(testCredentialRecord1.getCredentialId());
    }

    @Test
    void deletePasskeyFromUser_shouldThrowException_whenUserNotFound() {
        // Given
        String username = "nonexistent";
        String credentialId = "Y3JlZDFpZA";
        
        when(userEntityRepository.findByUsername(username)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> credentialRepository.deletePasskeyFromUser(credentialId, username))
                .isInstanceOf(PasskeyException.class)
                .hasMessage("User entity not found");

        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository, never()).findByCredentialId(any());
        verify(userCredentialRepository, never()).delete(any());
    }

    @Test
    void deletePasskeyFromUser_shouldThrowException_whenCredentialNotFound() {
        // Given
        String username = "testuser";
        String credentialId = "nonexistent";
        
        when(userEntityRepository.findByUsername(username)).thenReturn(testUserEntity);
        when(userCredentialRepository.findByCredentialId(any(Bytes.class))).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> credentialRepository.deletePasskeyFromUser(credentialId, username))
                .isInstanceOf(PasskeyException.class)
                .hasMessage("Credential not found or does not belong to user");

        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository).findByCredentialId(any(Bytes.class));
        verify(userCredentialRepository, never()).delete(any());
    }

    @Test
    void deletePasskeyFromUser_shouldThrowException_whenCredentialBelongsToAnotherUser() {
        // Given
        String username = "testuser";
        String credentialId = "Y3JlZDFpZA";
        
        // Create a different user's ID
        Bytes differentUserId = Bytes.fromBase64("ZGlmZmVyZW50dXNlcmlk");
        CredentialRecord differentUserCredential = mock(CredentialRecord.class);
        lenient().when(differentUserCredential.getCredentialId()).thenReturn(Bytes.fromBase64(credentialId));
        lenient().when(differentUserCredential.getUserEntityUserId()).thenReturn(differentUserId);
        lenient().when(differentUserCredential.getLabel()).thenReturn("Other User's Key");
        
        when(userEntityRepository.findByUsername(username)).thenReturn(testUserEntity);
        when(userCredentialRepository.findByCredentialId(any(Bytes.class)))
                .thenReturn(differentUserCredential);

        // When / Then
        assertThatThrownBy(() -> credentialRepository.deletePasskeyFromUser(credentialId, username))
                .isInstanceOf(PasskeyException.class)
                .hasMessage("Credential not found or does not belong to user");

        verify(userEntityRepository).findByUsername(username);
        verify(userCredentialRepository).findByCredentialId(any(Bytes.class));
        verify(userCredentialRepository, never()).delete(any());
    }
}
