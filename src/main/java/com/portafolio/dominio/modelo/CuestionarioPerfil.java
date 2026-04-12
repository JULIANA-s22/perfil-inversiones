package com.portafolio.dominio.modelo;

import java.util.List;

public class CuestionarioPerfil {

    public static final List<String> PREGUNTAS = List.of(
            "¡Hola! Soy tu asesor de inversiones y voy a ayudarte a descubrir qué perfil se adapta mejor a ti: conservador, moderado o agresivo. Para comenzar: ¿tienes experiencia previa invirtiendo tu dinero, o sería tu primera vez?",
            "¿En cuánto tiempo aproximadamente necesitarás usar este dinero? ¿En menos de 3 años, entre 3 y 10 años, o en más de 10 años?",
            "¿Cuál es tu principal objetivo con esta inversión: proteger lo que tienes y ganar algo seguro, crecer de forma equilibrada, o maximizar las ganancias aunque implique más riesgo?",
            "Imagina que inviertes $10.000.000 COP y en tres meses bajan a $8.000.000. ¿Qué harías: retiras todo para no perder más, esperas pacientemente a que se recupere, o aprovechas para invertir más?",
            "¿Qué parte de tus ahorros totales vas a invertir: casi todo lo que tienes, aproximadamente la mitad, o una porción pequeña que no afectaría tu vida si la perdieras?",
            "¿Cuánto dinero en pesos colombianos puedes aportar mensualmente para seguir haciendo crecer esta inversión?",
            "Por último: si pudieras elegir entre una inversión que te garantiza ganar un 3% anual seguro, una que puede darte entre 0% y 14% dependiendo del mercado, o una que puede ir de -10% a +25% en el año, ¿cuál preferirías?"
    );

    private CuestionarioPerfil() {}
}

