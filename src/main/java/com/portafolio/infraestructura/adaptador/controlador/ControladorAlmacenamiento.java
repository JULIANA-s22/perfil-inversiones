package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.modelo.UrlPresignada;
import com.portafolio.dominio.puerto.entrada.CasoUsoDocumentos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/almacenamiento")
@RequiredArgsConstructor
public class ControladorAlmacenamiento {

    private final CasoUsoDocumentos casoUsoDocumentos;

    @PostMapping("/presigned-url")
    public ResponseEntity<UrlPresignada> obtenerUrlSubida(
            @RequestParam String nombreArchivo,
            @RequestParam(defaultValue = "application/pdf") String tipoContenido) {

        return ResponseEntity.ok(casoUsoDocumentos.obtenerUrlSubida(nombreArchivo, tipoContenido));
    }
}
