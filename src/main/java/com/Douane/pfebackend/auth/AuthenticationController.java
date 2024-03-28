package com.Douane.pfebackend.auth;

import com.Douane.pfebackend.dto.AuthenticationRequest;
import com.Douane.pfebackend.dto.AuthenticationResponse;
import com.Douane.pfebackend.dto.MessgChekMail;
import com.Douane.pfebackend.dto.RegisterRequest;
import com.Douane.pfebackend.entites.userEntites.PasswordRequestUtil;
import com.Douane.pfebackend.entites.userEntites.PasswordRequestUtilnewpassword;
import com.Douane.pfebackend.entites.userEntites.User;
import com.Douane.pfebackend.lisner.RegistrationCompleteEventListener;
import com.Douane.pfebackend.sevices.userService.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final HttpServletRequest servletRequest;
    private final AuthenticationService service;
    private final RegistrationCompleteEventListener eventListener;
    @PostMapping("/register")



    public ResponseEntity<MessgChekMail> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
    @GetMapping("/verify")
    public ResponseEntity<?> confirmRegistration(@NotEmpty @RequestParam String token) {
        final String result = service.validateVerificationToken(token);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }
    @PostMapping("/password-reset-request")
    public String resetPasswordRequest(@RequestBody PasswordRequestUtil passwordRequestUtil
    )
            throws MessagingException, UnsupportedEncodingException {

        Optional<User> user = service.findByEmail(passwordRequestUtil.getEmail());
        String passwordResetUrl = "";
        if (user.isPresent()) {
            String passwordResetToken = UUID.randomUUID().toString();
            service.createPasswordResetTokenForUser(user.get(), passwordResetToken);
            passwordResetUrl = passwordResetEmailLink(user.get(), applicationUrl(servletRequest), passwordResetToken);
        }
        return passwordResetUrl;
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"
                +request.getServerPort()+request.getContextPath();
    }



    private String passwordResetEmailLink(User user, String applicationUrl,
                                          String passwordToken) throws MessagingException, UnsupportedEncodingException {
        String code = "votre code est : "+ passwordToken;
        eventListener.sendPasswordResetVerificationEmail(code,user);
        /// log.info("Click the link to reset your password :  {}", url);
        return code;
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody PasswordRequestUtilnewpassword passwordRequestUtil
    ){
        String tokenVerificationResult = service.validatePasswordResetToken(passwordRequestUtil.getCodeemail());
        if (!tokenVerificationResult.equalsIgnoreCase("valid")) {
            return "Invalid token password reset token";
        }
        Optional<User> theUser = Optional.ofNullable(service.findUserByPasswordToken(passwordRequestUtil.getCodeemail()));
        if (theUser.isPresent()) {
            service.changePassword(theUser.get(), passwordRequestUtil.getNewPassword());
            return "Password has been reset successfully";
        }
        return "Invalid password reset token";
    }



}