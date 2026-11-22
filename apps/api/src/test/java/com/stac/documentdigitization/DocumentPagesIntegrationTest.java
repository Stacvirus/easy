package com.stac.documentdigitization;

import com.jayway.jsonpath.JsonPath;
import com.stac.documentdigitization.exceptions.StorageUnavailableException;
import com.stac.documentdigitization.repositories.DocumentPageRepository;
import com.stac.documentdigitization.repositories.ObjectDeletionRepository;
import com.stac.documentdigitization.services.ObjectCleanupService;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies page storage and order against PostgreSQL with an in-memory object adapter. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(DocumentPagesIntegrationTest.FakeStorageConfiguration.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///document_pages_test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "app.auth.jwt-secret=integration-test-secret-with-at-least-32-bytes",
        "app.storage.secret-key=integration-test-minio-secret",
        "app.storage.cleanup-enabled=false",
        "logging.level.org.springframework.web=INFO"
})
class DocumentPagesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FakeStorageService storage;
    @Autowired ObjectCleanupService cleanup;
    @Autowired ObjectDeletionRepository deletions;
    @Autowired DocumentPageRepository pages;

    @Test
    void uploadReorderPreviewAndDeletePersistAcrossRequests() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        byte[] image = png();
        String body = mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("first.png", image)).file(file("second.png", image))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[1].position").value(2))
                .andReturn().getResponse().getContentAsString();
        UUID first = UUID.fromString(JsonPath.read(body, "$[0].id"));
        UUID second = UUID.fromString(JsonPath.read(body, "$[1].id"));
        assertEquals(2, storage.countFor(documentId));
        pages.findAllByDocumentIdOrderByPositionAsc(documentId).forEach(page ->
                org.junit.jupiter.api.Assertions.assertFalse(page.getObjectKey().contains("first.png")));

        mvc.perform(put("/api/v1/documents/{id}/pages/order", documentId)
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content(order(second, first)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(second.toString()));
        mvc.perform(get("/api/v1/documents/{id}/pages", documentId)
                .header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$[0].id").value(second.toString()))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[1].id").value(first.toString()))
                .andExpect(jsonPath("$[1].position").value(2));
        assertArrayEquals(image, mvc.perform(get("/api/v1/documents/{id}/pages/{pageId}/content", documentId, first)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());

        mvc.perform(delete("/api/v1/documents/{id}/pages/{pageId}", documentId, second)
                .header("Authorization", bearer(owner))).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/documents/{id}/pages", documentId).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(first.toString()))
                .andExpect(jsonPath("$[0].position").value(1));
        assertEquals(1, deletions.findAll().stream()
                .filter(item -> item.getObjectKey().contains(documentId.toString())).count());
        cleanup.processPending();
        assertEquals(1, storage.countFor(documentId));

        mvc.perform(delete("/api/v1/documents/{id}", documentId)
                .header("Authorization", bearer(owner))).andExpect(status().isNoContent());
        cleanup.processPending();
        assertEquals(0, storage.countFor(documentId));
        assertEquals(0, pages.findAllByDocumentIdOrderByPositionAsc(documentId).size());
    }

    @Test
    void invalidImagesAndIncompleteOrdersAreRejected() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        byte[] image = png();
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("valid.png", image))
                .file(file("bad.png", "not-an-image".getBytes()))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_IMAGE"));
        assertEquals(0, storage.countFor(documentId));
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(new MockMultipartFile("files", "empty.png", "image/png", new byte[0]))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FILE_EMPTY"));
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(new MockMultipartFile("files", "text.txt", "text/plain", image))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value("FILE_TYPE_NOT_ALLOWED"));
        String body = mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("one.png", image)).file(file("two.png", image))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID first = UUID.fromString(JsonPath.read(body, "$[0].id"));
        UUID second = UUID.fromString(JsonPath.read(body, "$[1].id"));
        for (String invalid : new String[]{order(first), order(first, first), order(first, UUID.randomUUID())}) {
            mvc.perform(put("/api/v1/documents/{id}/pages/order", documentId)
                    .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                    .content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("PAGE_ORDER_INVALID"));
        }
        mvc.perform(get("/api/v1/documents/{id}/pages", documentId).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$[0].id").value(first.toString()))
                .andExpect(jsonPath("$[1].id").value(second.toString()));
    }

    @Test
    void pagesRemainPrivateToTheirDocumentOwner() throws Exception {
        String owner = register();
        String other = register();
        UUID documentId = createDocument(owner);
        String body = mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("private.png", png())).header("Authorization", bearer(owner)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID pageId = UUID.fromString(JsonPath.read(body, "$[0].id"));
        mvc.perform(get("/api/v1/documents/{id}/pages", documentId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/documents/{id}/pages/{pageId}/content", documentId, pageId)
                .header("Authorization", bearer(other))).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/documents/{id}/pages/order", documentId)
                .header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON)
                .content(order(pageId))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/documents/{id}/pages/{pageId}", documentId, pageId)
                .header("Authorization", bearer(other))).andExpect(status().isNotFound());
        assertEquals(1, storage.countFor(documentId));
    }

    @Test
    void failedUploadRollsBackPagesAndQueuesAttemptedObjectsForCleanup() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        storage.failOnSecondPut();
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("one.png", png())).file(file("two.png", png()))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("STORAGE_UNAVAILABLE"));
        assertEquals(0, pages.findAllByDocumentIdOrderByPositionAsc(documentId).size());
        assertEquals(2, deletions.findAll().stream()
                .filter(item -> item.getObjectKey().contains(documentId.toString())).count());
        cleanup.processPending();
        assertEquals(0, storage.countFor(documentId));
    }

    @Test
    void uploadLimitsRejectFilesBeforeWritingObjects() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        MockMultipartHttpServletRequestBuilder tooMany = multipart("/api/v1/documents/{id}/pages", documentId);
        tooMany.header("Authorization", bearer(owner));
        byte[] image = png();
        for (int index = 0; index < 51; index++) {
            tooMany.file(file("page.png", image));
        }
        mvc.perform(tooMany)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PAGE_LIMIT_EXCEEDED"));
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("large.png", new byte[10 * 1024 * 1024 + 1]))
                .header("Authorization", bearer(owner)))
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.errorCode").value("FILE_TOO_LARGE"));
        mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_PARAMETER"));
        assertEquals(0, storage.countFor(documentId));
    }

    @Test
    void jpegImageCanBeUploadedAndPreviewed() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        byte[] image = image("jpg");
        String body = mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(new MockMultipartFile("files", "photo.jpg", "image/jpeg", image))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].contentType").value("image/jpeg"))
                .andReturn().getResponse().getContentAsString();
        UUID pageId = UUID.fromString(JsonPath.read(body, "$[0].id"));
        assertArrayEquals(image, mvc.perform(get("/api/v1/documents/{id}/pages/{pageId}/content", documentId, pageId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    @Test
    void failedObjectDeletionStaysQueuedUntilRetrySucceeds() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        String body = mvc.perform(multipart("/api/v1/documents/{id}/pages", documentId)
                .file(file("page.png", png())).header("Authorization", bearer(owner)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID pageId = UUID.fromString(JsonPath.read(body, "$[0].id"));
        mvc.perform(delete("/api/v1/documents/{id}/pages/{pageId}", documentId, pageId)
                .header("Authorization", bearer(owner))).andExpect(status().isNoContent());
        storage.failNextDelete();
        cleanup.processPending();
        assertEquals(1, deletions.findAll().stream()
                .filter(item -> item.getObjectKey().contains(documentId.toString())).count());
        assertEquals(1, storage.countFor(documentId));
        cleanup.processPending();
        assertEquals(0, deletions.findAll().stream()
                .filter(item -> item.getObjectKey().contains(documentId.toString())).count());
        assertEquals(0, storage.countFor(documentId));
    }

    private String register() throws Exception {
        String email = "pages-" + UUID.randomUUID() + "@example.com";
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private UUID createDocument(String token) throws Exception {
        String body = mvc.perform(post("/api/v1/documents").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Notes\",\"documentDate\":\"2026-11-22\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private static MockMultipartFile file(String filename, byte[] bytes) {
        return new MockMultipartFile("files", filename, "image/png", bytes);
    }

    private static String order(UUID... ids) {
        return "{\"pageIds\":[" + String.join(",", Arrays.stream(ids)
                .map(id -> "\"" + id + "\"").toList()) + "]}";
    }

    private static String bearer(String token) { return "Bearer " + token; }

    private static byte[] png() throws IOException {
        return image("png");
    }

    private static byte[] image(String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, output);
        return output.toByteArray();
    }

    @TestConfiguration
    static class FakeStorageConfiguration {
        @Bean
        @Primary
        FakeStorageService fakeStorageService() {
            return new FakeStorageService();
        }
    }

    static class FakeStorageService implements StorageService {
        private final Map<String, byte[]> objects = new ConcurrentHashMap<>();
        private int putsUntilFailure = Integer.MAX_VALUE;
        private boolean failNextDelete;

        @Override
        public synchronized void put(String key, byte[] content, String contentType) {
            if (--putsUntilFailure == 0) {
                putsUntilFailure = Integer.MAX_VALUE;
                throw new StorageUnavailableException();
            }
            objects.put(key, content.clone());
        }

        @Override
        public byte[] get(String key) {
            byte[] bytes = objects.get(key);
            if (bytes == null) {
                throw new StorageUnavailableException();
            }
            return bytes.clone();
        }

        @Override
        public synchronized void delete(String key) {
            if (failNextDelete) {
                failNextDelete = false;
                throw new StorageUnavailableException();
            }
            objects.remove(key);
        }

        synchronized void failNextDelete() {
            failNextDelete = true;
        }

        synchronized void failOnSecondPut() {
            putsUntilFailure = 2;
        }

        long countFor(UUID documentId) {
            return objects.keySet().stream().filter(key -> key.contains(documentId.toString())).count();
        }
    }
}
