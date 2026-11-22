package com.stac.documentdigitization.properties;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Supported response languages and the fallback used when none matches. */
@ConfigurationProperties(prefix = "app.i18n")
public record I18nProperties(Locale defaultLocale, List<Locale> supportedLocales) {}
