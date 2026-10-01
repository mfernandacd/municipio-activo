package com.municipioactivo.backend.model; // O el paquete exacto que tenga tu archivo

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NUEVOS CAMPOS REQUERIDOS POR LA HU-3 ---
    @Column(name = "codigo_seguimiento", unique = true, length = 20)
    private String codigoSeguimiento;

    @Column(name = "contribuyente_email")
    private String contribuyenteEmail;

    // --- CAMPOS QUE YA TIENE TU PROYECTO ---
    private String categoria;
    private String direccion;
    
    @Column(name = "google_maps_url")
    private String googleMapsUrl;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "imagen_path")
    private String imagenPath;
    
    private String estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
    }

    public Reclamo() {}

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public String getContribuyenteEmail() { return contribuyenteEmail; }
    public void setContribuyenteEmail(String contribuyenteEmail) { this.contribuyenteEmail = contribuyenteEmail; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

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
}