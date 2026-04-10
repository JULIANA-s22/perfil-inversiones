package com.portafolio.infraestructura.configuracion.seguridad;

import com.portafolio.dominio.puerto.salida.ProveedorToken;
import com.portafolio.dominio.puerto.salida.RepositorioUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private final ProveedorToken proveedorToken;
    private final RepositorioUsuario repositorioUsuario;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            if (proveedorToken.esValido(token)) {
                String correo = proveedorToken.extraerCorreo(token);
                repositorioUsuario.buscarPorCorreo(correo).ifPresent(usuario -> {
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario.getCorreo(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
