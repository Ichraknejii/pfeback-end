package com.Douane.pfebackend.sevices.userService;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageService {

    @Resource
    private MessageSource messageSource;

    private Locale locale = LocaleContextHolder.getLocale();

    public String getMessage(String code) {
        return messageSource.getMessage(code, null, locale);
    }

    public String getMessage(String code, Object... params) {
        return messageSource.getMessage(code, params, locale);
    }
}
