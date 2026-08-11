package com.busgo.security;

import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID getUserId(Authentication auth) {
        if (auth == null) return null;
        if (auth instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
            Object uid = jwt.getClaim("uid");
            String uidStr = uid != null ? uid.toString() : jwt.getSubject();
            try { return uidStr != null ? UUID.fromString(uidStr) : null; } catch (Exception ignored) {}
        }
        // fallback: resolve by principal name
        String principal = auth.getName();
        if (principal != null) {
            User user = userRepository.findByEmail(principal).orElseGet(() -> userRepository.findByMobile(principal).orElse(null));
            if (user != null) return user.getId();
        }
        return null;
    }

    public boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        // check authorities
        for (GrantedAuthority a : auth.getAuthorities()) {
            String v = a.getAuthority();
            if (v != null && (v.equals("ROLE_ADMIN") || v.equals("ADMIN"))) return true;
        }
        // if JWT, check claims for roles
        if (auth instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
            Object roles = jwt.getClaim("roles");
            if (roles != null) {
                String rs = roles.toString();
                if (rs.contains("ADMIN")) return true;
            }
            Object realm = jwt.getClaim("realm_access");
            if (realm != null) {
                try {
                    var node = (java.util.Map<?,?>) realm;
                    Object r = node.get("roles");
                    if (r != null && r.toString().contains("ADMIN")) return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }
}
