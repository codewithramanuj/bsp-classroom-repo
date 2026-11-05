package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.LoginRequest;
import com.bsp.supportassistant.dto.LoginResponse;
import com.bsp.supportassistant.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// POST /api/public/login checks the user name and password, and returns a token.
// A wrong login throws an exception, and the client gets 401 (see SecurityConfig).
@RestController
@RequestMapping("/api/public")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // Loads the user from the table and compares the password with the BCrypt hash.
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new LoginResponse(jwtService.createToken(request.username()));
    }
}
