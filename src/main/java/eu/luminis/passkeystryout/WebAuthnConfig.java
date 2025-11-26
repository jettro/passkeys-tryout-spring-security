package eu.luminis.passkeystryout;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;

@Configuration
public class WebAuthnConfig {
    
    @Bean
    public JdbcPublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(JdbcOperations jdbc) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
    }
    
    @Bean
    public JdbcUserCredentialRepository userCredentialRepository(JdbcOperations jdbc) {
        return new JdbcUserCredentialRepository(jdbc);
    }
}
