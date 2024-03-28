package com.Douane.pfebackend.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;
import com.Douane.pfebackend.entites.userEntites.User;
@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private  User user;
    private String applicationUrl;
    public RegistrationCompleteEvent(User user, String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
    }
}
