package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.modelo.ResultadoSimulacion;
import com.portafolio.dominio.puerto.entrada.CasoUsoHistorial;
import com.portafolio.dominio.puerto.entrada.CasoUsoSimulacion;
import com.portafolio.infraestructura.adaptador.controlador.dto.PeticionSimulacion;
import com.portafolio.infraestructura.adaptador.controlador.dto.RespuestaSimulacion;
import com.portafolio.infraestructura.adaptador.mensajeria.MensajeSimulacion;
import com.portafolio.infraestructura.adaptador.mensajeria.ProductorNotificacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/simulacion") @RequiredArgsConstructor
public class ControladorSimulacion {
    private final CasoUsoSimulacion casoUsoSimulacion;
    private final CasoUsoHistorial casoUsoHistorial;
    private final ProductorNotificacion productorNotificacion;

    @PostMapping
    public ResponseEntity<RespuestaSimulacion> simular(
            @Valid @RequestBody PeticionSimulacion peticion,
            @RequestParam(defaultValue = "false") boolean enviarCorreo,
            Authentication auth) {

        ResultadoSimulacion resultado = casoUsoSimulacion.simular(
                peticion.capitalActual(), peticion.aporteMensual(), peticion.tiempoAnios());

        String correo = auth.getName();

        casoUsoHistorial.guardar(correo, peticion.capitalActual(), peticion.aporteMensual(),
                peticion.tiempoAnios(), resultado.getConservador().getValorFuturo(),
                resultado.getModerado().getValorFuturo(), resultado.getAgresivo().getValorFuturo());

        if (enviarCorreo) {
            productorNotificacion.publicarSimulacion(MensajeSimulacion.builder()
                    .correoDestino(correo).capitalActual(peticion.capitalActual())
                    .aporteMensual(peticion.aporteMensual()).tiempoAnios(peticion.tiempoAnios())
                    .valorConservador(resultado.getConservador().getValorFuturo())
                    .valorModerado(resultado.getModerado().getValorFuturo())
                    .valorAgresivo(resultado.getAgresivo().getValorFuturo()).build());
        }

        return ResponseEntity.ok(RespuestaSimulacion.desde(resultado));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
