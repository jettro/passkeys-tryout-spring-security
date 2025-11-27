package eu.luminis.passkeystryout.repository;

import eu.luminis.passkeystryout.entity.User;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class CredentialRepository {
    private final JdbcOperations jdbcOperations;
    private final UserRepository userRepository;

    public CredentialRepository(JdbcOperations jdbcOperations, UserRepository userRepository) {
        this.jdbcOperations = jdbcOperations;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> findPasskeysInfoByUsername(String username) {
        // Get WebAuthn user entity
        String userEntityQuery = "SELECT id, name, display_name FROM user_entities WHERE name = ?";
        List<Map<String, Object>> userEntities = jdbcOperations.queryForList(userEntityQuery, username);

        if (!userEntities.isEmpty()) {
            String userEntityId = (String) userEntities.get(0).get("id");

            // Get registered passkeys
            String credentialsQuery = "SELECT credential_id, label, created, last_used, signature_count, " +
                    "authenticator_transports, backup_state " +
                    "FROM user_credentials WHERE user_entity_user_id = ? ORDER BY created DESC";
            List<Map<String, Object>> credentials = jdbcOperations.queryForList(credentialsQuery, userEntityId);

            // Convert SQL Timestamps to Instant for Thymeleaf
            credentials.forEach(cred -> {
                if (cred.get("created") instanceof java.sql.Timestamp) {
                    cred.put("created", ((java.sql.Timestamp) cred.get("created")).toInstant());
                }
                if (cred.get("last_used") instanceof java.sql.Timestamp) {
                    cred.put("last_used", ((java.sql.Timestamp) cred.get("last_used")).toInstant());
                }
            });

            return credentials;
        }

        return List.of();
    }

    @Transactional
    public void deletePasskeyFromUser(String credentialId, String username) {
        // Verify user owns this passkey
        userRepository.findByUsername(username).orElseThrow(() -> new PasskeyException("User not found"));

        // Get user entity
        String userEntityQuery = "SELECT id FROM user_entities WHERE name = ?";
        List<Map<String, Object>> userEntities = jdbcOperations.queryForList(userEntityQuery, username);

        if (userEntities.isEmpty()) {
            throw new PasskeyException("User entity not found");
        }

        String userEntityId = (String) userEntities.getFirst().get("id");

        // Verify credential belongs to user
        String verifyQuery = "SELECT id FROM user_credentials WHERE credential_id = ? AND user_entity_user_id = ?";
        List<Map<String, Object>> credentials = jdbcOperations.queryForList(verifyQuery, credentialId, userEntityId);

        if (credentials.isEmpty()) {
            throw new PasskeyException("Credential not found or does not belong to user");
        }

        // Delete from Spring Security's user_credentials table
        String deleteCredentialQuery = "DELETE FROM user_credentials WHERE credential_id = ?";
        jdbcOperations.update(deleteCredentialQuery, credentialId);
    }
}
