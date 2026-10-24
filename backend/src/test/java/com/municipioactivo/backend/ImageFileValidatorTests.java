package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class ImageFileValidatorTests {

    @Test
    void acceptsJpegWithMatchingContentTypeAndSignature() {
        MockMultipartFile image = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});

        assertEquals(".jpg", ImageFileValidator.validateAndGetExtension(image));
    }

    @Test
    void rejectsSpoofedImageContentType() {
        MockMultipartFile image = new MockMultipartFile(
                "imageFile", "photo.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> ImageFileValidator.validateAndGetExtension(image));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsImagesOverFiveMegabytes() {
        byte[] content = new byte[5 * 1024 * 1024 + 1];
        content[0] = (byte) 0xff;
        content[1] = (byte) 0xd8;
        content[2] = (byte) 0xff;
        MockMultipartFile image = new MockMultipartFile("imageFile", "large.jpg", "image/jpeg", content);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> ImageFileValidator.validateAndGetExtension(image));

        assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatusCode());
    }
}
