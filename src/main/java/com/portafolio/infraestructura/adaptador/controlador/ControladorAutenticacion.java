package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;
import com.portafolio.dominio.puerto.entrada.CasoUsoAutenticacion;
import com.portafolio.infraestructura.adaptador.controlador.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/autenticacion")
@RequiredArgsConstructor
public class ControladorAutenticacion {

    private final CasoUsoAutenticacion casoUsoAutenticacion;

    @PostMapping("/registro")
    public ResponseEntity<RespuestaUsuario> registrar(@Valid @RequestBody PeticionRegistro peticion) {
        Usuario usuario = casoUsoAutenticacion.registrar(
                peticion.correo(), peticion.contrasena(), peticion.nombreCompleto());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaUsuario.desde(usuario));
    }

    @PostMapping("/inicio-sesion")
    public ResponseEntity<RespuestaToken> iniciarSesion(@Valid @RequestBody PeticionInicioSesion peticion) {
        TokenAutenticacion token = casoUsoAutenticacion.iniciarSesion(
                peticion.correo(), peticion.contrasena());
        return ResponseEntity.ok(RespuestaToken.desde(token));
    }

    @PostMapping("/refrescar")
    public ResponseEntity<RespuestaToken> refrescar(@Valid @RequestBody PeticionRefrescoToken peticion) {
        TokenAutenticacion token = casoUsoAutenticacion.refrescarToken(peticion.tokenRefresco());
        return ResponseEntity.ok(RespuestaToken.desde(token));
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, String>> perfil() {
        return ResponseEntity.ok(Map.of("mensaje", "Estás autenticado correctamente"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
