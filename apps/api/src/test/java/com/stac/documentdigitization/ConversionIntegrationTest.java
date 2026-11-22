package com.stac.documentdigitization;

import com.jayway.jsonpath.JsonPath;
import com.stac.documentdigitization.clients.ocr.RecognizedLineDto;
import com.stac.documentdigitization.clients.ocr.RecognizedPageDto;
import com.stac.documentdigitization.exceptions.OcrUnavailableException;
import com.stac.documentdigitization.exceptions.StorageUnavailableException;
import com.stac.documentdigitization.services.ConversionJobStateService;
import com.stac.documentdigitization.services.ConversionProcessor;
import com.stac.documentdigitization.services.interfaces.OcrServiceClient;
import com.stac.documentdigitization.services.interfaces.StorageService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Checks the persisted job lifecycle with isolated PostgreSQL and fake external adapters. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ConversionIntegrationTest.FakeAdapters.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///document_conversions_test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "app.auth.jwt-secret=integration-test-secret-with-at-least-32-bytes",
        "app.storage.secret-key=integration-test-minio-secret",
        "app.storage.cleanup-enabled=false",
        "app.ocr.token=integration-test-ocr-token",
        "app.conversion.worker-enabled=false",
        "logging.level.org.springframework.web=INFO"
})
class ConversionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ConversionProcessor processor;
    @Autowired ConversionJobStateService states;
    @Autowired FakeStorage storage;
    @Autowired FakeOcr ocr;

    @Test
    void conversionUsesSubmittedOrderAndKeepsPreviousExportsImmutable() throws Exception {
        String owner = register();
        String other = register();
        UUID documentId = createDocument(owner);
        byte[] firstImage = png(0x112233);
        byte[] secondImage = png(0x445566);
        List<UUID> pageIds = upload(owner, documentId, firstImage, secondImage);
        String firstJobBody = mvc.perform(post("/api/v1/documents/{id}/conversions", documentId)
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"fr\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        UUID firstJob = UUID.fromString(JsonPath.read(firstJobBody, "$.id"));
        mvc.perform(post("/api/v1/documents/{id}/conversions", documentId)
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"en\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(firstJob.toString()));
        mvc.perform(delete("/api/v1/documents/{id}/pages/{pageId}", documentId, pageIds.get(0))
                .header("Authorization", bearer(owner)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONVERSION_IN_PROGRESS"));
        mvc.perform(delete("/api/v1/documents/{id}", documentId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/v1/documents/{id}/pages/order", documentId)
                .header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                .content(order(pageIds.get(1), pageIds.get(0))))
                .andExpect(status().isOk());
        ocr.clear();
        assertEquals(true, processor.processNext());
        assertArrayEquals(firstImage, ocr.images.get(0));
        assertArrayEquals(secondImage, ocr.images.get(1));
        assertEquals(List.of(pageIds.get(0), pageIds.get(1)), ocr.pages.get(0).stream()
                .map(RecognizedPageDto::pageId).toList());
        mvc.perform(get("/api/v1/conversions/{id}", firstJob).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        String exportBody = mvc.perform(get("/api/v1/documents/{id}/exports", documentId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].objectKey").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        UUID firstExport = UUID.fromString(JsonPath.read(exportBody, "$[0].id"));
        byte[] firstDocx = mvc.perform(get("/api/v1/documents/{id}/exports/{exportId}/download", documentId, firstExport)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertFalse(firstDocx.length == 0);
        mvc.perform(get("/api/v1/conversions/{id}", firstJob).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/documents/{id}/exports/{exportId}/download", documentId, firstExport)
                .header("Authorization", bearer(other))).andExpect(status().isNotFound());

        UUID secondJob = submit(owner, documentId);
        assertNotEquals(firstJob, secondJob);
        ocr.clear();
        processor.processNext();
        assertArrayEquals(secondImage, ocr.images.get(0));
        assertArrayEquals(firstImage, ocr.images.get(1));
        mvc.perform(get("/api/v1/documents/{id}/exports", documentId).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$", hasSize(2)));
        assertArrayEquals(firstDocx, mvc.perform(
                get("/api/v1/documents/{id}/exports/{exportId}/download", documentId, firstExport)
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    @Test
    void failedJobsCanBeResubmittedAndInterruptedJobsAreRequeued() throws Exception {
        String owner = register();
        UUID documentId = createDocument(owner);
        mvc.perform(post("/api/v1/documents/{id}/conversions", documentId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DOCUMENT_HAS_NO_PAGES"));
        upload(owner, documentId, png(0x123456));
        UUID failedJob = submit(owner, documentId);
        ocr.failNextRecognition();
        processor.processNext();
        mvc.perform(get("/api/v1/conversions/{id}", failedJob)
                .header("Authorization", bearer(owner)).header("Accept-Language", "fr"))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureCode").value("OCR_UNAVAILABLE"))
                .andExpect(jsonPath("$.failureMessage").value("Le service de reconnaissance est temporairement indisponible"));
        UUID retry = submit(owner, documentId);
        assertNotEquals(failedJob, retry);
        assertEquals(retry, states.claimNext().jobId());
        mvc.perform(get("/api/v1/conversions/{id}", retry).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
        states.requeueInterrupted();
        mvc.perform(get("/api/v1/conversions/{id}", retry).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$.status").value("PENDING"));
        processor.processNext();
        mvc.perform(get("/api/v1/conversions/{id}", retry).header("Authorization", bearer(owner)))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private String register() throws Exception {
        String email = "conversion-" + UUID.randomUUID() + "@example.com";
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private UUID createDocument(String token) throws Exception {
        String body = mvc.perform(post("/api/v1/documents").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Conversion test\",\"documentDate\":\"2026-11-22\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private List<UUID> upload(String token, UUID documentId, byte[]... images) throws Exception {
        var request = multipart("/api/v1/documents/{id}/pages", documentId);
        request.header("Authorization", bearer(token));
        for (int index = 0; index < images.length; index++) {
            request.file(new MockMultipartFile("files", "page-" + index + ".png", "image/png", images[index]));
        }
        String body = mvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        List<UUID> ids = new ArrayList<>();
        for (int index = 0; index < images.length; index++) {
            ids.add(UUID.fromString(JsonPath.read(body, "$[" + index + "].id")));
        }
        return ids;
    }

    private UUID submit(String token, UUID documentId) throws Exception {
        String body = mvc.perform(post("/api/v1/documents/{id}/conversions", documentId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private static String order(UUID... ids) {
        return "{\"pageIds\":[" + String.join(",", Arrays.stream(ids)
                .map(id -> "\"" + id + "\"").toList()) + "]}";
    }

    private static String bearer(String token) { return "Bearer " + token; }

    private static byte[] png(int rgb) throws IOException {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, rgb);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    @TestConfiguration
    static class FakeAdapters {
        @Bean @Primary FakeStorage fakeStorage() { return new FakeStorage(); }
        @Bean @Primary FakeOcr fakeOcr() { return new FakeOcr(); }
    }

    static class FakeStorage implements StorageService {
        final Map<String, byte[]> objects = new ConcurrentHashMap<>();

        public void put(String key, byte[] content, String contentType) { objects.put(key, content.clone()); }
        public byte[] get(String key) {
            byte[] content = objects.get(key);
            if (content == null) throw new StorageUnavailableException();
            return content.clone();
        }
        public void delete(String key) { objects.remove(key); }
    }

    static class FakeOcr implements OcrServiceClient {
        final List<byte[]> images = new ArrayList<>();
        final List<List<RecognizedPageDto>> pages = new ArrayList<>();
        boolean failNext;

        public List<RecognizedLineDto> recognize(byte[] image, String contentType, String language) {
            if (failNext) {
                failNext = false;
                throw new OcrUnavailableException();
            }
            images.add(image.clone());
            return List.of(new RecognizedLineDto("line " + images.size(), null, null));
        }

        public byte[] renderDocx(List<RecognizedPageDto> recognized) {
            pages.add(List.copyOf(recognized));
            return ("fake-docx-" + recognized.stream().map(page -> page.pageId().toString()).toList())
                    .getBytes(StandardCharsets.UTF_8);
        }

        void failNextRecognition() { failNext = true; }
        void clear() { images.clear(); pages.clear(); }
    }
}
