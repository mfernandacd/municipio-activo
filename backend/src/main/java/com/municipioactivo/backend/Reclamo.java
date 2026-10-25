package com.municipioactivo.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

// Entidad JPA que representa un reclamo municipal en la base de datos.
@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categoria;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String direccion;

    @NotBlank(message = "El barrio es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String barrio;

    @NotBlank(message = "La zona es obligatoria")
    @Pattern(regexp = "(?i)^(NORTE|SUR|ESTE|OESTE|CENTRO|RURAL|SIN ESPECIFICAR)$")
    @Column(nullable = false, length = 20)
    private String zona;

    @Size(max = 500)
    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 15, message = "La descripción debe tener al menos 15 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_path", length = 255)
    private String imagenPath;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 20)
    private String codigoSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    // Asigna automáticamente la fecha de creación y el código de seguimiento antes de insertar el reclamo.
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.codigoSeguimiento == null || this.codigoSeguimiento.isBlank()) {
            this.codigoSeguimiento = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
    }

    public Reclamo() {}

    public Reclamo(
            String categoria,
            String direccion,
            String barrio,
            String zona,
            String googleMapsUrl,
            String descripcion,
            String imagenPath) {
        this.categoria = categoria;
        this.direccion = direccion;
        this.barrio = barrio;
        this.zona = zona;
        this.googleMapsUrl = googleMapsUrl;
        this.descripcion = descripcion;
        this.imagenPath = imagenPath;
    }

    // Métodos de acceso utilizados por JPA, Jackson y el resto de la aplicación.
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenPath() { return imagenPath; }
    public void setImagenPath(String imagenPath) { this.imagenPath = imagenPath; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    @JsonIgnore
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
