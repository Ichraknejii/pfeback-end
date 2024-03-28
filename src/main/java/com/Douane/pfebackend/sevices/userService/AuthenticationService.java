package com.Douane.pfebackend.sevices.userService;

import com.Douane.pfebackend.configsecurete.AppConstants;
import com.Douane.pfebackend.dto.AuthenticationRequest;
import com.Douane.pfebackend.dto.AuthenticationResponse;
import com.Douane.pfebackend.dto.MessgChekMail;
import com.Douane.pfebackend.dto.RegisterRequest;
import com.Douane.pfebackend.entites.userEntites.User;
import com.Douane.pfebackend.entites.userEntites.VerificationToken;
import com.Douane.pfebackend.repository.UserRepository;
import com.Douane.pfebackend.repository.VerificationTokenRepository;
import com.Douane.pfebackend.token.Token;
import com.Douane.pfebackend.token.TokenRepository;
import com.Douane.pfebackend.token.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    private  AuthenticationManager authenticationManager;
    private final PasswordResetTokenService passwordResetTokenService;
    private final VerificationTokenRepository vtokenRepository;
    private final MailService mailService;
    public MessgChekMail register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().USER)
                //.role(request.getRole())
                .build();
        var savedUser = repository.save(user);
        // var jwtToken = jwtService.generateToken(user);
        //  var refreshToken = jwtService.generateRefreshToken(user);


        // saveUserToken(savedUser, jwtToken);
        //verification email
        final String token = UUID.randomUUID().toString();
        createVerificationTokenForUser(user, token);
        mailService.sendVerificationToken(token, user);
        return MessgChekMail.builder()
                .msg("Success! Please, check your email to complete your registration")

                .build();
    }

    public void createVerificationTokenForUser(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        vtokenRepository.save(myToken);
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public String validateVerificationToken(String token) {
        Optional<VerificationToken> verificationToken = vtokenRepository.findByToken(token);
        if (verificationToken == null) {
            return AppConstants.TOKEN_INVALID;
        }

        final User user = verificationToken.get().getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.get().getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return AppConstants.TOKEN_EXPIRED;
        }

        user.setActive(true);


        mailService.sendTextEmail(user.getEmail(),"vous etes le bienvenu ","vous etes le bienvenu ");
        vtokenRepository.deleteById(verificationToken.get().getId());
        repository.save(user);
        return AppConstants.TOKEN_VALID;
    }
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    //rest pas
    public void changePassword(User theUser, String newPassword) {
        theUser.setPassword(passwordEncoder.encode(newPassword));
        repository.save(theUser);
    }
    public String validatePasswordResetToken(String token) {
        return passwordResetTokenService.validatePasswordResetToken(token);
    }

    public User findUserByPasswordToken(String token) {
        return passwordResetTokenService.findUserByPasswordToken(token).get();
    }


    public void createPasswordResetTokenForUser(User user, String passwordResetToken) {
        passwordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
    }

    public boolean oldPasswordIsValid(User user, String oldPassword){
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}