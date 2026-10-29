package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.imagenes " +
           "WHERE p.categoria.idCategoria = :idCategoria AND (p.vigente IS NULL OR p.vigente = true)")
    List<Producto> findByCategoriaIdCategoria(@Param("idCategoria") Long idCategoria);

    @Query("SELECT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.imagenes " +
           "WHERE p.marca.idMarca = :idMarca AND (p.vigente IS NULL OR p.vigente = true)")
    List<Producto> findByMarcaIdMarca(@Param("idMarca") Long idMarca);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.imagenes " +
           "WHERE p.vigente IS NULL OR p.vigente = true")
    List<Producto> findByVigenteTrue();

    @Query("SELECT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.imagenes " +
           "WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.marca.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.categoria.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "AND (p.vigente IS NULL OR p.vigente = true)")
    List<Producto> buscarPorTexto(@Param("texto") String texto);

    @Query("SELECT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.imagenes " +
           "WHERE p.idProducto = :id")
    java.util.Optional<Producto> findByIdConRelaciones(@Param("id") Long id);

    long countByMarcaIdMarcaAndCategoriaIdCategoria(Integer idMarca, Integer idCategoria);

    boolean existsByCodigoSku(String codigoSku);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    java.util.Optional<Producto> findByIdConBloqueo(@Param("id") Long id);
}
