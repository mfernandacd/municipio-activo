package com.municipioactivo.backend;

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

// Entidad JPA que representa un reclamo municipal en la base de datos.
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
    // Asigna automáticamente la fecha de creación antes de insertar el reclamo.
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

    // Métodos de acceso utilizados por JPA, Jackson y el resto de la aplicación.
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

// Repositorio Spring Data que permite guardar y consultar reclamos.
@Repository
interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    // Busca reclamos que tengan el estado indicado.
    List<Reclamo> findByEstado(String estado);
}

// Define las operaciones de negocio disponibles para los reclamos.
interface ReclamoService {
    Reclamo guardarReclamo(Reclamo reclamo);
    List<Reclamo> obtenerTodosLosReclamos();
    List<Reclamo> obtenerReclamosPendientes();
    List<Reclamo> obtenerReclamosPorEstado(String estado);
}

// Implementa la lógica de negocio delegando la persistencia en el repositorio.
@Service
class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;

    @Autowired
    public ReclamoServiceImpl(ReclamoRepository reclamoRepository) {
        this.reclamoRepository = reclamoRepository;
    }

    @Override
    @Transactional
    // Persiste un reclamo nuevo o actualiza uno existente.
    public Reclamo guardarReclamo(Reclamo reclamo) {
        return reclamoRepository.save(reclamo);
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene todos los reclamos almacenados.
    public List<Reclamo> obtenerTodosLosReclamos() {
        return reclamoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene únicamente los reclamos pendientes.
    public List<Reclamo> obtenerReclamosPendientes() {
        return reclamoRepository.findByEstado("PENDIENTE");
    }

    @Override
    @Transactional(readOnly = true)
    // Normaliza el estado recibido y obtiene los reclamos correspondientes.
    public List<Reclamo> obtenerReclamosPorEstado(String estado) {
        return reclamoRepository.findByEstado(estado.toUpperCase());
    }
}