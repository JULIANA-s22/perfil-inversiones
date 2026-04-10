package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.modelo.DocumentoPdf;
import com.portafolio.dominio.puerto.entrada.CasoUsoGestionPdf;
import com.portafolio.infraestructura.adaptador.controlador.dto.PeticionSolicitudPdf;
import com.portafolio.infraestructura.adaptador.controlador.dto.RespuestaDocumentoPdf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class ControladorPdf {

    private final CasoUsoGestionPdf casoUsoGestionPdf;

    @PostMapping("/solicitar")
    public ResponseEntity<RespuestaDocumentoPdf> solicitar(@Valid @RequestBody PeticionSolicitudPdf peticion) {
        DocumentoPdf documento = casoUsoGestionPdf.solicitar(peticion.aDominio());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaDocumentoPdf.desde(documento));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<RespuestaDocumentoPdf>> listarPorUsuario(@PathVariable UUID usuarioId) {
        List<RespuestaDocumentoPdf> documentos = casoUsoGestionPdf.listarPorUsuario(usuarioId)
                .stream()
                .map(RespuestaDocumentoPdf::desde)
                .toList();
        return ResponseEntity.ok(documentos);
    }

    @PutMapping("/{id}/iniciar-procesamiento")
    public ResponseEntity<Void> iniciarProcesamiento(@PathVariable Integer id) {
        casoUsoGestionPdf.iniciarProcesamiento(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> completar(@PathVariable Integer id, @RequestParam String urlBucket) {
        casoUsoGestionPdf.completar(id, urlBucket);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/error")
    public ResponseEntity<Void> marcarError(@PathVariable Integer id) {
        casoUsoGestionPdf.marcarError(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
