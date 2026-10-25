package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class ReclamoControllerTests {

    private ReclamoService reclamoService;
    private ReclamoController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        reclamoService = mock(ReclamoService.class);
        controller = new ReclamoController(reclamoService);
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                "vecino@example.com", null, List.of());
    }

    @Test
    void createsNewPendingClaimFromPublicFieldsOnly() throws Exception {
        when(reclamoService.registrarReclamo(
                org.mockito.ArgumentMatchers.any(Reclamo.class), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "Norte",
                        "NORTE",
                        "https://maps.google.com/",
                        "Hay un bache que dificulta el paso."),
                null,
                authentication);

        Reclamo savedClaim = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("PENDIENTE", savedClaim.getEstado());
        assertEquals("Bacheo", savedClaim.getCategoria());
        assertEquals("Norte", savedClaim.getBarrio());
        assertEquals("NORTE", savedClaim.getZona());
        assertNull(savedClaim.getId());
        assertNull(savedClaim.getFechaCreacion());
        assertNull(savedClaim.getCodigoSeguimiento());
    }

    @Test
    void createsNewPendingClaimFromJson() {
        when(reclamoService.registrarReclamo(
                org.mockito.ArgumentMatchers.any(Reclamo.class), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.crearReclamoJson(
                new ReclamoController.ReclamoJsonRequest(
                        "Alumbrado y Luminarias Serranas",
                        "Calle Los Cerros esquina Camino del Sol",
                        "Centro",
                        "CENTRO",
                        "",
                        "Falta de iluminación pública en la esquina."),
                authentication);

        Reclamo savedClaim = response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("PENDIENTE", savedClaim.getEstado());
        assertEquals("Alumbrado y Luminarias Serranas", savedClaim.getCategoria());
        assertEquals("Calle Los Cerros esquina Camino del Sol", savedClaim.getDireccion());
        assertEquals("Centro", savedClaim.getBarrio());
        assertEquals("CENTRO", savedClaim.getZona());
    }

    @Test
    void rejectsUnsupportedImageTypesBeforeSavingTheClaim() throws Exception {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.crearReclamo(
                        new ReclamoController.ReclamoRequest(
                                "Bacheo",
                                "Calle Belgrano 450",
                                "Norte",
                                "NORTE",
                                "",
                                "Hay un bache que dificulta el paso."),
                        new MockMultipartFile("imagen", "../../imagen.svg", "image/svg+xml", new byte[]{1}),
                        authentication));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(reclamoService, never()).guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class));
    }

    @Test
    void updatesClaimWithSupportedState() {
        Reclamo reclamo = new Reclamo(
                "Bacheo", "Calle Belgrano 450", "Norte", "NORTE", "", "Hay un bache que dificulta el paso.", "");
        reclamo.setId(12L);
        when(reclamoService.buscarPorId(12L)).thenReturn(Optional.of(reclamo));
        when(reclamoService.guardarReclamo(any(Reclamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("en_proceso"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EN_PROCESO", response.getBody().getEstado());
        verify(reclamoService).buscarPorId(12L);
        verify(reclamoService).guardarReclamo(reclamo);
    }

    @Test
    void rejectsUnsupportedClaimState() {
        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("CANCELADO"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reclamoService, never()).buscarPorId(any());
        verify(reclamoService, never()).guardarReclamo(any(Reclamo.class));
    }

    @Test
    void findsClaimByTrackingCode() {
        Reclamo reclamo = new Reclamo(
                "Bacheo", "Calle Belgrano 450", "Norte", "NORTE", "", "Hay un bache que dificulta el paso.", "");
        when(reclamoService.buscarPorCodigoSeguimiento("ABC123")).thenReturn(Optional.of(reclamo));

        var response = controller.consultarPorCodigo(" ABC123 ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reclamo, response.getBody());
        verify(reclamoService).buscarPorCodigoSeguimiento("ABC123");
    }

    @Test
    void returnsNotFoundForUnknownTrackingCode() {
        when(reclamoService.buscarPorCodigoSeguimiento("UNKNOWN")).thenReturn(Optional.empty());

        var response = controller.consultarPorCodigo("UNKNOWN");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void returnsNotFoundWhenClaimDoesNotExist() {
        when(reclamoService.buscarPorId(12L)).thenReturn(Optional.empty());

        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("RESUELTO"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reclamoService, never()).guardarReclamo(any(Reclamo.class));
    }
}
