package com.portafolio.dominio.modelo;

public record TokenAutenticacion(String tokenAcceso, String tokenRefresco, long expiraEnSegundos) {
}
