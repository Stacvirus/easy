package com.stac.documentdigitization.configurations;

import com.stac.documentdigitization.exceptions.ApiErrorDto;
import com.stac.documentdigitization.exceptions.ErrorCode;
import com.stac.documentdigitization.services.I18nService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import tools.jackson.databind.ObjectMapper;

/** Writes the same error shape for failures raised before MVC handles a request. */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {
    private final ObjectMapper mapper;
    private final I18nService i18n;
    private final LocaleResolver localeResolver;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), ApiErrorDto.of(code,
                i18n.message(code.getMessageKey(), localeResolver.resolveLocale(request)),
                request.getRequestURI(), Map.of()));
    }
}
