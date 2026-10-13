package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaIdCategoria(Long idCategoria);
    List<Producto> findByMarcaIdMarca(Long idMarca);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByVigenteTrue();

    @Query("SELECT p FROM Producto p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.marca.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Producto> buscarPorTexto(@Param("texto") String texto);

    long countByMarcaIdMarcaAndCategoriaIdCategoria(Integer idMarca, Integer idCategoria);

    boolean existsByCodigoSku(String codigoSku);
}
