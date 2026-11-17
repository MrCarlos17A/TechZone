package com.tienda.backend_techzone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class BackendTechzoneApplication {

	public static void main(String[] args) {
		cargarDotEnvSiExiste();
		SpringApplication.run(BackendTechzoneApplication.class, args);
	}

	@Bean
	CommandLineRunner sincronizarBaseDatos(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE IF EXISTS medio_pago ADD COLUMN IF NOT EXISTS instrucciones VARCHAR(300)");
				jdbcTemplate.execute("UPDATE medio_pago SET instrucciones = 'Escanea el código QR oficial desde tu app bancaria o billetera móvil (Yape / Plin) e ingresa el número de operación.' WHERE id_medio_pago = 1 AND (instrucciones IS NULL OR instrucciones = '')");
				jdbcTemplate.execute("UPDATE medio_pago SET instrucciones = 'Procesamiento en línea 100% seguro con tecnología 3DS Secure (Visa, Mastercard, American Express).' WHERE id_medio_pago = 2 AND (instrucciones IS NULL OR instrucciones = '')");
				jdbcTemplate.execute("UPDATE medio_pago SET instrucciones = 'Aceptamos tarjetas de débito nacionales con compras por internet habilitadas.' WHERE id_medio_pago = 3 AND (instrucciones IS NULL OR instrucciones = '')");
				jdbcTemplate.execute("UPDATE medio_pago SET instrucciones = 'Transfiere al número de cuenta o CCI institucional y adjunta el número de operación para verificación inmediata.' WHERE id_medio_pago = 4 AND (instrucciones IS NULL OR instrucciones = '')");
				jdbcTemplate.execute("UPDATE medio_pago SET instrucciones = 'Genera tu código CIP y realiza el pago en agentes bancarios autorizados o banca por internet.' WHERE id_medio_pago = 5 AND (instrucciones IS NULL OR instrucciones = '')");
				jdbcTemplate.execute("UPDATE pago SET estado_pago = 'CONFIRMADO', codigo_operacion = 'OP-YAPE-000002' WHERE codigo_operacion = '123456'");
				jdbcTemplate.execute("UPDATE venta SET direccion_envio = 'EL VIVERO S/N, Tumán, Chiclayo - Lambayeque' WHERE id_venta = 2 AND (direccion_envio = 'EL VIVERO S/N' OR direccion_envio IS NULL)");
				jdbcTemplate.execute("ALTER TABLE IF EXISTS configuracion_sitio ADD COLUMN IF NOT EXISTS ruc VARCHAR(11) DEFAULT '20608945123'");
				jdbcTemplate.execute("ALTER TABLE IF EXISTS configuracion_sitio ADD COLUMN IF NOT EXISTS razon_social VARCHAR(150) DEFAULT 'TECHZONE GAMING STORE S.A.C.'");
				jdbcTemplate.execute("ALTER TABLE IF EXISTS configuracion_sitio ADD COLUMN IF NOT EXISTS direccion_fiscal VARCHAR(200) DEFAULT 'Av. Javier Prado Este 4200, Santiago de Surco - Lima'");
				jdbcTemplate.execute("ALTER TABLE IF EXISTS configuracion_sitio ADD COLUMN IF NOT EXISTS web VARCHAR(100) DEFAULT 'www.techzone.com'");
				jdbcTemplate.execute("ALTER TABLE IF EXISTS configuracion_sitio ADD COLUMN IF NOT EXISTS leyenda_fiscal VARCHAR(255) DEFAULT 'Autorizado mediante resolución SUNAT N° 034-005'");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET ruc = '20608945123' WHERE ruc IS NULL OR ruc = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET razon_social = 'TECHZONE GAMING STORE S.A.C.' WHERE razon_social IS NULL OR razon_social = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET direccion_fiscal = 'Av. Javier Prado Este 4200, Santiago de Surco - Lima' WHERE direccion_fiscal IS NULL OR direccion_fiscal = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET web = 'www.techzone.com' WHERE web IS NULL OR web = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET leyenda_fiscal = 'Autorizado mediante resolución SUNAT N° 034-005' WHERE leyenda_fiscal IS NULL OR leyenda_fiscal = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET nombre_sitio = 'TechZone Gaming Store' WHERE nombre_sitio IS NULL OR nombre_sitio = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET telefono = '+51 967 662 103' WHERE telefono IS NULL OR telefono = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET email = 'ventas@techzone.com' WHERE email IS NULL OR email = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET tagline = 'Componentes Gamer y Tecnología de Última Generación' WHERE tagline IS NULL OR tagline = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET horario = 'Lun - Dom: 09:00 a.m. - 21:00 p.m.' WHERE horario IS NULL OR horario = ''");
				jdbcTemplate.execute("UPDATE configuracion_sitio SET copyright = '© 2026 TechZone Gaming Store S.A.C. Todos los derechos reservados.' WHERE copyright IS NULL OR copyright = ''");
			} catch (Exception ignored) {
			}
		};
	}

	private static void cargarDotEnvSiExiste() {
		Path[] rutas = new Path[]{
				Paths.get(".env"),
				Paths.get("backend-techzone", ".env"),
				Paths.get("..", "backend-techzone", ".env")
		};
		for (Path ruta : rutas) {
			if (Files.exists(ruta) && Files.isRegularFile(ruta)) {
				try {
					List<String> lineas = Files.readAllLines(ruta);
					for (String linea : lineas) {
						String l = linea.trim();
						if (l.isEmpty() || l.startsWith("#")) continue;
						int idx = l.indexOf('=');
						if (idx > 0) {
							String key = l.substring(0, idx).trim();
							String val = l.substring(idx + 1).trim();
							if (val.length() >= 2 && ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'")))) {
								val = val.substring(1, val.length() - 2);
							}
							if (System.getProperty(key) == null && System.getenv(key) == null) {
								System.setProperty(key, val);
							}
						}
					}
					break;
				} catch (Exception ignored) {
				}
			}
		}
	}
}
