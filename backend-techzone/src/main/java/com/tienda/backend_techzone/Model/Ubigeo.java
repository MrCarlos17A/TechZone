package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubigeo")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubigeo {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id_ubigeo", nullable = false)
    private Long idUbigeo;

    @Column(length = 50)
    private String departamento;

    @Column(length = 50)
    private String provincia;

    @Column(length = 50)
    private String distrito;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;

    // Getters y Setters explícitos para compatibilidad con cualquier IDE sin plugin Lombok
    public Long getIdUbigeo() { return idUbigeo; }
    public void setIdUbigeo(Long idUbigeo) { this.idUbigeo = idUbigeo; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }
}
