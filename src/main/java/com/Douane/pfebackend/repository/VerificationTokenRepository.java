package com.Douane.pfebackend.repository;

import com.Douane.pfebackend.entites.userEntites.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;



public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
}
