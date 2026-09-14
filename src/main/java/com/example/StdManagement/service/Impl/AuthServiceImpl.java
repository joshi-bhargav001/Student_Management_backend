package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Request.RefreshRequest;
import com.example.StdManagement.dto.Request.SendOtpRequest;
import com.example.StdManagement.dto.Request.VerifyOtpRequest;
import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.dto.Response.SendOtpResponse;
import com.example.StdManagement.dto.Response.VerifyOtpResponse;
import com.example.StdManagement.dto.Response.SignUpResponse;
import com.example.StdManagement.entity.EmailOtp;
import com.example.StdManagement.entity.User;
import com.example.StdManagement.enums.Role;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.repository.EmailOtpRepository;
import com.example.StdManagement.repository.UserRepository;
import com.example.StdManagement.security.JwtService;
import com.example.StdManagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public SignUpResponse insertRegister(SignUpRequest request) {

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exist");
        }
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password do not match");
        }
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        User saveUser = userRepository.save(user);

        return SignUpResponse.builder()
                .id(saveUser.getId())
                .name(saveUser.getName())
                .username(saveUser.getUsername())
                .email(saveUser.getEmail())
                .role(saveUser.getRole())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (passwordEncoder.upgradeEncoding(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        String refreshToken = jwtService.generateRefreshToken((UserDetails) authentication.getPrincipal());

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .token(token)
                .refreshToken(refreshToken)
                .message("Login successful")
                .role(user.getRole())
                .build();
    }

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username;

        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .message("Token refreshed successfully")
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                .map(existing -> {
                    existing.setOtpCode(otp);
                    existing.setOtpExpiresAt(expiresAt);
                    return existing;
                })
                .orElseGet(() -> EmailOtp.builder()
                        .email(email)
                        .otpCode(otp)
                        .otpExpiresAt(expiresAt)
                        .build());

        emailOtpRepository.saveAndFlush(emailOtp);

        SimpleMailMessage message = buildOtpEmail(email, otp);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.error("Failed to send OTP email to {}", email, exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to send OTP email."
            );
        }

        return SendOtpResponse.builder()
                .email(email)
                .message("OTP sent successfully. It is valid for 5 minutes.")
                .build();
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

            EmailOtp emailOtp = emailOtpRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "OTP not found for this email."
                    ));

            if (emailOtp.getOtpCode() == null || emailOtp.getOtpExpiresAt() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has not been requested.");
            }

        if (LocalDateTime.now().isAfter(emailOtp.getOtpExpiresAt())) {
            emailOtpRepository.deleteByEmail(email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired.");
        }

            if (!otp.equals(emailOtp.getOtpCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP.");
            }

        emailOtpRepository.deleteByEmail(email);

        return VerifyOtpResponse.builder()
                .email(email)
                .message("OTP verified successfully.")
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private SimpleMailMessage buildOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Student Management System - Email Verification OTP");
        message.setText(
                "Hello,\n\n" +
                        "Your OTP for account verification is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "Please do not share this OTP with anyone.\n\n" +
                        "Regards,\n" +
                        "Student Management System"
        );
        return message;
    }
}
