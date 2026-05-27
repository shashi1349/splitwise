package com.shashi.splitwise.auth.application;

import com.shashi.splitwise.auth.api.dto.AuthResponse;
import com.shashi.splitwise.auth.api.dto.LoginRequest;
import com.shashi.splitwise.auth.api.dto.RegisterRequest;
import com.shashi.splitwise.auth.api.dto.UserDto;
import com.shashi.splitwise.common.config.AppProperties;
import com.shashi.splitwise.common.error.ConflictException;
import com.shashi.splitwise.common.security.JwtService;
import com.shashi.splitwise.user.domain.User;
import com.shashi.splitwise.user.domain.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDS = "Invalid email or password.";

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AppProperties props;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, AppProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.props = props;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normaliseEmail(req.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email is already registered.");
        }
        User saved = users.save(new User(email, req.displayName().trim(), encoder.encode(req.password())));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = normaliseEmail(req.email());
        User user = users.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDS));
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CREDS);
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwt.issue(user.getId(), user.getEmail());
        return new AuthResponse(token, props.jwt().ttlMinutes() * 60L, UserDto.from(user));
    }

    private static String normaliseEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }
}
