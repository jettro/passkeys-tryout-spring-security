package eu.luminis.passkeystryout;

import eu.luminis.passkeystryout.entity.User;
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
    private final JdbcOperations jdbcOperations;
    
    public DashboardController(UserRepository userRepository, JdbcOperations jdbcOperations) {
        this.userRepository = userRepository;
        this.jdbcOperations = jdbcOperations;
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
            // Get WebAuthn user entity
            String userEntityQuery = "SELECT id, name, display_name FROM user_entities WHERE name = ?";
            List<Map<String, Object>> userEntities = jdbcOperations.queryForList(userEntityQuery, username);
            
            if (!userEntities.isEmpty()) {
                String userEntityId = (String) userEntities.get(0).get("id");
                
                // Get registered passkeys
                String credentialsQuery = "SELECT label, created, last_used, authenticator_transports, backup_state " +
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
                
                model.addAttribute("passkeys", credentials);
                model.addAttribute("passkeyCount", credentials.size());
            } else {
                model.addAttribute("passkeys", List.of());
                model.addAttribute("passkeyCount", 0);
            }
        }
        
        return "dashboard";
    }
}
