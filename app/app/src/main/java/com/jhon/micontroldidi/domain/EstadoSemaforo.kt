package com.jhon.micontroldidi.domain

/**
 * Estado del semáforo de eficiencia según el porcentaje de kilómetros vacíos
 * frente al umbral máximo configurado en el perfil de trabajo.
 */
enum class EstadoSemaforo {
    VERDE,
    ROJO,
    NEUTRO,
    PENDIENTE
}
