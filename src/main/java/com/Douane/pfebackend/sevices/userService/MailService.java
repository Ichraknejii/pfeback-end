package com.Douane.pfebackend.sevices.userService;

import com.Douane.pfebackend.entites.userEntites.User;

public interface MailService {

    void sendVerificationToken(String token, User user);

    void sendTextEmail(String string, String string2, String string3);
}