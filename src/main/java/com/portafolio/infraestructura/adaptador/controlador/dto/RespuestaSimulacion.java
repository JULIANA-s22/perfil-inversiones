package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.ProyeccionAnual;
import com.portafolio.dominio.modelo.ResultadoPerfil;
import com.portafolio.dominio.modelo.ResultadoSimulacion;

import java.util.List;

public record RespuestaSimulacion(
        long capitalActual,
        long aporteMensual,
        int tiempoAnios,
        PerfilDto conservador,
        PerfilDto moderado,
        PerfilDto agresivo,
        List<ProyeccionAnualDto> proyeccionConservador,
        List<ProyeccionAnualDto> proyeccionModerado,
        List<ProyeccionAnualDto> proyeccionAgresivo
) {
    public record PerfilDto(String nombre, double tasaAnual, long valorFuturo, String etiqueta) {
        public static PerfilDto desde(ResultadoPerfil perfil) {
            return new PerfilDto(perfil.getNombre(), perfil.getTasaAnual(), perfil.getValorFuturo(), perfil.getEtiqueta());
        }
    }

    public record ProyeccionAnualDto(int anio, long pasivo, long proyectado) {
        public static ProyeccionAnualDto desde(ProyeccionAnual p) {
            return new ProyeccionAnualDto(p.getAnio(), p.getPasivo(), p.getProyectado());
        }
    }

    public static RespuestaSimulacion desde(ResultadoSimulacion r) {
        return new RespuestaSimulacion(
                r.getCapitalActual(),
                r.getAporteMensual(),
                r.getTiempoAnios(),
                PerfilDto.desde(r.getConservador()),
                PerfilDto.desde(r.getModerado()),
                PerfilDto.desde(r.getAgresivo()),
                r.getProyeccionConservador().stream().map(ProyeccionAnualDto::desde).toList(),
                r.getProyeccionModerado().stream().map(ProyeccionAnualDto::desde).toList(),
                r.getProyeccionAgresivo().stream().map(ProyeccionAnualDto::desde).toList()
        );
    }
}