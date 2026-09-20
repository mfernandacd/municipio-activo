package com.municipio.serranoble.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

// 1. ENTIDAD (Clase pública principal)
@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false, length = 50)
    private String categoria;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false, length = 255)
    private String direccion;

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

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Reclamo() {}

    public Reclamo(String categoria, String direccion, String googleMapsUrl, String descripcion, String imagenPath) {
        this.categoria = categoria;
        this.direccion = direccion;
        this.googleMapsUrl = googleMapsUrl;
        this.descripcion = descripcion;
        this.imagenPath = imagenPath;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

// 2. REPOSITORIO (Sin modifier 'public' para ir en el mismo archivo)
@Repository
interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findByEstado(String estado);
}

// 3. INTERFAZ DE SERVICIO
interface ReclamoService {
    Reclamo guardarReclamo(Reclamo reclamo);
    List<Reclamo> obtenerTodosLosReclamos();
    List<Reclamo> obtenerReclamosPendientes();
}

// 4. IMPLEMENTACIÓN DEL SERVICIO
@Service
class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;

    @Autowired
    public ReclamoServiceImpl(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }

    @Override
    @Transactional
    public Reclamo guardarReclamo(Reclamo reclamo) {
        return reclamoRepository.save(reclamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerTodosLosReclamos() {
        return reclamoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reclamo> obtenerReclamosPendientes() {
        return reclamoRepository.findByEstado("PENDIENTE");
    }
}