package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.puerto.entrada.CasoUsoHistorial;
import com.portafolio.infraestructura.adaptador.controlador.dto.RespuestaHistorial;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/historial") @RequiredArgsConstructor
public class ControladorHistorial {
    private final CasoUsoHistorial casoUsoHistorial;

    @GetMapping
    public ResponseEntity<List<RespuestaHistorial>> obtenerHistorial(Authentication auth) {
        return ResponseEntity.ok(casoUsoHistorial.obtenerHistorial(auth.getName())
                .stream().map(RespuestaHistorial::desde).toList());
    }
}
