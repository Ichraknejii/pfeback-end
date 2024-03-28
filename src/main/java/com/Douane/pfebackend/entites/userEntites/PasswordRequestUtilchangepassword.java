package com.Douane.pfebackend.entites.userEntites;

import lombok.Data;

@Data
public class PasswordRequestUtilchangepassword {
    private String email;
    private String oldPassword;
    private String newPassword;
}
