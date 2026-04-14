package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;
import com.portafolio.dominio.puerto.salida.EncriptadorContrasena;
import com.portafolio.dominio.puerto.salida.ProveedorToken;
import com.portafolio.dominio.puerto.salida.RepositorioUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    @Mock
    RepositorioUsuario repositorioUsuario;

    @Mock
    ProveedorToken proveedorToken;

    @Mock
    EncriptadorContrasena encriptadorContrasena;

    @InjectMocks
    ServicioAutenticacion servicio;

    // -------------------------------------------------------------------------
    // registrar
    // -------------------------------------------------------------------------

    @Test
    void registrar_debeGuardarYRetornarUsuario_cuandoCorreoNoExiste() {
        // ARRANGE
        String correo = "juan@test.com";
        String contrasena = "1234";
        String nombre = "Juan Pérez";

        Usuario usuarioGuardado = Usuario.builder()
                .id(UUID.randomUUID()).correo(correo)
                .contrasena("hash-encriptado").nombreCompleto(nombre)
                .build();

        when(repositorioUsuario.existePorCorreo(correo)).thenReturn(false);
        when(encriptadorContrasena.encriptar(contrasena)).thenReturn("hash-encriptado");
        when(repositorioUsuario.guardar(any())).thenReturn(usuarioGuardado);

        // ACT
        Usuario resultado = servicio.registrar(correo, contrasena, nombre);

        // ASSERT
        assertThat(resultado).isEqualTo(usuarioGuardado);
    }

    @Test
    void registrar_debeLanzarExcepcion_cuandoCorreoYaEstaRegistrado() {
        // ARRANGE
        String correo = "juan@test.com";
        when(repositorioUsuario.existePorCorreo(correo)).thenReturn(true);

        // ACT + ASSERT — assertThatThrownBy es la forma fluida de assertThrows
        assertThatThrownBy(() -> servicio.registrar(correo, "1234", "Juan"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(correo);
    }

    // -------------------------------------------------------------------------
    // iniciarSesion
    // -------------------------------------------------------------------------

    @Test
    void iniciarSesion_debeRetornarToken_cuandoCredencialesSonValidas() {
        // ARRANGE
        String correo = "juan@test.com";
        String contrasena = "1234";

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).correo(correo)
                .contrasena("hash-encriptado").nombreCompleto("Juan")
                .build();

        TokenAutenticacion tokenEsperado = new TokenAutenticacion("access-token", "refresh-token", 3600);

        when(repositorioUsuario.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));
        when(encriptadorContrasena.coincide(contrasena, "hash-encriptado")).thenReturn(true);
        when(proveedorToken.generarToken(usuario)).thenReturn(tokenEsperado);

        // ACT
        TokenAutenticacion resultado = servicio.iniciarSesion(correo, contrasena);

        // ASSERT
        assertThat(resultado).isEqualTo(tokenEsperado);
    }

    @Test
    void iniciarSesion_debeLanzarExcepcion_cuandoUsuarioNoExiste() {
        // ARRANGE
        String correo = "noexiste@test.com";
        when(repositorioUsuario.buscarPorCorreo(correo)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.iniciarSesion(correo, "1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void iniciarSesion_debeLanzarExcepcion_cuandoContrasenaEsIncorrecta() {
        // ARRANGE
        String correo = "juan@test.com";

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).correo(correo)
                .contrasena("hash-encriptado").nombreCompleto("Juan")
                .build();

        when(repositorioUsuario.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));
        when(encriptadorContrasena.coincide(anyString(), anyString())).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.iniciarSesion(correo, "contrasena-incorrecta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciales inválidas");
    }

    // -------------------------------------------------------------------------
    // refrescarToken
    // -------------------------------------------------------------------------

    @Test
    void refrescarToken_debeRetornarNuevoToken_cuandoTokenEsValido() {
        // ARRANGE
        String tokenRefresco = "refresh-token-valido";
        String correo = "juan@test.com";

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).correo(correo)
                .contrasena("hash").nombreCompleto("Juan")
                .build();

        TokenAutenticacion tokenEsperado = new TokenAutenticacion("nuevo-access", "nuevo-refresh", 3600);

        when(proveedorToken.esValido(tokenRefresco)).thenReturn(true);
        when(proveedorToken.extraerCorreo(tokenRefresco)).thenReturn(correo);
        when(repositorioUsuario.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));
        when(proveedorToken.generarToken(usuario)).thenReturn(tokenEsperado);

        // ACT
        TokenAutenticacion resultado = servicio.refrescarToken(tokenRefresco);

        // ASSERT
        assertThat(resultado).isEqualTo(tokenEsperado);
    }

    @Test
    void refrescarToken_debeLanzarExcepcion_cuandoTokenEsInvalido() {
        // ARRANGE
        String tokenInvalido = "token-expirado";
        when(proveedorToken.esValido(tokenInvalido)).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.refrescarToken(tokenInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token de refresco inválido o expirado");
    }

    @Test
    void refrescarToken_debeLanzarExcepcion_cuandoTokenEsValidoPeroUsuarioNoExiste() {
        // ARRANGE
        String tokenRefresco = "token-valido";
        String correo = "eliminado@test.com";

        when(proveedorToken.esValido(tokenRefresco)).thenReturn(true);
        when(proveedorToken.extraerCorreo(tokenRefresco)).thenReturn(correo);
        when(repositorioUsuario.buscarPorCorreo(correo)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.refrescarToken(tokenRefresco))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado");
    }

}
