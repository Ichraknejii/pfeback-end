package com.Douane.pfebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Douane.pfebackend.entites.userEntites.PasswordResetToken;




public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String passwordResetToken);
}
