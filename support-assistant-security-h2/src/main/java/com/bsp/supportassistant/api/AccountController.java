package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.MeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    // Open to everyone: it is under /api/public.
    @GetMapping("/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "up");
    }

    // Spring Security gives the controller the user who logged in.
    // Spring Security 7 also lists FACTOR_PASSWORD (how the user proved who they are). Only the roles are shown here.
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return new MeResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(name -> name.startsWith("ROLE_"))
                        .toList());
    }
}
