package eu.luminis.passkeystryout.passkey;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Controller to handle deletion of passkeys. You can only delete passkeys for the currently authenticated user.
 * Storing of passkeys is handled by the webauthn endpoint from Spring Security,
 * so this controller only needs to delete the passkey from the database.
 */
@Controller
@NullMarked
public class PasskeyDeleteController {

    private final CredentialRepository credentialRepository;

    public PasskeyDeleteController(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @GetMapping("/passkey/register")
    public String register() {
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
