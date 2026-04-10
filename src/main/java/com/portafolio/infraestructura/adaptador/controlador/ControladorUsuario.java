package com.portafolio.infraestructura.adaptador.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario {

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, String>> perfil() {
        return ResponseEntity.ok(Map.of("mensaje", "Estás autenticado correctamente"));
    }
}
