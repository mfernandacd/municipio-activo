package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

class ReclamoControllerTests {

    private ReclamoService reclamoService;
    private ReclamoController controller;

    @BeforeEach
    void setUp() {
        reclamoService = mock(ReclamoService.class);
        controller = new ReclamoController(reclamoService);
    }

    @Test
    void createsNewPendingClaimFromPublicFieldsOnly() throws Exception {
        when(reclamoService.guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "https://maps.google.com/",
                        "Hay un bache que dificulta el paso."),
                null);

        Reclamo savedClaim = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("PENDIENTE", savedClaim.getEstado());
        assertEquals("Bacheo", savedClaim.getCategoria());
        assertNull(savedClaim.getId());
        assertNull(savedClaim.getFechaCreacion());
        assertNull(savedClaim.getCodigoSeguimiento());
    }

    @Test
    void rejectsUnsupportedImageTypesBeforeSavingTheClaim() throws Exception {
        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "",
                        "Hay un bache que dificulta el paso."),
                new MockMultipartFile("imagen", "../../imagen.svg", "image/svg+xml", new byte[]{1}));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reclamoService, never()).guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class));
    }
}
