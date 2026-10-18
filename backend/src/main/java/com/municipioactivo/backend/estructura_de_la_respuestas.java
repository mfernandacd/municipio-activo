package com.municipioactivo.dto;

public class ReclamoResponseDTO {
    private Long id;
    private String codigoSeguimiento;
    private String categoria;
    private String direccion;
    private String descripcion;
    private String estado;

    // Constructores, Getters y Setters
    public ReclamoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}