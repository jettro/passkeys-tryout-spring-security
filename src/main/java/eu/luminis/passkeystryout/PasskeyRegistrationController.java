package eu.luminis.passkeystryout;

import eu.luminis.passkeystryout.entity.User;
import eu.luminis.passkeystryout.repository.CredentialRepository;
import eu.luminis.passkeystryout.repository.PasskeyException;
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
    private final CredentialRepository credentialRepository;

    public PasskeyRegistrationController(UserRepository userRepository, CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }
    
    @GetMapping("/passkey/register")
    public String passkeyRegistrationPage() {
        return "register-passkey";
    }
    
    @DeleteMapping("/passkey/{credentialId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deletePasskey(
            @PathVariable String credentialId,
            Authentication authentication) {
        
        try {
            String username = getUsername(authentication);

            credentialRepository.deletePasskeyFromUser(credentialId, username);

            return ResponseEntity.ok(Map.of("message", "Passkey deleted successfully"));
        } catch (PasskeyException e) {
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
