package com.example.StdManagement.repository;

import com.example.StdManagement.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findByEmail(String email);

    Optional<EmailOtp> findByEmailAndOtpCode(String email, String otpCode);

    void deleteByEmail(String email);
}
