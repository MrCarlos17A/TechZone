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
