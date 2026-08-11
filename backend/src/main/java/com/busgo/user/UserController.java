package com.busgo.user;

import com.busgo.user.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        String username = auth.getName();
        User user = userRepository.findByEmail(username).orElseGet(() -> userRepository.findByMobile(username).orElse(null));
        if (user == null) return ResponseEntity.status(404).body("User not found");

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmailField());
        dto.setMobile(user.getMobile());
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        dto.setRoles(roles);
        return ResponseEntity.ok(dto);
    }
}
