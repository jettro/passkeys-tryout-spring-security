package eu.luminis.passkeystryout;

import eu.luminis.passkeystryout.entity.User;
import eu.luminis.passkeystryout.repository.UserRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Controller
public class PasskeyRegistrationController {
    
    private final UserRepository userRepository;
    private final JdbcOperations jdbcOperations;
    
    public PasskeyRegistrationController(UserRepository userRepository, JdbcOperations jdbcOperations) {
        this.userRepository = userRepository;
        this.jdbcOperations = jdbcOperations;
    }
    
    @GetMapping("/passkey/register")
    public String passkeyRegistrationPage() {
        return "register-passkey";
    }
    
    @DeleteMapping("/passkey/{credentialId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, String>> deletePasskey(
            @PathVariable String credentialId,
            Authentication authentication) {
        
        try {
            // Get username from authentication
            String username = getUsername(authentication);
            
            // Verify user owns this passkey
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get user entity
            String userEntityQuery = "SELECT id FROM user_entities WHERE name = ?";
            List<Map<String, Object>> userEntities = jdbcOperations.queryForList(userEntityQuery, username);
            
            if (userEntities.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User entity not found"));
            }
            
            String userEntityId = (String) userEntities.get(0).get("id");
            
            // Verify credential belongs to user
            String verifyQuery = "SELECT id FROM user_credentials WHERE credential_id = ? AND user_entity_user_id = ?";
            List<Map<String, Object>> credentials = jdbcOperations.queryForList(verifyQuery, credentialId, userEntityId);
            
            if (credentials.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Credential not found or does not belong to user"));
            }
            
            // Delete from Spring Security's user_credentials table
            String deleteCredentialQuery = "DELETE FROM user_credentials WHERE credential_id = ?";
            jdbcOperations.update(deleteCredentialQuery, credentialId);
            
            return ResponseEntity.ok(Map.of("message", "Passkey deleted successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete passkey: " + e.getMessage()));
        }
    }
    
    private String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof PublicKeyCredentialUserEntity userEntity) {
            return userEntity.getName();
        } else {
            return String.valueOf(principal);
        }
    }
}
