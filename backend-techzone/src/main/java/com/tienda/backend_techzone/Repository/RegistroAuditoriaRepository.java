package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
    List<RegistroAuditoria> findTop100ByOrderByFechaHoraDesc();

    List<RegistroAuditoria> findByModuloOrderByFechaHoraDesc(String modulo);
}
