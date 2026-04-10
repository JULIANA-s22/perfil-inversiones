package com.portafolio.infraestructura.configuracion.seguridad;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;
import com.portafolio.dominio.puerto.salida.ProveedorToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class ProveedorTokenJwt implements ProveedorToken {

    private final SecretKey llave;
    private final long expiracionAccesoMs;
    private final long expiracionRefrescoMs;

    public ProveedorTokenJwt(
            @Value("${jwt.secreto}") String secreto,
            @Value("${jwt.expiracion-acceso-ms:900000}") long expiracionAccesoMs,
            @Value("${jwt.expiracion-refresco-ms:604800000}") long expiracionRefrescoMs) {
        this.llave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionAccesoMs = expiracionAccesoMs;
        this.expiracionRefrescoMs = expiracionRefrescoMs;
    }

    @Override
    public TokenAutenticacion generarToken(Usuario usuario) {
        long ahora = System.currentTimeMillis();

        String acceso = Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("usuarioId", usuario.getId().toString())
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expiracionAccesoMs))
                .signWith(llave)
                .compact();

        String refresco = Jwts.builder()
                .subject(usuario.getCorreo())
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expiracionRefrescoMs))
                .signWith(llave)
                .compact();

        return new TokenAutenticacion(acceso, refresco, expiracionAccesoMs / 1000);
    }

    @Override
    public String extraerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    @Override
    public boolean esValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(llave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
