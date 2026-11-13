package com.elishaearley.csfoundations.shortener.auth;

import com.elishaearley.csfoundations.commons.jwt.JwtTokens;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final JwtTokens jwt;

    public AuthService(UserAccountRepository users, PasswordEncoder passwords, JwtTokens jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    public TokenResponse register(String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.findByEmail(normalized).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserAccount saved = users.save(new UserAccount(normalized, passwords.encode(password)));
        return new TokenResponse(jwt.issue(String.valueOf(saved.getId()), saved.getEmail()));
    }

    public TokenResponse login(String email, String password) {
        UserAccount user = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        if (!passwords.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
        return new TokenResponse(jwt.issue(String.valueOf(user.getId()), user.getEmail()));
    }

    public record TokenResponse(String token) {}
}
