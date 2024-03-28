package com.Douane.pfebackend.entites.userEntites;

import jakarta.persistence.Entity;

@Entity
public class VerificationToken extends AbstractToken {

    private static final long serialVersionUID = -6551160985498051566L;

    public VerificationToken() {
        super();
    }

    public VerificationToken(final String token) {
        super(token);
    }

    public VerificationToken(final String token, final User user) {
        super(token, user);
    }
}
