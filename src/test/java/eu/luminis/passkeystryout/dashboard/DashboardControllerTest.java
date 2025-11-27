package eu.luminis.passkeystryout.dashboard;

import eu.luminis.passkeystryout.passkey.CredentialRepository;
import eu.luminis.passkeystryout.user.User;
import eu.luminis.passkeystryout.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    CredentialRepository credentialRepository;

    @Nested
    class Dashboard {

        @Test
        void returnsDashboardWithPasswordAuthAndExistingUser() throws Exception {
            UserDetails principal = org.springframework.security.core.userdetails.User
                    .withUsername("alice")
                    .password("password")
                    .roles("USER")
                    .build();

            User user = new User();
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
            List<Map<String, Object>> passkeys = List.of(Map.of(
                    "id", "key1",
                    "credential_id", "cred1",
                    "label", "Alice's Passkey",
                    "created", Instant.now(),
                    "last_used", Instant.now(),
                    "signature_count", 42,
                    "backup_state", false));
            when(credentialRepository.findPasskeysInfoByUsername("alice")).thenReturn(passkeys);

            mockMvc.perform(get("/dashboard").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("username", "alice"))
                    .andExpect(model().attribute("authMethod", "Password"))
                    .andExpect(model().attribute("passkeys", passkeys))
                    .andExpect(model().attribute("passkeyCount", 1));
        }

        @Test
        void returnsDashboardWithPasskeyAuthAndExistingUser() throws Exception {
            PublicKeyCredentialUserEntity webAuthnUser = mock(PublicKeyCredentialUserEntity.class);
            when(webAuthnUser.getName()).thenReturn("bob");

            User user = new User();
            when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
            List<Map<String, Object>> passkeys = List.of(
                    Map.of("id", "key2",
                            "credential_id", "cred1",
                            "label", "Alice's Passkey",
                            "created", Instant.now(),
                            "last_used", Instant.now(),
                            "signature_count", 42,
                            "backup_state", false),
                    Map.of("id", "key3", "credential_id", "cred1",
                            "label", "Alice's Passkey",
                            "created", Instant.now(),
                            "last_used", Instant.now(),
                            "signature_count", 42,
                            "backup_state", false));
            when(credentialRepository.findPasskeysInfoByUsername("bob")).thenReturn(passkeys);

            mockMvc.perform(get("/dashboard").with(authentication(
                            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(webAuthnUser, null, List.of())
                    )))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("username", "bob"))
                    .andExpect(model().attribute("authMethod", "Passkey"))
                    .andExpect(model().attribute("passkeys", passkeys))
                    .andExpect(model().attribute("passkeyCount", 2));
        }

        @Test
        void returnsDashboardWithUnknownPrincipalAndNoUser() throws Exception {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            mockMvc.perform(get("/dashboard").with(authentication(
                            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                    "unknown", null, List.of())
                    )))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("username", "unknown"))
                    .andExpect(model().attribute("authMethod", "Password"))
                    .andExpect(model().attribute("passkeys", List.of()))
                    .andExpect(model().attribute("passkeyCount", 0));
        }

        @Test
        void returnsDashboardWithExistingUserAndNoPasskeys() throws Exception {
            UserDetails principal = org.springframework.security.core.userdetails.User
                    .withUsername("carol")
                    .password("password")
                    .roles("USER")
                    .build();

            User user = new User();
            when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
            when(credentialRepository.findPasskeysInfoByUsername("carol")).thenReturn(List.of());

            mockMvc.perform(get("/dashboard").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("username", "carol"))
                    .andExpect(model().attribute("authMethod", "Password"))
                    .andExpect(model().attribute("passkeys", List.of()))
                    .andExpect(model().attribute("passkeyCount", 0));
        }
    }
}
