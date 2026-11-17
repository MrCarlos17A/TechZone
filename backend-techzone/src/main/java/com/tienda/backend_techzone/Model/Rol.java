package com.tienda.backend_techzone.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rol {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol", nullable = false)
    private Long idRol;

    @Column(length = 50)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;

    @Column(name = "es_administrativo")
    @Builder.Default
    private Boolean esAdministrativo = false;

    @Column(name = "permisos", columnDefinition = "TEXT")
    private String permisos;

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }

    public Boolean getEsAdministrativo() { return esAdministrativo; }
    public void setEsAdministrativo(Boolean esAdministrativo) { this.esAdministrativo = esAdministrativo; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }
}
