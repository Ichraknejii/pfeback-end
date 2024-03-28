package com.Douane.pfebackend.repository;

import com.Douane.pfebackend.entites.userEntites.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User>	findByEmail(String email);
}