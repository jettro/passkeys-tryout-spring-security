package eu.luminis.passkeystryout;

import eu.luminis.passkeystryout.entity.User;
import eu.luminis.passkeystryout.repository.CredentialRepository;
import eu.luminis.passkeystryout.repository.UserRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    public DashboardController(UserRepository userRepository, CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Determine principal and username for both password and passkey logins
        Object principal = authentication.getPrincipal();
        String username;
        boolean isPasskeyAuth;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
            isPasskeyAuth = false;
        } else if (principal instanceof PublicKeyCredentialUserEntity userEntity) {
            username = userEntity.getName();
            isPasskeyAuth = true;
        } else {
            username = String.valueOf(principal);
            isPasskeyAuth = false;
        }

        model.addAttribute("username", username);
        model.addAttribute("authMethod", isPasskeyAuth ? "Passkey" : "Password");

        // Get user info
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            List<Map<String, Object>> credentials = credentialRepository.findPasskeysInfoByUsername(username);
            model.addAttribute("passkeys", credentials);
            model.addAttribute("passkeyCount", credentials.size());
        } else {
            model.addAttribute("passkeys", List.of());
            model.addAttribute("passkeyCount", 0);
        }


        return "dashboard";
    }
}
