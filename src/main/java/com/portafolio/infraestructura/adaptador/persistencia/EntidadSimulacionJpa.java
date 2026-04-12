package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "simulaciones")
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EntidadSimulacionJpa {
    @Id private UUID id;
    @Column(name = "correo_usuario", nullable = false) private String correoUsuario;
    @Column(name = "capital_actual") private long capitalActual;
    @Column(name = "aporte_mensual") private long aporteMensual;
    @Column(name = "tiempo_anios") private int tiempoAnios;
    @Column(name = "valor_conservador") private long valorConservador;
    @Column(name = "valor_moderado") private long valorModerado;
    @Column(name = "valor_agresivo") private long valorAgresivo;
    @Column(name = "creado_en") private LocalDateTime creadoEn;

    public static EntidadSimulacionJpa desdeDominio(RegistroSimulacion r) {
        return EntidadSimulacionJpa.builder().id(r.getId()).correoUsuario(r.getCorreoUsuario())
                .capitalActual(r.getCapitalActual()).aporteMensual(r.getAporteMensual()).tiempoAnios(r.getTiempoAnios())
                .valorConservador(r.getValorConservador()).valorModerado(r.getValorModerado()).valorAgresivo(r.getValorAgresivo())
                .creadoEn(r.getCreadoEn()).build();
    }
    public RegistroSimulacion aDominio() {
        return RegistroSimulacion.builder().id(id).correoUsuario(correoUsuario)
                .capitalActual(capitalActual).aporteMensual(aporteMensual).tiempoAnios(tiempoAnios)
                .valorConservador(valorConservador).valorModerado(valorModerado).valorAgresivo(valorAgresivo)
                .creadoEn(creadoEn).build();
    }
}
