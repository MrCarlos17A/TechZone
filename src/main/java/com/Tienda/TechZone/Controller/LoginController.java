package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.RolRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import com.Tienda.TechZone.Repository.UbigeoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> LoginApi(@RequestParam String username,
            @RequestParam String password,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);
            securityContextRepository.saveContext(contexto, request, response);

            String rol = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .orElse("ROLE_Cliente");
            boolean esStaff = rol.equals("ROLE_Administrador") || rol.equals("ROLE_Trabajador");
            respuesta.put("ok", true);
            respuesta.put("esStaff", esStaff);
            respuesta.put("destino", esStaff ? "/admin" : "/");
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Datos invalidos");
        }
        return respuesta;
    }

    @GetMapping("/login")
    public String PaginaLogin() {
        return "redirect:/?abrirLogin=true";
    }

    @GetMapping("/registro")
    public String PaginaRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String Registrar(@RequestParam String nombres,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String password,
            @RequestParam String passwordConfirmar,
            RedirectAttributes flash) {

        if (correo == null || !correo.contains("@") || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            flash.addFlashAttribute("error", "Ingresa un correo valido que contenga @");
            return "redirect:/registro";
        }

        if (password == null || password.length() < 6) {
            flash.addFlashAttribute("error", "La contrasena debe tener al menos 6 caracteres");
            return "redirect:/registro";
        }

        if (!password.equals(passwordConfirmar)) {
            flash.addFlashAttribute("error", "Las contrasenas no coinciden");
            return "redirect:/registro";
        }

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            flash.addFlashAttribute("error", "El correo ya esta registrado");
            return "redirect:/registro";
        }

        Rol clienteRol = rolRepository.findByNombre("Cliente").orElse(null);
        if (clienteRol == null) {
            flash.addFlashAttribute("error", "Error del sistema: rol no encontrado");
            return "redirect:/registro";
        }

        Persona persona = personaRepository.save(Persona.builder()
                .nombres(nombres)
                .apellidoPaterno(apellidos)
                .telefono(telefono)
                .vigente(true)
                .build());

        Usuario usuario = Usuario.builder()
                .persona(persona)
                .rol(clienteRol)
                .correo(correo)
                .password(passwordEncoder.encode(password))
                .estado("activo")
                .build();

        usuarioRepository.save(usuario);
        clienteRepository.save(Cliente.builder().persona(persona).totalGastado(java.math.BigDecimal.ZERO).build());
        flash.addFlashAttribute("exito", "Cuenta creada correctamente. Ahora puedes iniciar sesion.");
        return "redirect:/?abrirLogin=true";
    }

    @GetMapping("/debug/usuarios")
    @ResponseBody
    public List<Usuario> DebugUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/debug/verificar")
    @ResponseBody
    public String DebugVerificar(@RequestParam String correo, @RequestParam String password) {
        var usuario = usuarioRepository.findByCorreo(correo);
        if (usuario.isEmpty()) {
            return "USUARIO NO ENCONTRADO: " + correo;
        }
        Usuario u = usuario.get();
        boolean matches = passwordEncoder.matches(password, u.getPassword());
        return "Usuario: " + u.getCorreo() + " | Rol: " + u.getRol().getNombre() + " | Estado: " + u.getEstado()
                + " | Password match: " + matches + " | Password hash: " + u.getPassword();
    }

    @GetMapping("/acceso-denegado")
    public String AccesoDenegado() {
        return "acceso-denegado";
    }

    @GetMapping("/mi-perfil")
    public String MiPerfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
        model.addAttribute("ubigeos", ubigeoRepository.findAllByVigenteTrue());
        return "perfil";
    }

    @PostMapping("/actualizar-perfil")
    public String ActualizarPerfil(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) String idTipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String ubigeo,
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String passwordNuevo,
            RedirectAttributes flash) {
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElseThrow();

        if (nombres != null && !nombres.isBlank() && usuario.getPersona() != null) {
            usuario.getPersona().setNombres(nombres.trim());
        }
        if (apellidoPaterno != null && usuario.getPersona() != null) {
            usuario.getPersona().setApellidoPaterno(apellidoPaterno.trim());
        }
        if (apellidoMaterno != null && usuario.getPersona() != null) {
            usuario.getPersona().setApellidoMaterno(apellidoMaterno.trim());
        }
        if (idTipoDocumento != null && !idTipoDocumento.isBlank() && usuario.getPersona() != null) {
            try {
                long idTd = Long.parseLong(idTipoDocumento.trim());
                tipoDocumentoRepository.findById(idTd)
                        .ifPresent(td -> usuario.getPersona().setTipoDocumento(td));
            } catch (NumberFormatException ignored) {
            }
        }
        if (numeroDocumento != null && usuario.getPersona() != null) {
            usuario.getPersona().setNumeroDocumento(numeroDocumento.trim());
        }
        if (telefono != null && usuario.getPersona() != null) {
            usuario.getPersona().setTelefono(telefono.trim());
        }
        if (fechaNacimiento != null && !fechaNacimiento.isBlank() && usuario.getPersona() != null) {
            usuario.getPersona().setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }
        if (direccion != null && usuario.getPersona() != null) {
            usuario.getPersona().setDireccion(direccion.trim());
        }
        if (referencia != null && usuario.getPersona() != null) {
            usuario.getPersona().setReferencia(referencia.trim());
        }
        if (ubigeo != null && !ubigeo.isBlank() && usuario.getPersona() != null) {
            try {
                long idUbi = Long.parseLong(ubigeo.trim());
                ubigeoRepository.findById(idUbi).ifPresent(u -> usuario.getPersona().setUbigeo(u));
            } catch (NumberFormatException ignored) {
            }
        }

        if (passwordNuevo != null && !passwordNuevo.isEmpty()) {
            if (passwordNuevo.length() < 6) {
                flash.addFlashAttribute("error", "La nueva contrasena debe tener al menos 6 caracteres");
                return "redirect:/mi-perfil";
            }
            if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                flash.addFlashAttribute("error", "La contrasena actual es incorrecta");
                return "redirect:/mi-perfil";
            }
            usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        }

        usuarioRepository.save(usuario);

        flash.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/mi-perfil";
    }
}
