package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    List<TipoDocumento> findAllByVigenteTrue();
}
