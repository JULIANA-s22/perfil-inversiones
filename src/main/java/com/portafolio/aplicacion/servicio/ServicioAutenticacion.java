package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;
import com.portafolio.dominio.puerto.entrada.CasoUsoAutenticacion;
import com.portafolio.dominio.puerto.salida.EncriptadorContrasena;
import com.portafolio.dominio.puerto.salida.ProveedorToken;
import com.portafolio.dominio.puerto.salida.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion implements CasoUsoAutenticacion {

    private final RepositorioUsuario repositorioUsuario;
    private final ProveedorToken proveedorToken;
    private final EncriptadorContrasena encriptadorContrasena;

    @Override
    public Usuario registrar(String correo, String contrasena, String nombreCompleto) {
        if (repositorioUsuario.existePorCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado: " + correo);
        }
        String contrasenaEncriptada = encriptadorContrasena.encriptar(contrasena);
        Usuario usuario = Usuario.crear(correo, contrasenaEncriptada, nombreCompleto);
        return repositorioUsuario.guardar(usuario);
    }

    @Override
    public TokenAutenticacion iniciarSesion(String correo, String contrasena) {
        Usuario usuario = repositorioUsuario.buscarPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!encriptadorContrasena.coincide(contrasena, usuario.getContrasena())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        return proveedorToken.generarToken(usuario);
    }

    @Override
    public TokenAutenticacion refrescarToken(String tokenRefresco) {
        if (!proveedorToken.esValido(tokenRefresco)) {
            throw new IllegalArgumentException("Token de refresco inválido o expirado");
        }
        String correo = proveedorToken.extraerCorreo(tokenRefresco);
        Usuario usuario = repositorioUsuario.buscarPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return proveedorToken.generarToken(usuario);
    }
}
