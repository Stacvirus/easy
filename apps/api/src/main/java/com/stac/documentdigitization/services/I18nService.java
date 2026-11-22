package com.stac.documentdigitization.services;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/** Resolves API messages in the selected request language. */
@Service
@RequiredArgsConstructor
public class I18nService {
    private final MessageSource messageSource;

    public String message(String key, Object... args) {
        return message(key, LocaleContextHolder.getLocale(), args);
    }

    /** Used by security filters before MVC binds the request locale. */
    public String message(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, key, locale);
    }
}
