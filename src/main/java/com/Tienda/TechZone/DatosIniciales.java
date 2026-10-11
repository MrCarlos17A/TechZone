package com.Tienda.TechZone;

import com.Tienda.TechZone.Model.*;
import com.Tienda.TechZone.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            rolRepository.save(Rol.builder().nombre("Administrador").descripcion("Acceso total al sistema").build());
            rolRepository.save(Rol.builder().nombre("Trabajador").descripcion("Gestion de productos y pedidos").build());
            rolRepository.save(Rol.builder().nombre("Cliente").descripcion("Compra y navegacion").build());
        }

        if (marcaRepository.count() == 0) {
            marcaRepository.save(Marca.builder().nombre("ASUS").imagen("/Assets/IMG/Marcas/ASUS.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("MSI").imagen("/Assets/IMG/Marcas/MSI.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("GIGABYTE").imagen("/Assets/IMG/Marcas/GIGABYTE.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("CORSAIR").imagen("/Assets/IMG/Marcas/CORSAIR.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("REDRAGON").imagen("/Assets/IMG/Marcas/REDRAGON.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("Logitech").imagen("/Assets/IMG/Marcas/Logitech.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("Kingston").imagen("/Assets/IMG/Marcas/Kingston.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("ALIENWARE").imagen("/Assets/IMG/Marcas/ALIENWARE.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("TOSHIBA").imagen("/Assets/IMG/Marcas/TOSHIBA.png").estadoMarca(true).build());
            marcaRepository.save(Marca.builder().nombre("MICRONICS").imagen("/Assets/IMG/Marcas/MICRONICS.png").estadoMarca(true).build());
        }

        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(Categoria.builder().nombre("Monitores").descripcion("Monitores para gaming y oficina").estadoCategoria(true).build());
            categoriaRepository.save(Categoria.builder().nombre("Tarjetas de Video").descripcion("GPU para gaming y renderizado").estadoCategoria(true).build());
        }

        if (productoRepository.count() == 0) {
            Marca asus = marcaRepository.findAll().stream().filter(m -> m.getNombre().equals("ASUS")).findFirst().orElse(null);
            Marca msi = marcaRepository.findAll().stream().filter(m -> m.getNombre().equals("MSI")).findFirst().orElse(null);
            Marca gigabyte = marcaRepository.findAll().stream().filter(m -> m.getNombre().equals("GIGABYTE")).findFirst().orElse(null);

            Categoria monitores = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Monitores")).findFirst().orElse(null);
            Categoria tarjetas = categoriaRepository.findAll().stream().filter(c -> c.getNombre().equals("Tarjetas de Video")).findFirst().orElse(null);

            // Monitores
            if (asus != null && monitores != null) {
                productoRepository.save(Producto.builder().marca(asus).categoria(monitores).nombre("ASUS ROG STRIX XG27ACMES 27\" QHD 120HZ").descripcion("Monitor gaming ASUS ROG STRIX 27 pulgadas, resolucion QHD, 120Hz").precio(new java.math.BigDecimal("273.73")).stock(15).imagen("/Assets/IMG/Produtos/Monitores/ASUS 27.png").estadoProducto(true).build());
                productoRepository.save(Producto.builder().marca(asus).categoria(monitores).nombre("ASUS TUF GAMING VG34VQL3A 34\" CURVO WQHD 180HZ").descripcion("Monitor curvo ASUS TUF 34 pulgadas, WQHD, 180Hz").precio(new java.math.BigDecimal("359.00")).stock(5).imagen("/Assets/IMG/Produtos/Monitores/ASUS 34.png").estadoProducto(true).build());
            }
            if (msi != null && monitores != null) {
                productoRepository.save(Producto.builder().marca(msi).categoria(monitores).nombre("MSI MAG 271QPX QD-OLED 27\" WQHD 250HZ").descripcion("Monitor MSI con panel QD-OLED, 27 pulgadas, WQHD, 250Hz").precio(new java.math.BigDecimal("258.26")).stock(10).imagen("/Assets/IMG/Produtos/Monitores/MSI 27.png").estadoProducto(true).build());
            }
            if (gigabyte != null && monitores != null) {
                productoRepository.save(Producto.builder().marca(gigabyte).categoria(monitores).nombre("GIGABYTE GS27FA 27\" FHD 180HZ").descripcion("Monitor GIGABYTE 27 pulgadas, Full HD, 180Hz").precio(new java.math.BigDecimal("329.00")).stock(8).imagen("/Assets/IMG/Produtos/Monitores/GiGABYTE 27.png").estadoProducto(true).build());
            }

            // Tarjetas de Video
            if (asus != null && tarjetas != null) {
                productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5080 16GB HATSUNE MIKU OC ROG ASTRAL").descripcion("Tarjeta de video ASUS RTX 5080 16GB edicion Hatsune Miku").precio(new java.math.BigDecimal("2490.00")).stock(3).imagen("/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS MIKU 5080.png").estadoProducto(true).build());
                productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5070 12GB WHITE PRIME OC").descripcion("Tarjeta de video ASUS RTX 5070 12GB color blanco").precio(new java.math.BigDecimal("879.00")).stock(12).imagen("/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS PRIME 5070.png").estadoProducto(true).build());
                productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5080 16GB ROG ASTRAL OC").descripcion("Tarjeta de video ASUS RTX 5080 16GB ROG ASTRAL").precio(new java.math.BigDecimal("2090.00")).stock(6).imagen("/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS 5080.png").estadoProducto(true).build());
            }
            if (msi != null && tarjetas != null) {
                productoRepository.save(Producto.builder().marca(msi).categoria(tarjetas).nombre("MSI RTX 5080 12GB TRIO").descripcion("Tarjeta de video MSI RTX 5080 12GB TRIO").precio(new java.math.BigDecimal("869.90")).stock(9).imagen("/Assets/IMG/Produtos/Tarjetas_de_Video/MSI TRIO 5070.png").estadoProducto(true).build());
            }
        }

        if (usuarioRepository.count() == 0) {
            Rol admin = rolRepository.findAll().stream().filter(r -> r.getNombre().equals("Administrador")).findFirst().orElse(null);
            Rol trabajador = rolRepository.findAll().stream().filter(r -> r.getNombre().equals("Trabajador")).findFirst().orElse(null);
            Rol cliente = rolRepository.findAll().stream().filter(r -> r.getNombre().equals("Cliente")).findFirst().orElse(null);

            if (admin != null) {
                usuarioRepository.save(Usuario.builder().rol(admin).nombres("Carlos").apellidos("Arroyo").correo("admin@techzone.com").password(passwordEncoder.encode("Admin123**")).telefono("967662103").estadoUsuario(true).build());
            }
            if (trabajador != null) {
                usuarioRepository.save(Usuario.builder().rol(trabajador).nombres("Gabriel").apellidos("Ipanaque").correo("trabajador@techzone.com").password(passwordEncoder.encode("Gabriel123**")).telefono("999111222").estadoUsuario(true).build());
            }
            if (cliente != null) {
                usuarioRepository.save(Usuario.builder().rol(cliente).nombres("Jhon").apellidos("Paz").correo("jhon@gmail.com").password(passwordEncoder.encode("Jhon123**")).telefono("999888777").estadoUsuario(true).build());
            }
        }
    }
}
