package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {
    List<TipoDocumento> findAllByVigenteTrue();
}
