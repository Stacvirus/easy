package com.stac.documentdigitization;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises authentication, ownership and migrations against isolated PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///document_digitization_test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "app.auth.jwt-secret=integration-test-secret-with-at-least-32-bytes",
        "app.storage.secret-key=integration-test-minio-secret",
        "app.storage.cleanup-enabled=false",
        "app.ocr.token=integration-test-ocr-token",
        "app.conversion.worker-enabled=false",
        "logging.level.org.springframework.web=INFO",
        "logging.level.org.hibernate.SQL=INFO"
})
class IdentityAndDocumentsIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void registrationAndLoginRejectDuplicatesAndWrongPasswords() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        MvcResult registration = mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email.toUpperCase() + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String token = JsonPath.read(registration.getResponse().getContentAsString(), "$.accessToken");
        org.junit.jupiter.api.Assertions.assertFalse(token.isBlank());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_EXISTS"));
    }

    @Test
    void passwordLongerThanBcryptByteLimitHasItsOwnError() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/v1/auth/register")
                .header("Accept-Language", "fr")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + "é".repeat(37) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_TOO_LONG"))
                .andExpect(jsonPath("$.message").value("Le mot de passe est trop long"));
    }

    @Test
    void usersCannotReadOrModifyOtherUsersSubjectsAndDocuments() throws Exception {
        String owner = register();
        String other = register();
        UUID subjectId = UUID.fromString(JsonPath.read(mvc.perform(post("/api/v1/subjects")
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mathematics\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id"));

        mvc.perform(get("/api/v1/subjects/{id}", subjectId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/subjects").header("Authorization", bearer(other)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(post("/api/v1/documents").header("Authorization", bearer(other))
                .contentType(MediaType.APPLICATION_JSON)
                .content(documentBody(subjectId, "Other user's subject")))
                .andExpect(status().isNotFound());

        UUID documentId = UUID.fromString(JsonPath.read(mvc.perform(post("/api/v1/documents")
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content(documentBody(subjectId, "Lecture 1")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id"));
        mvc.perform(get("/api/v1/documents/{id}", documentId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/documents/{id}", documentId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/documents").header("Authorization", bearer(other)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(delete("/api/v1/subjects/{id}", subjectId).header("Authorization", bearer(owner)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("SUBJECT_IN_USE"));
    }

    @Test
    void documentLibraryFiltersBySubjectAndDateAndAcceptsGeneralDocuments() throws Exception {
        String owner = register();
        UUID subjectId = UUID.fromString(JsonPath.read(mvc.perform(post("/api/v1/subjects")
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"History\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id"));
        mvc.perform(post("/api/v1/documents").header("Authorization", bearer(owner))
                .contentType(MediaType.APPLICATION_JSON).content(documentBody(subjectId, "History notes")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/documents").header("Authorization", bearer(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"General scan\",\"documentDate\":\"2026-11-21\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.subjectId").doesNotExist());

        mvc.perform(get("/api/v1/documents").header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/v1/documents").param("subjectId", subjectId.toString())
                .param("date", "2026-11-22").header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("History notes"));
        mvc.perform(get("/api/v1/documents").param("date", "2026-11-20")
                .header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void subjectsCanBeRenamedAndDeletedWhenUnused() throws Exception {
        String owner = register();
        UUID subjectId = UUID.fromString(JsonPath.read(mvc.perform(post("/api/v1/subjects")
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" Biology \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Biology"))
                .andReturn().getResponse().getContentAsString(), "$.id"));
        mvc.perform(patch("/api/v1/subjects/{id}", subjectId).header("Authorization", bearer(owner))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Chemistry\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Chemistry"));
        mvc.perform(post("/api/v1/subjects").header("Authorization", bearer(owner))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"chemistry\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("SUBJECT_EXISTS"));
        mvc.perform(delete("/api/v1/subjects/{id}", subjectId).header("Authorization", bearer(owner)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/subjects/{id}", subjectId).header("Authorization", bearer(owner)))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedEndpointsAndInvalidBodiesAreRejected() throws Exception {
        mvc.perform(get("/api/v1/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.path").value("/api/v1/documents"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
        mvc.perform(get("/api/v1/documents").header("X-Correlation-Id", "invalid id with spaces"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
        mvc.perform(get("/api/v1/documents").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHENTICATED"));
        String owner = register();
        mvc.perform(post("/api/v1/documents").header("Authorization", bearer(owner))
                .header("X-Correlation-Id", "validation-check")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.correlationId").value("validation-check"));
        mvc.perform(put("/api/v1/documents").header("Authorization", bearer(owner)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
        mvc.perform(get("/api/v1/documents").param("date", "not-a-date")
                .header("Authorization", bearer(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PARAMETER"));
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.schemas.SubjectResponseDto").exists());
    }

    @Test
    void errorsUseFrenchWhenRequestedAndEnglishOtherwise() throws Exception {
        mvc.perform(get("/api/v1/documents").header("Accept-Language", "fr-FR"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));
        mvc.perform(get("/api/v1/documents").header("Accept-Language", "es"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
        mvc.perform(get("/api/v1/documents"))
                .andExpect(jsonPath("$.message").value("Authentication required"));

        String owner = register();
        mvc.perform(post("/api/v1/subjects").header("Authorization", bearer(owner))
                .header("Accept-Language", "fr")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requête invalide"))
                .andExpect(jsonPath("$.fields.name").value("Ce champ est obligatoire"));
        mvc.perform(get("/api/v1/subjects/{id}", UUID.randomUUID())
                .header("Authorization", bearer(owner)).header("Accept-Language", "fr"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Matière introuvable"));
    }

    private String register() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String response = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private static String bearer(String token) { return "Bearer " + token; }

    private static String documentBody(UUID subjectId, String title) {
        return "{\"title\":\"" + title + "\",\"documentDate\":\"2026-11-22\",\"subjectId\":\"" + subjectId + "\"}";
    }
}
