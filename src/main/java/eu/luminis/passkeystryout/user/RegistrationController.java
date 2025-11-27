package eu.luminis.passkeystryout.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, 
                          @RequestParam String displayName,
                          @RequestParam String password,
                          Model model) {
        
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, displayName, encodedPassword);
        userRepository.save(user);
        
        return "redirect:/login?registered";
    }
}
