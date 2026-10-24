package com.municipioactivo.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

final class ImageFileValidator {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private ImageFileValidator() {
    }

    static String validateAndGetExtension(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "La imagen no puede superar los 5 MB.");
        }

        String contentType = file.getContentType();
        String extension = contentType == null ? null : switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> null;
        };

        if (extension == null || !hasValidSignature(file, extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El archivo debe ser una imagen JPG, PNG, GIF o WebP válida.");
        }
        return extension;
    }

    private static boolean hasValidSignature(MultipartFile file, String extension) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            return switch (extension) {
                case ".jpg" -> startsWith(header, (byte) 0xff, (byte) 0xd8, (byte) 0xff);
                case ".png" -> startsWith(header,
                        (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                        (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a);
                case ".gif" -> asciiAt(header, 0, "GIF87a") || asciiAt(header, 0, "GIF89a");
                case ".webp" -> asciiAt(header, 0, "RIFF") && asciiAt(header, 8, "WEBP");
                default -> false;
            };
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo de imagen.",
                    exception);
        }
    }

    private static boolean startsWith(byte[] value, byte... prefix) {
        if (value.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) return false;
        }
        return true;
    }

    private static boolean asciiAt(byte[] value, int offset, String expected) {
        byte[] bytes = expected.getBytes(StandardCharsets.US_ASCII);
        if (value.length < offset + bytes.length) return false;
        for (int index = 0; index < bytes.length; index++) {
            if (value[offset + index] != bytes[index]) return false;
        }
        return true;
    }
}
