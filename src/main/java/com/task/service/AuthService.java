package com.task.service;

import com.task.dto.JwtAuthResponse;
import com.task.dto.LoginRequest;
import com.task.dto.RegisterRequest;
import com.task.entity.User;
import com.task.enums.Role;
import com.task.exception.BadRequestException;
import com.task.repository.UserRepository;
import com.task.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    public AuthService(AuthenticationManager authManager,
                       UserRepository userRepository,
                       PasswordEncoder encoder,
                       JwtTokenProvider jwtProvider) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
    }

    public JwtAuthResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        String token = jwtProvider.generateToken(auth);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User profile not found."));

        return new JwtAuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered.");
        }

        User account = new User();
        account.setUsername(request.getUsername());
        account.setPassword(encoder.encode(request.getPassword()));
        account.setEmail(request.getEmail());
        // Public registration strictly assigns ROLE_USER to prevent privilege escalation
        account.setRole(Role.ROLE_USER);

        userRepository.save(account);
        return "User account created successfully.";
    }
}
