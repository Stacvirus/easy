package com.stac.documentdigitization.services;

import com.stac.documentdigitization.exceptions.FileEmptyException;
import com.stac.documentdigitization.exceptions.FileTooLargeException;
import com.stac.documentdigitization.exceptions.FileTypeNotAllowedException;
import com.stac.documentdigitization.exceptions.InvalidImageException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Checks declared type, actual decoder format, dimensions and byte limit. */
@Service
public class ImageUploadValidator {
    public static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final long MAX_PIXELS = 25_000_000;

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileEmptyException();
        }
        if (file.getSize() > MAX_BYTES) {
            throw new FileTooLargeException();
        }
        String declaredType = file.getContentType();
        if (!"image/jpeg".equals(declaredType) && !"image/png".equals(declaredType)) {
            throw new FileTypeNotAllowedException();
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException error) {
            throw new InvalidImageException();
        }
        if (bytes.length > MAX_BYTES) {
            throw new FileTooLargeException();
        }
        try (MemoryCacheImageInputStream stream =
                     new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new InvalidImageException();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream);
                String actualType = reader.getFormatName().toLowerCase(Locale.ROOT);
                String expectedType = declaredType.equals("image/png") ? "png" : "jpeg";
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (!expectedType.equals(actualType) || width < 1 || height < 1
                        || (long) width * height > MAX_PIXELS || reader.read(0) == null) {
                    throw new InvalidImageException();
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException error) {
            throw new InvalidImageException();
        }
        return new ValidatedImage(bytes, declaredType, declaredType.equals("image/png") ? "png" : "jpg");
    }

    public record ValidatedImage(byte[] bytes, String contentType, String extension) {}
}
