package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import com.portafolio.dominio.puerto.salida.RepositorioSimulacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioHistorialTest {

    @Mock
    RepositorioSimulacion repositorio;

    @InjectMocks
    ServicioHistorial servicio;

    // --- guardar ---

    @Test
    void guardar_debeRetornarElRegistroQueDevuelveElRepositorio() {
        // ARRANGE
        RegistroSimulacion registroEsperado = RegistroSimulacion.builder()
                .id(UUID.randomUUID())
                .correoUsuario("juan@test.com")
                .capitalActual(10_000)
                .aporteMensual(500)
                .tiempoAnios(5)
                .valorConservador(12_000)
                .valorModerado(14_000)
                .valorAgresivo(16_000)
                .creadoEn(LocalDateTime.now())
                .build();

        // le decimos al mock: cuando te llamen con cualquier RegistroSimulacion, devuelve este
        when(repositorio.guardar(any())).thenReturn(registroEsperado);

        // ACT
        RegistroSimulacion resultado = servicio.guardar(
                "juan@test.com", 10_000, 500, 5, 12_000, 14_000, 16_000
        );

        // ASSERT
        assertThat(resultado).isEqualTo(registroEsperado);
    }

    @Test
    void guardar_debeLlamarAlRepositorioExactamenteUnaVez() {
        // ARRANGE
        when(repositorio.guardar(any())).thenReturn(RegistroSimulacion.builder()
                .id(UUID.randomUUID()).correoUsuario("juan@test.com")
                .capitalActual(10_000).aporteMensual(500).tiempoAnios(5)
                .valorConservador(12_000).valorModerado(14_000).valorAgresivo(16_000)
                .creadoEn(LocalDateTime.now()).build());

        // ACT
        servicio.guardar("juan@test.com", 10_000, 500, 5, 12_000, 14_000, 16_000);

        // ASSERT — verifica que el repositorio fue llamado exactamente una vez con cualquier argumento
        verify(repositorio).guardar(any());
    }

    // --- obtenerHistorial ---

    @Test
    void obtenerHistorial_debeRetornarLaListaDelRepositorio() {
        // ARRANGE
        String correo = "juan@test.com";
        List<RegistroSimulacion> listaEsperada = List.of(
                RegistroSimulacion.builder()
                        .id(UUID.randomUUID()).correoUsuario(correo)
                        .capitalActual(10_000).aporteMensual(500).tiempoAnios(5)
                        .valorConservador(12_000).valorModerado(14_000).valorAgresivo(16_000)
                        .creadoEn(LocalDateTime.now()).build()
        );

        when(repositorio.buscarPorCorreo(correo)).thenReturn(listaEsperada);

        // ACT
        List<RegistroSimulacion> resultado = servicio.obtenerHistorial(correo);

        // ASSERT
        assertThat(resultado).isEqualTo(listaEsperada);
    }

    @Test
    void obtenerHistorial_debeRetornarListaVacia_cuandoNoHayRegistros() {
        // ARRANGE
        String correo = "sinhistorial@test.com";
        when(repositorio.buscarPorCorreo(correo)).thenReturn(List.of());

        // ACT
        List<RegistroSimulacion> resultado = servicio.obtenerHistorial(correo);

        // ASSERT
        assertThat(resultado).isEmpty();
    }
}
