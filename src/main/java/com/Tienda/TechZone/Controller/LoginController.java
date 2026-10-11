package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.PedidoRepository;
import com.Tienda.TechZone.Repository.RolRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Service.DetallePedidoService;
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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoService detallePedidoService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> LoginApi(@RequestParam String username,
                                        @RequestParam String password,
                                        jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);
            jakarta.servlet.http.HttpSession sesion = request.getSession(true);
            sesion.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
            try {
                request.changeSessionId();
            } catch (Exception e) {
                // Contenedor sin soporte: se continua con la misma sesion
            }
            respuesta.put("ok", true);
        } catch (Exception e) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Correo o contrasena incorrectos");
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

        Usuario usuario = Usuario.builder()
                .rol(clienteRol)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .telefono(telefono)
                .password(passwordEncoder.encode(password))
                .estadoUsuario(true)
                .build();

        usuarioRepository.save(usuario);
        flash.addFlashAttribute("exito", "Cuenta creada correctamente. Ahora puedes iniciar sesion.");
        return "redirect:/?abrirLogin=true";
    }

    @GetMapping("/debug/mis-pedidos")
    @ResponseBody
    @Transactional(readOnly = true)
    public String DebugMisPedidos(@RequestParam String correo) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
            if (usuario == null) {
                return "USUARIO NO ENCONTRADO: " + correo;
            }
            List<Pedido> pedidos = pedidoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
            StringBuilder sb = new StringBuilder();
            sb.append("OK | Usuario: ").append(correo)
              .append(" | Pedidos: ").append(pedidos.size());
            for (Pedido p : pedidos) {
                sb.append(" | [#").append(p.getIdPedido())
                  .append(" estado=").append(p.getEstado())
                  .append(" total=").append(p.getTotal())
                  .append(" fecha=").append(p.getFechaPedido())
                  .append(" detalles=").append(detallePedidoService.ListarDetallesPorPedido(p.getIdPedido()).size())
                  .append("]");
            }
            return sb.toString();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "ERROR: " + e.getClass().getName() + ": " + e.getMessage() + "\n" + sw.toString();
        }
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
        return "Usuario: " + u.getCorreo() + " | Rol: " + u.getRol().getNombre() + " | Estado: " + u.getEstadoUsuario() + " | Password match: " + matches + " | Password hash: " + u.getPassword();
    }

    @GetMapping("/acceso-denegado")
    public String AccesoDenegado() {
        return "acceso-denegado";
    }

    @GetMapping("/mi-perfil")
    public String MiPerfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElseThrow();
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/actualizar-perfil")
    public String ActualizarPerfil(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String nombres,
            @RequestParam String apellidos,
            @RequestParam String telefono,
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String passwordNuevo,
            RedirectAttributes flash) {
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElseThrow();

        if (passwordNuevo != null && !passwordNuevo.isEmpty()) {
            if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                flash.addFlashAttribute("error", "La contrasena actual es incorrecta");
                return "redirect:/mi-perfil";
            }
            usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        }

        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setTelefono(telefono);
        usuarioRepository.save(usuario);

        flash.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/mi-perfil";
    }
}
