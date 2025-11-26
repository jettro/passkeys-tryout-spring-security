package eu.luminis.passkeystryout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PasskeyRegistrationController {
    
    @GetMapping("/passkey/register")
    public String passkeyRegistrationPage() {
        return "register-passkey";
    }
}
