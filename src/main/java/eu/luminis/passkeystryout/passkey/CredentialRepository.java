package eu.luminis.passkeystryout.passkey;

import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CredentialRepository {
    private final JdbcPublicKeyCredentialUserEntityRepository userEntityRepository;
    private final JdbcUserCredentialRepository userCredentialRepository;

    public CredentialRepository(JdbcPublicKeyCredentialUserEntityRepository userEntityRepository,
                                JdbcUserCredentialRepository userCredentialRepository) {
        this.userEntityRepository = userEntityRepository;
        this.userCredentialRepository = userCredentialRepository;
    }

    /**
     * Find information about all passkeys (credentials) for a given username.
     *
     * @param username String containing the username of the user
     * @return List of maps with passkey information, or empty list if user has no passkeys
     */
    public List<Map<String, Object>> findPasskeysInfoByUsername(String username) {
        PublicKeyCredentialUserEntity userEntity = userEntityRepository.findByUsername(username);
        if (userEntity == null) {
            return List.of();
        }

        List<CredentialRecord> credentialRecords = userCredentialRepository.findByUserId(userEntity.getId());
        if (credentialRecords.isEmpty()) {
            return List.of();
        }

        return credentialRecords.stream().map(credentialRecord -> {
            Map<String, Object> result = new HashMap<>();
            result.put("credential_id", credentialRecord.getCredentialId().toBase64UrlString());
            result.put("label", credentialRecord.getLabel());
            result.put("created", credentialRecord.getCreated());
            result.put("last_used", credentialRecord.getLastUsed());
            result.put("signature_count", credentialRecord.getSignatureCount());
            result.put("backup_state", credentialRecord.isBackupState());
            return result;
        }).toList();
    }

    /**
     * Delete a passkey (credential) for a given user.
     *
     * @param credentialId String containing the credential ID in Base64 format
     * @param username String containing the username of the user
     * @throws PasskeyException if the user or credential is not found or does not belong to the user
     */
    public void deletePasskeyFromUser(String credentialId, String username) {
        PublicKeyCredentialUserEntity userEntity = userEntityRepository.findByUsername(username);
        if (userEntity == null) {
            throw new PasskeyException("User entity not found");
        }

        CredentialRecord record = userCredentialRepository.findByCredentialId(Bytes.fromBase64(credentialId));

        if (record == null || !record.getUserEntityUserId().equals(userEntity.getId())) {
            throw new PasskeyException("Credential not found or does not belong to user");
        }

        userCredentialRepository.delete(record.getCredentialId());
    }
}
