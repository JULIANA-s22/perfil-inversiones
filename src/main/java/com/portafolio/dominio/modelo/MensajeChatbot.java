package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChatbot {

    private Integer id;
    private Integer conversacionId;
    private RolMensaje rol;
    private String contenido;
    @Builder.Default
    private LocalDateTime enviadoEn = LocalDateTime.now();

    public enum RolMensaje {
        USUARIO, ASISTENTE
    }

    public static MensajeChatbot crear(Integer conversacionId, RolMensaje rol, String contenido) {
        return MensajeChatbot.builder()
                .conversacionId(conversacionId)
                .rol(rol)
                .contenido(contenido)
                .build();
    }
}
