package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversaciones_chatbot")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntidadConversacionChatbotJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "iniciada_en", nullable = false)
    private LocalDateTime iniciadaEn;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "estado", nullable = false, columnDefinition = "estado_conversacion")
    private ConversacionChatbot.EstadoConversacion estado;

    public static EntidadConversacionChatbotJpa desdeDominio(ConversacionChatbot conversacion) {
        return EntidadConversacionChatbotJpa.builder()
                .id(conversacion.getId())
                .usuarioId(conversacion.getUsuarioId())
                .iniciadaEn(conversacion.getIniciadaEn())
                .estado(conversacion.getEstado())
                .build();
    }

    public ConversacionChatbot aDominio() {
        return ConversacionChatbot.builder()
                .id(id)
                .usuarioId(usuarioId)
                .iniciadaEn(iniciadaEn)
                .estado(estado)
                .build();
    }
}
