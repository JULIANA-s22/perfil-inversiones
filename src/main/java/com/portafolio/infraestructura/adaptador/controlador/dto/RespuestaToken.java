package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.TokenAutenticacion;

public record RespuestaToken(String tokenAcceso, String tokenRefresco, long expiraEnSegundos) {

    public static RespuestaToken desde(TokenAutenticacion token) {
        return new RespuestaToken(
                token.tokenAcceso(),
                token.tokenRefresco(),
                token.expiraEnSegundos()
        );
    }
}
