package com.municipioactivo.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    private String emailUsuario;
    private String codigoSeguimiento;

    public Reclamo() {}

    public Reclamo(String titulo, String descripcion, String emailUsuario, String codigoSeguimiento) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.emailUsuario = emailUsuario;
        this.codigoSeguimiento = codigoSeguimiento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }
}