package com.Tienda.TechZone;

import com.Tienda.TechZone.Model.*;
import com.Tienda.TechZone.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final ProductoImagenRepository productoImagenRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PagoRepository pagoRepository;
    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // ══════════════════════════════════════════════
        // 1. ELIMINAR DATOS INNECESARIOS (solo admin)
        // ══════════════════════════════════════════════
        limpiarDatosInnecesarios();

        // ══════════════════════════════════════════════
        // 2. ROLES
        // ══════════════════════════════════════════════
        if (rolRepository.count() == 0) {
            rolRepository.save(Rol.builder().idRol(1).nombre("Administrador").vigente(true).build());
            rolRepository.save(Rol.builder().idRol(2).nombre("Trabajador").vigente(true).build());
            rolRepository.save(Rol.builder().idRol(3).nombre("Cliente").vigente(true).build());
        }

        // ══════════════════════════════════════════════
        // 3. TIPOS DE DOCUMENTO (solo DNI, RUC, Pasaporte)
        // ══════════════════════════════════════════════
        if (tipoDocumentoRepository.count() == 0) {
            tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(1).nombre("DNI").vigente(true).build());
            tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(3).nombre("RUC").vigente(true).build());
            tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(4).nombre("Pasaporte").vigente(true).build());
        } else {
            // Eliminar tipos que no sean DNI, RUC ni Pasaporte
            for (TipoDocumento td : tipoDocumentoRepository.findAll()) {
                String n = td.getNombre() == null ? "" : td.getNombre().trim();
                if (!n.equals("DNI") && !n.equals("RUC") && !n.equals("Pasaporte")) {
                    tipoDocumentoRepository.delete(td);
                }
            }
            if (!tipoDocumentoRepository.existsById(1)) {
                tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(1).nombre("DNI").vigente(true).build());
            }
            if (!tipoDocumentoRepository.existsById(3)) {
                tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(3).nombre("RUC").vigente(true).build());
            }
            if (!tipoDocumentoRepository.existsById(4)) {
                tipoDocumentoRepository.save(TipoDocumento.builder().idTipoDocumento(4).nombre("Pasaporte").vigente(true).build());
            }
        }

        // ══════════════════════════════════════════════
        // 4. MEDIOS DE PAGO
        // ══════════════════════════════════════════════
        if (medioPagoRepository.count() == 0) {
            medioPagoRepository.save(MedioPago.builder().idMedioPago(1).nombre("Efectivo").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(2).nombre("Yape").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(3).nombre("Plin").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(4).nombre("Tarjeta de Credito").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(5).nombre("Tarjeta de Debito").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(6).nombre("Transferencia").vigente(true).build());
            medioPagoRepository.save(MedioPago.builder().idMedioPago(7).nombre("PagoEfectivo").vigente(true).build());
        }

        // ══════════════════════════════════════════════
        // 5. UBIGEOS - Lima, Callao, Lambayeque completo
        // ══════════════════════════════════════════════
        if (ubigeoRepository.count() == 0) {
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150101).departamento("Lima").provincia("Lima").distrito("Lima").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150103).departamento("Lima").provincia("Lima").distrito("Ate").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150107).departamento("Lima").provincia("Lima").distrito("Breña").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150114).departamento("Lima").provincia("Lima").distrito("Comas").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150116).departamento("Lima").provincia("Lima").distrito("El Agustino").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150128).departamento("Lima").provincia("Lima").distrito("La Molina").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150130).departamento("Lima").provincia("Lima").distrito("La Victoria").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150141).departamento("Lima").provincia("Lima").distrito("Lince").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150143).departamento("Lima").provincia("Lima").distrito("Los Olivos").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150147).departamento("Lima").provincia("Lima").distrito("Miraflores").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150150).departamento("Lima").provincia("Lima").distrito("Pueblo Libre").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150154).departamento("Lima").provincia("Lima").distrito("Rimac").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150159).departamento("Lima").provincia("Lima").distrito("San Borja").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150161).departamento("Lima").provincia("Lima").distrito("San Isidro").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150163).departamento("Lima").provincia("Lima").distrito("San Juan de Lurigancho").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150167).departamento("Lima").provincia("Lima").distrito("San Martin de Porres").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150169).departamento("Lima").provincia("Lima").distrito("San Miguel").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150173).departamento("Lima").provincia("Lima").distrito("Santiago de Surco").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150177).departamento("Lima").provincia("Lima").distrito("Surquillo").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150178).departamento("Lima").provincia("Lima").distrito("Villa El Salvador").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(150179).departamento("Lima").provincia("Lima").distrito("Villa Maria del Triunfo").vigente(true).build());
            ubigeoRepository.save(Ubigeo.builder().idUbigeo(200101).departamento("Callao").provincia("Callao").distrito("Callao").vigente(true).build());
        }

        Map<Integer, Ubigeo> ubigeosLambayeque = Map.ofEntries(
                Map.entry(140101, Ubigeo.builder().idUbigeo(140101).departamento("Lambayeque").provincia("Chiclayo").distrito("Chiclayo").vigente(true).build()),
                Map.entry(140102, Ubigeo.builder().idUbigeo(140102).departamento("Lambayeque").provincia("Chiclayo").distrito("Cayalti").vigente(true).build()),
                Map.entry(140103, Ubigeo.builder().idUbigeo(140103).departamento("Lambayeque").provincia("Chiclayo").distrito("Chongoyape").vigente(true).build()),
                Map.entry(140104, Ubigeo.builder().idUbigeo(140104).departamento("Lambayeque").provincia("Chiclayo").distrito("Eten").vigente(true).build()),
                Map.entry(140105, Ubigeo.builder().idUbigeo(140105).departamento("Lambayeque").provincia("Chiclayo").distrito("Eten Puerto").vigente(true).build()),
                Map.entry(140106, Ubigeo.builder().idUbigeo(140106).departamento("Lambayeque").provincia("Chiclayo").distrito("Jose Leonardo Ortiz").vigente(true).build()),
                Map.entry(140107, Ubigeo.builder().idUbigeo(140107).departamento("Lambayeque").provincia("Chiclayo").distrito("La Victoria").vigente(true).build()),
                Map.entry(140108, Ubigeo.builder().idUbigeo(140108).departamento("Lambayeque").provincia("Chiclayo").distrito("Lagunas").vigente(true).build()),
                Map.entry(140109, Ubigeo.builder().idUbigeo(140109).departamento("Lambayeque").provincia("Chiclayo").distrito("Monsefu").vigente(true).build()),
                Map.entry(140110, Ubigeo.builder().idUbigeo(140110).departamento("Lambayeque").provincia("Chiclayo").distrito("Nueva Arica").vigente(true).build()),
                Map.entry(140111, Ubigeo.builder().idUbigeo(140111).departamento("Lambayeque").provincia("Chiclayo").distrito("Oyotun").vigente(true).build()),
                Map.entry(140112, Ubigeo.builder().idUbigeo(140112).departamento("Lambayeque").provincia("Chiclayo").distrito("Picsi").vigente(true).build()),
                Map.entry(140113, Ubigeo.builder().idUbigeo(140113).departamento("Lambayeque").provincia("Chiclayo").distrito("Pimentel").vigente(true).build()),
                Map.entry(140114, Ubigeo.builder().idUbigeo(140114).departamento("Lambayeque").provincia("Chiclayo").distrito("Reque").vigente(true).build()),
                Map.entry(140115, Ubigeo.builder().idUbigeo(140115).departamento("Lambayeque").provincia("Chiclayo").distrito("Santa Rosa").vigente(true).build()),
                Map.entry(140116, Ubigeo.builder().idUbigeo(140116).departamento("Lambayeque").provincia("Chiclayo").distrito("Saña").vigente(true).build()),
                Map.entry(140117, Ubigeo.builder().idUbigeo(140117).departamento("Lambayeque").provincia("Chiclayo").distrito("Catache").vigente(true).build()),
                Map.entry(140118, Ubigeo.builder().idUbigeo(140118).departamento("Lambayeque").provincia("Chiclayo").distrito("Chucay").vigente(true).build()),
                Map.entry(140119, Ubigeo.builder().idUbigeo(140119).departamento("Lambayeque").provincia("Chiclayo").distrito("Salas").vigente(true).build()),
                Map.entry(140120, Ubigeo.builder().idUbigeo(140120).departamento("Lambayeque").provincia("Chiclayo").distrito("San Jose").vigente(true).build()),
                Map.entry(140121, Ubigeo.builder().idUbigeo(140121).departamento("Lambayeque").provincia("Chiclayo").distrito("Tucume").vigente(true).build()),
                Map.entry(140122, Ubigeo.builder().idUbigeo(140122).departamento("Lambayeque").provincia("Chiclayo").distrito("Tuman").vigente(true).build()),
                Map.entry(140201, Ubigeo.builder().idUbigeo(140201).departamento("Lambayeque").provincia("Ferreñafe").distrito("Ferreñafe").vigente(true).build()),
                Map.entry(140202, Ubigeo.builder().idUbigeo(140202).departamento("Lambayeque").provincia("Ferreñafe").distrito("Cañaris").vigente(true).build()),
                Map.entry(140203, Ubigeo.builder().idUbigeo(140203).departamento("Lambayeque").provincia("Ferreñafe").distrito("Incahuasi").vigente(true).build()),
                Map.entry(140204, Ubigeo.builder().idUbigeo(140204).departamento("Lambayeque").provincia("Ferreñafe").distrito("Manuel Antonio Mesones Muro").vigente(true).build()),
                Map.entry(140205, Ubigeo.builder().idUbigeo(140205).departamento("Lambayeque").provincia("Ferreñafe").distrito("Pitipo").vigente(true).build()),
                Map.entry(140206, Ubigeo.builder().idUbigeo(140206).departamento("Lambayeque").provincia("Ferreñafe").distrito("Pueblo Nuevo").vigente(true).build()),
                Map.entry(140301, Ubigeo.builder().idUbigeo(140301).departamento("Lambayeque").provincia("Lambayeque").distrito("Lambayeque").vigente(true).build()),
                Map.entry(140302, Ubigeo.builder().idUbigeo(140302).departamento("Lambayeque").provincia("Lambayeque").distrito("Chochope").vigente(true).build()),
                Map.entry(140303, Ubigeo.builder().idUbigeo(140303).departamento("Lambayeque").provincia("Lambayeque").distrito("Illimo").vigente(true).build()),
                Map.entry(140304, Ubigeo.builder().idUbigeo(140304).departamento("Lambayeque").provincia("Lambayeque").distrito("Jayanca").vigente(true).build()),
                Map.entry(140305, Ubigeo.builder().idUbigeo(140305).departamento("Lambayeque").provincia("Lambayeque").distrito("Mochumi").vigente(true).build()),
                Map.entry(140306, Ubigeo.builder().idUbigeo(140306).departamento("Lambayeque").provincia("Lambayeque").distrito("Morrope").vigente(true).build()),
                Map.entry(140307, Ubigeo.builder().idUbigeo(140307).departamento("Lambayeque").provincia("Lambayeque").distrito("Motupe").vigente(true).build()),
                Map.entry(140308, Ubigeo.builder().idUbigeo(140308).departamento("Lambayeque").provincia("Lambayeque").distrito("Olmos").vigente(true).build()),
                Map.entry(140309, Ubigeo.builder().idUbigeo(140309).departamento("Lambayeque").provincia("Lambayeque").distrito("Pacora").vigente(true).build()),
                Map.entry(140310, Ubigeo.builder().idUbigeo(140310).departamento("Lambayeque").provincia("Lambayeque").distrito("Salas").vigente(true).build()),
                Map.entry(140311, Ubigeo.builder().idUbigeo(140311).departamento("Lambayeque").provincia("Lambayeque").distrito("San Jose").vigente(true).build()),
                Map.entry(140312, Ubigeo.builder().idUbigeo(140312).departamento("Lambayeque").provincia("Lambayeque").distrito("Tucume").vigente(true).build()));
        ubigeosLambayeque.forEach((id, u) -> {
            if (!ubigeoRepository.existsById(id)) {
                ubigeoRepository.save(u);
            }
        });

        // ══════════════════════════════════════════════
        // 6. MARCAS
        // ══════════════════════════════════════════════
        if (marcaRepository.count() == 0) {
            marcaRepository.save(Marca.builder().idMarca(1).nombre("ASUS").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(2).nombre("MSI").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(3).nombre("GIGABYTE").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(4).nombre("CORSAIR").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(5).nombre("REDRAGON").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(6).nombre("Logitech").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(7).nombre("Kingston").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(8).nombre("ALIENWARE").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(9).nombre("TOSHIBA").vigente(true).build());
            marcaRepository.save(Marca.builder().idMarca(10).nombre("MICRONICS").vigente(true).build());
        }

        // ══════════════════════════════════════════════
        // 7. CATEGORIAS
        // ══════════════════════════════════════════════
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(Categoria.builder().idCategoria(1).nombre("Monitores").vigente(true).build());
            categoriaRepository.save(Categoria.builder().idCategoria(2).nombre("Tarjetas de Video").vigente(true).build());
        }

        // ══════════════════════════════════════════════
        // 8. PRODUCTOS
        // ══════════════════════════════════════════════
        if (productoRepository.count() == 0) {
            Marca asus = marcaRepository.findById(1).orElse(null);
            Marca msi = marcaRepository.findById(2).orElse(null);
            Marca gigabyte = marcaRepository.findById(3).orElse(null);
            Categoria monitores = categoriaRepository.findById(1).orElse(null);
            Categoria tarjetas = categoriaRepository.findById(2).orElse(null);
            Map<String, String> mapaImagenes = new java.util.LinkedHashMap<>();

            if (asus != null && monitores != null) {
                Producto p1 = productoRepository.save(Producto.builder().marca(asus).categoria(monitores).nombre("ASUS ROG STRIX XG27ACMES 27\" QHD 120HZ").descripcion("Monitor gaming ASUS ROG STRIX 27 pulgadas, resolucion QHD, 120Hz").precioVenta(new BigDecimal("273.73")).stock(15).vigente(true).build());
                mapaImagenes.put(String.valueOf(p1.getIdProducto()), "/Assets/IMG/Produtos/Monitores/ASUS 27.png");
                Producto p2 = productoRepository.save(Producto.builder().marca(asus).categoria(monitores).nombre("ASUS TUF GAMING VG34VQL3A 34\" CURVO WQHD 180HZ").descripcion("Monitor curvo ASUS TUF 34 pulgadas, WQHD, 180Hz").precioVenta(new BigDecimal("359.00")).stock(5).vigente(true).build());
                mapaImagenes.put(String.valueOf(p2.getIdProducto()), "/Assets/IMG/Produtos/Monitores/ASUS 34.png");
            }
            if (msi != null && monitores != null) {
                Producto p3 = productoRepository.save(Producto.builder().marca(msi).categoria(monitores).nombre("MSI MAG 271QPX QD-OLED 27\" WQHD 250HZ").descripcion("Monitor MSI con panel QD-OLED, 27 pulgadas, WQHD, 250Hz").precioVenta(new BigDecimal("258.26")).stock(10).vigente(true).build());
                mapaImagenes.put(String.valueOf(p3.getIdProducto()), "/Assets/IMG/Produtos/Monitores/MSI 27.png");
            }
            if (gigabyte != null && monitores != null) {
                Producto p4 = productoRepository.save(Producto.builder().marca(gigabyte).categoria(monitores).nombre("GIGABYTE GS27FA 27\" FHD 180HZ").descripcion("Monitor GIGABYTE 27 pulgadas, Full HD, 180Hz").precioVenta(new BigDecimal("329.00")).stock(8).vigente(true).build());
                mapaImagenes.put(String.valueOf(p4.getIdProducto()), "/Assets/IMG/Produtos/Monitores/GiGABYTE 27.png");
            }
            if (asus != null && tarjetas != null) {
                Producto p5 = productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5080 16GB HATSUNE MIKU OC ROG ASTRAL").descripcion("Tarjeta de video ASUS RTX 5080 16GB edicion Hatsune Miku").precioVenta(new BigDecimal("2490.00")).stock(3).vigente(true).build());
                mapaImagenes.put(String.valueOf(p5.getIdProducto()), "/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS MIKU 5080.png");
                Producto p6 = productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5070 12GB WHITE PRIME OC").descripcion("Tarjeta de video ASUS RTX 5070 12GB color blanco").precioVenta(new BigDecimal("879.00")).stock(12).vigente(true).build());
                mapaImagenes.put(String.valueOf(p6.getIdProducto()), "/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS PRIME 5070.png");
                Producto p7 = productoRepository.save(Producto.builder().marca(asus).categoria(tarjetas).nombre("ASUS RTX 5080 16GB ROG ASTRAL OC").descripcion("Tarjeta de video ASUS RTX 5080 16GB ROG ASTRAL").precioVenta(new BigDecimal("2090.00")).stock(6).vigente(true).build());
                mapaImagenes.put(String.valueOf(p7.getIdProducto()), "/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS 5080.png");
            }
            if (msi != null && tarjetas != null) {
                Producto p8 = productoRepository.save(Producto.builder().marca(msi).categoria(tarjetas).nombre("MSI RTX 5080 12GB TRIO").descripcion("Tarjeta de video MSI RTX 5080 12GB TRIO").precioVenta(new BigDecimal("869.90")).stock(9).vigente(true).build());
                mapaImagenes.put(String.valueOf(p8.getIdProducto()), "/Assets/IMG/Produtos/Tarjetas_de_Video/MSI TRIO 5070.png");
            }

            for (Map.Entry<String, String> entry : mapaImagenes.entrySet()) {
                Long idProd = Long.valueOf(entry.getKey());
                Producto prod = productoRepository.findById(idProd).orElse(null);
                if (prod != null) {
                    productoImagenRepository.save(ProductoImagen.builder()
                            .producto(prod)
                            .urlImagen(entry.getValue())
                            .esPrincipal(true)
                            .build());
                }
            }
        }

        // Backfill SKU para productos existentes sin codigo
        List<Producto> sinSku = productoRepository.findAll().stream()
                .filter(p -> p.getCodigoSku() == null || p.getCodigoSku().isBlank())
                .toList();
        Map<String, Integer> correlativos = new java.util.HashMap<>();
        for (Producto p : sinSku) {
            String preM = prefijoSku(p.getMarca() != null ? p.getMarca().getNombre() : "");
            String preC = prefijoSku(p.getCategoria() != null ? p.getCategoria().getNombre() : "");
            int idM = p.getMarca() != null ? p.getMarca().getIdMarca() : 0;
            int idC = p.getCategoria() != null ? p.getCategoria().getIdCategoria() : 0;
            String clave = idM + "-" + idC;
            int n = correlativos.getOrDefault(clave, 0);
            String sku;
            do {
                n++;
                sku = preM + preC + String.format("%02d", n);
            } while (productoRepository.existsByCodigoSku(sku));
            correlativos.put(clave, n);
            p.setCodigoSku(sku);
            productoRepository.save(p);
        }

        // ══════════════════════════════════════════════
        // 9. PROVEEDORES (20 distribuidores reales)
        // ══════════════════════════════════════════════
        if (proveedorRepository.count() < 20) {
            TipoDocumento ruc = tipoDocumentoRepository.findById(3).orElse(null);
            if (ruc != null) {
                String[][] proveedores = {
                    {"ASUSTeK Computer Inc.", "20558662471", "987654321", "contacto@asus.com.pe", "Roberto Salazar"},
                    {"Micro-Star INTL Co., Ltd.", "20512345678", "912345678", "ventas@msi.com", "Maria Torres"},
                    {"GIGABYTE Technology Co., Ltd.", "20698765432", "923456789", "ventas@gigabyte.com", "Juan Perez"},
                    {"NVIDIA Corporation", "20456789123", "934567890", "ventas@nvidia.com", "Carlos Mendez"},
                    {"CORSAIR Gaming, Inc.", "20534567891", "945678901", "ventas@corsair.com", "Ana Lopez"},
                    {"Logitech International S.A.", "20645678912", "956789012", "ventas@logitech.com", "Pedro Ruiz"},
                    {"Kingston Technology Company, Inc.", "20467891234", "967890123", "ventas@kingston.com", "Luis Garcia"},
                    {"Dell Technologies Inc.", "20578912345", "978901234", "ventas@dell.com", "Sofia Martinez"},
                    {"HP Inc.", "20689123456", "989012345", "ventas@hp.com", "Diego Rodriguez"},
                    {"Lenovo Group Limited", "20491234567", "990123456", "ventas@lenovo.com", "Carmen Vargas"},
                    {"Synexo Corporation", "20601234567", "901234567", "ventas@synexo.com", "Miguel Sanchez"},
                    {"Ingram Micro Inc.", "20512345679", "912345670", "ventas@ingrammicro.com", "Rosa Chen"},
                    {"Arrow Electronics, Inc.", "20623456789", "923456780", "ventas@arrow.com", "Fernando Torres"},
                    {"Avnet, Inc.", "20434567890", "934567891", "ventas@avnet.com", "Patricia Gomez"},
                    {"Tech Data Corporation", "20545678901", "945678902", "ventas@techdata.com", "Andres Lopez"},
                    {"Wortmann AG (Terra Computer)", "20656789012", "956789013", "ventas@wortmann.com", "Elena Rodriguez"},
                    {"Acer Inc.", "20467890123", "967890124", "ventas@acer.com", "Roberto Diaz"},
                    {"Seagate Technology LLC", "20578901234", "978901235", "ventas@seagate.com", "Monica Herrera"},
                    {"Western Digital Corporation", "20689012345", "989012346", "ventas@westerndigital.com", "Jorge Ramos"},
                    {"Samsung Electronics Co., Ltd.", "20490123456", "990123457", "ventas@samsung.com", "Lucia Fernandez"}
                };
                for (String[] p : proveedores) {
                    boolean existe = proveedorRepository.findAll().stream()
                            .anyMatch(pr -> p[1].equals(pr.getNumeroDocumento()));
                    if (!existe) {
                        proveedorRepository.save(Proveedor.builder()
                                .razonSocial(p[0]).tipoDocumento(ruc).numeroDocumento(p[1])
                                .telefonoContacto(p[2]).correo(p[3]).nombreEncargado(p[4]).vigente(true).build());
                    }
                }
            }
        }

        // ══════════════════════════════════════════════
        // 10. UNICO USUARIO: ADMIN CARLOS FELIPE ARROYO VEGA
        // ══════════════════════════════════════════════
        Rol admin = rolRepository.findById(1).orElse(null);

        // Si no existe el admin, crearlo
        if (usuarioRepository.findByCorreo("admin@techzone.com").isEmpty()) {
            Ubigeo ubigeoTuman = ubigeoRepository.findById(140122).orElse(null);
            TipoDocumento tipoDni = tipoDocumentoRepository.findById(1).orElse(null);
            Persona p1 = personaRepository.save(Persona.builder()
                    .nombres("Carlos Felipe")
                    .apellidoPaterno("Arroyo")
                    .apellidoMaterno("Vega")
                    .tipoDocumento(tipoDni)
                    .numeroDocumento("73234768")
                    .telefono("967662103")
                    .direccion("Av. Santa Ana")
                    .referencia("Plaza")
                    .ubigeo(ubigeoTuman)
                    .fechaNacimiento(LocalDate.of(2005, 4, 13))
                    .vigente(true)
                    .build());
            usuarioRepository.save(Usuario.builder()
                    .persona(p1)
                    .rol(admin)
                    .correo("admin@techzone.com")
                    .password(passwordEncoder.encode("Admin123**"))
                    .estado("activo")
                    .build());
        } else {
            // Actualizar datos del admin existente
            usuarioRepository.findByCorreo("admin@techzone.com").ifPresent(adminUser -> {
                Persona pa = adminUser.getPersona();
                if (pa != null) {
                    Ubigeo ubigeoTuman = ubigeoRepository.findById(140122).orElse(null);
                    TipoDocumento tipoDni = tipoDocumentoRepository.findById(1).orElse(null);
                    pa.setNombres("Carlos Felipe");
                    pa.setApellidoPaterno("Arroyo");
                    pa.setApellidoMaterno("Vega");
                    pa.setTipoDocumento(tipoDni);
                    pa.setNumeroDocumento("73234768");
                    pa.setTelefono("967662103");
                    pa.setDireccion("Av. Santa Ana");
                    pa.setReferencia("Plaza");
                    pa.setUbigeo(ubigeoTuman);
                    pa.setFechaNacimiento(LocalDate.of(2005, 4, 13));
                    personaRepository.save(pa);
                }
                adminUser.setEstado("activo");
                if (admin != null) {
                    adminUser.setRol(admin);
                }
                usuarioRepository.save(adminUser);
            });
        }
    }

    /**
     * Elimina todos los datos innecesarios:
     * - Usuarios que no sean admin@techzone.com
     * - Ventas, detalles de venta, pagos
     * - Carritos y detalles de carrito
     * - Clientes y empleados no admin
     */
    private void limpiarDatosInnecesarios() {
        // Eliminar ventas y dependencias
        List<Venta> ventas = ventaRepository.findAll();
        if (!ventas.isEmpty()) {
            detalleVentaRepository.deleteAll();
            pagoRepository.deleteAll();
            ventaRepository.deleteAll();
        }

        // Eliminar carritos y dependencias
        List<Carrito> carritos = carritoRepository.findAll();
        if (!carritos.isEmpty()) {
            detalleCarritoRepository.deleteAll();
            carritoRepository.deleteAll();
        }

        // Eliminar clientes y empleados
        clienteRepository.deleteAll();
        empleadoRepository.deleteAll();

        // Eliminar usuarios que no sean admin
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            if (!"admin@techzone.com".equals(u.getCorreo())) {
                // Eliminar persona asociada si existe
                if (u.getPersona() != null) {
                    personaRepository.delete(u.getPersona());
                }
                usuarioRepository.delete(u);
            }
        }
    }

    private String prefijoSku(String texto) {
        if (texto == null || texto.isBlank()) {
            return "XX";
        }
        String limpio = texto.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (limpio.length() >= 2) {
            return limpio.substring(0, 2);
        }
        return String.format("%-2s", limpio).replace(' ', 'X');
    }
}
