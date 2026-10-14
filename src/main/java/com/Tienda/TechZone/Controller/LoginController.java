package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Model.Ubigeo;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.RolRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
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
import org.springframework.transaction.annotation.Transactional;
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
                    .orElse("ROLE_Usuario");
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
    public String PaginaRegistro(Model model) {
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAllByVigenteTrue());
        return "registro";
    }

    @PostMapping("/registro")
    @Transactional
    public String Registrar(@RequestParam String nombres,
            @RequestParam String apellidos,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String password,
            @RequestParam String passwordConfirmar,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) String idTipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String idPersona,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String ubigeo,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            RedirectAttributes flash) {

        if (nombres == null || nombres.isBlank()) {
            flash.addFlashAttribute("error", "Los nombres son obligatorios");
            return "redirect:/registro";
        }
        if (nombres.trim().length() > 100) {
            flash.addFlashAttribute("error", "Los nombres no pueden superar 100 caracteres");
            return "redirect:/registro";
        }

        if (apellidos == null || apellidos.isBlank()) {
            flash.addFlashAttribute("error", "El apellido paterno es obligatorio");
            return "redirect:/registro";
        }
        if (apellidos.trim().length() > 50) {
            flash.addFlashAttribute("error", "El apellido paterno no puede superar 50 caracteres");
            return "redirect:/registro";
        }

        if (correo == null || !correo.contains("@") || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            flash.addFlashAttribute("error", "Ingresa un correo valido que contenga @");
            return "redirect:/registro";
        }
        if (correo.trim().length() > 100) {
            flash.addFlashAttribute("error", "El correo no puede superar 100 caracteres");
            return "redirect:/registro";
        }
        correo = correo.trim();

        if (telefono == null || !telefono.matches("\\d{9}")) {
            flash.addFlashAttribute("error", "El telefono debe tener 9 digitos");
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

        if (numeroDocumento != null && !numeroDocumento.isBlank()
                && !numeroDocumento.matches("[0-9A-Za-z]{6,20}")) {
            flash.addFlashAttribute("error", "El numero de documento debe tener entre 6 y 20 caracteres");
            return "redirect:/registro";
        }

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            flash.addFlashAttribute("error", "El correo ya esta registrado");
            return "redirect:/registro";
        }

        Rol usuarioRol = rolRepository.findByNombre("Usuario").orElse(null);
        if (usuarioRol == null) {
            flash.addFlashAttribute("error", "Error del sistema: rol no encontrado");
            return "redirect:/registro";
        }

        TipoDocumento tipoDoc = null;
        if (idTipoDocumento != null && !idTipoDocumento.isBlank()) {
            try {
                tipoDoc = tipoDocumentoRepository.findById(Long.parseLong(idTipoDocumento.trim())).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }

        Ubigeo ubigeoSel = null;
        if (ubigeo != null && !ubigeo.isBlank()) {
            try {
                ubigeoSel = ubigeoRepository.findById(Long.parseLong(ubigeo.trim())).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }

        java.time.LocalDate fechaNac = null;
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            try {
                fechaNac = java.time.LocalDate.parse(fechaNacimiento);
            } catch (RuntimeException ignored) {
                flash.addFlashAttribute("error", "La fecha de nacimiento no es valida");
                return "redirect:/registro";
            }
        }

        Persona persona = null;
        if (idPersona != null && !idPersona.isBlank()) {
            try {
                Persona existente = personaRepository.findById(Long.parseLong(idPersona.trim())).orElse(null);
                // Solo se reusa si coincide el numero de documento (evita apuntar a otra
                // persona)
                if (existente != null && Boolean.TRUE.equals(existente.getVigente())
                        && existente.getNumeroDocumento() != null && numeroDocumento != null
                        && !numeroDocumento.isBlank()
                        && existente.getNumeroDocumento().trim().equalsIgnoreCase(numeroDocumento.trim())) {
                    persona = existente;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (persona != null) {
            if (!nombres.isBlank()) {
                persona.setNombres(nombres.trim());
            }
            if (!apellidos.isBlank()) {
                persona.setApellidoPaterno(apellidos.trim());
            }
            if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
                persona.setApellidoMaterno(apellidoMaterno.trim());
            }
            if (tipoDoc != null) {
                persona.setTipoDocumento(tipoDoc);
            }
            if (telefono != null && !telefono.isBlank()) {
                persona.setTelefono(telefono.trim());
            }
            if (direccion != null && !direccion.isBlank()) {
                persona.setDireccion(direccion.trim());
            }
            if (referencia != null && !referencia.isBlank()) {
                persona.setReferencia(referencia.trim());
            }
            if (ubigeoSel != null) {
                persona.setUbigeo(ubigeoSel);
            }
            if (fechaNac != null) {
                persona.setFechaNacimiento(fechaNac);
            }
            persona = personaRepository.save(persona);
        } else {
            persona = personaRepository.save(Persona.builder()
                    .nombres(nombres.trim())
                    .apellidoPaterno(apellidos.trim())
                    .apellidoMaterno(
                            apellidoMaterno != null && !apellidoMaterno.isBlank() ? apellidoMaterno.trim() : null)
                    .tipoDocumento(tipoDoc)
                    .numeroDocumento(
                            numeroDocumento != null && !numeroDocumento.isBlank() ? numeroDocumento.trim() : null)
                    .telefono(telefono.trim())
                    .direccion(direccion != null && !direccion.isBlank() ? direccion.trim() : null)
                    .referencia(referencia != null && !referencia.isBlank() ? referencia.trim() : null)
                    .ubigeo(ubigeoSel)
                    .fechaNacimiento(fechaNac)
                    .vigente(true)
                    .build());
        }

        Usuario usuario = Usuario.builder()
                .persona(persona)
                .rol(usuarioRol)
                .correo(correo)
                .password(passwordEncoder.encode(password))
                .estado("activo")
                .build();

        usuarioRepository.save(usuario);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, password));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);
            securityContextRepository.saveContext(contexto, request, response);

            flash.addFlashAttribute("exito", "Cuenta creada correctamente. Bienvenido, " + persona.getNombres() + ".");
        } catch (Exception e) {
            flash.addFlashAttribute("exito", "Cuenta creada correctamente. Ahora puedes iniciar sesion.");
            return "redirect:/?abrirLogin=true";
        }

        return "redirect:/";
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
