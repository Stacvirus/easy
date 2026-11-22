package com.stac.documentdigitization.configurations;

import com.stac.documentdigitization.properties.I18nProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/** Resolves Accept-Language to English or French, defaulting to English. */
@Configuration
public class LocaleConfiguration {
    @Bean(name = "localeResolver")
    LocaleResolver localeResolver(I18nProperties properties) {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(properties.supportedLocales());
        resolver.setDefaultLocale(properties.defaultLocale());
        return resolver;
    }
}
