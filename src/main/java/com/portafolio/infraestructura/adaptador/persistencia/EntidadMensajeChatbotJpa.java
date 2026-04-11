package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.MensajeChatbot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chatbot")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntidadMensajeChatbotJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "conversacion_id", nullable = false)
    private Integer conversacionId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "rol", nullable = false, columnDefinition = "rol_mensaje")
    private MensajeChatbot.RolMensaje rol;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "enviado_en", nullable = false)
    private LocalDateTime enviadoEn;

    public static EntidadMensajeChatbotJpa desdeDominio(MensajeChatbot mensaje) {
        return EntidadMensajeChatbotJpa.builder()
                .id(mensaje.getId())
                .conversacionId(mensaje.getConversacionId())
                .rol(mensaje.getRol())
                .contenido(mensaje.getContenido())
                .enviadoEn(mensaje.getEnviadoEn())
                .build();
    }

    public MensajeChatbot aDominio() {
        return MensajeChatbot.builder()
                .id(id)
                .conversacionId(conversacionId)
                .rol(rol)
                .contenido(contenido)
                .enviadoEn(enviadoEn)
                .build();
    }
}
