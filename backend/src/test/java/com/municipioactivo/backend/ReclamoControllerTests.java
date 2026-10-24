package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class ReclamoControllerTests {

    private ReclamoService reclamoService;
    private EmailService emailService;
    private ReclamoController controller;

    @BeforeEach
    void setUp() {
        reclamoService = mock(ReclamoService.class);
        emailService = mock(EmailService.class);
        controller = new ReclamoController(reclamoService, emailService);
    }

    @Test
    void createsClaimAndSendsConfirmationToAuthenticatedUsersEmail() throws Exception {
        when(reclamoService.guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class)))
                .thenAnswer(invocation -> {
                    Reclamo saved = invocation.getArgument(0);
                    saved.setCodigoSeguimiento("ABC123DEF456");
                    return saved;
                });
        when(emailService.enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456")).thenReturn(true);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "usuario@example.com", null, List.of());

        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "https://maps.google.com/",
                        "Hay un bache que dificulta el paso."),
                null,
                authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ABC123DEF456", response.getBody().codigoSeguimiento());
        assertTrue(response.getBody().correoEnviado());
        ArgumentCaptor<Reclamo> claimCaptor = ArgumentCaptor.forClass(Reclamo.class);
        verify(reclamoService).guardarReclamo(claimCaptor.capture());
        assertEquals("PENDIENTE", claimCaptor.getValue().getEstado());
        assertEquals("Bacheo", claimCaptor.getValue().getCategoria());
        verify(emailService).enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456");
    }

    @Test
    void keepsClaimRegisteredWhenConfirmationEmailCannotBeSent() throws Exception {
        when(reclamoService.guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class)))
                .thenAnswer(invocation -> {
                    Reclamo saved = invocation.getArgument(0);
                    saved.setCodigoSeguimiento("ABC123DEF456");
                    return saved;
                });
        when(emailService.enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456")).thenReturn(false);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "usuario@example.com", null, List.of());

        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "",
                        "Hay un bache que dificulta el paso."),
                null,
                authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ABC123DEF456", response.getBody().codigoSeguimiento());
        assertFalse(response.getBody().correoEnviado());
    }

    @Test
    void rejectsUnsupportedImageTypesBeforeSavingTheClaim() throws Exception {
        var response = controller.crearReclamo(
                new ReclamoController.ReclamoRequest(
                        "Bacheo",
                        "Calle Belgrano 450",
                        "",
                        "Hay un bache que dificulta el paso."),
                new MockMultipartFile("imagen", "../../imagen.svg", "image/svg+xml", new byte[]{1}),
                UsernamePasswordAuthenticationToken.authenticated(
                        "usuario@example.com", null, List.of()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reclamoService, never()).guardarReclamo(org.mockito.ArgumentMatchers.any(Reclamo.class));
    }

    @Test
    void updatesClaimWithSupportedState() {
        Reclamo reclamo = new Reclamo("Bacheo", "Calle Belgrano 450", "", "Hay un bache que dificulta el paso.", "");
        reclamo.setId(12L);
        when(reclamoService.obtenerTodosLosReclamos()).thenReturn(List.of(reclamo));
        when(reclamoService.guardarReclamo(any(Reclamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("en_proceso"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EN_PROCESO", response.getBody().getEstado());
        verify(reclamoService).guardarReclamo(reclamo);
    }

    @Test
    void rejectsUnsupportedClaimState() {
        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("CANCELADO"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reclamoService, never()).obtenerTodosLosReclamos();
        verify(reclamoService, never()).guardarReclamo(any(Reclamo.class));
    }

    @Test
    void findsClaimByTrackingCode() {
        Reclamo reclamo = new Reclamo("Bacheo", "Calle Belgrano 450", "", "Hay un bache que dificulta el paso.", "");
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
        when(reclamoService.obtenerTodosLosReclamos()).thenReturn(List.of());

        var response = controller.actualizarEstadoReclamo(12L, new ReclamoController.EstadoRequest("RESUELTO"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(reclamoService, never()).guardarReclamo(any(Reclamo.class));
    }
}
