package com.jhon.micontroldidi.data.repository

import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio del perfil de trabajo.
 *
 * Solo existe un perfil: [guardar] siempre escribe sobre
 * [PerfilTrabajoEntity.ID_UNICO], sin importar el id que traiga la entidad.
 */
class PerfilTrabajoRepository(private val perfilTrabajoDao: PerfilTrabajoDao) {

    fun observar(): Flow<PerfilTrabajoEntity?> =
        perfilTrabajoDao.observar(PerfilTrabajoEntity.ID_UNICO)

    suspend fun obtener(): PerfilTrabajoEntity? =
        perfilTrabajoDao.obtener(PerfilTrabajoEntity.ID_UNICO)

    /**
     * Guarda el perfil validando las reglas de negocio.
     * Retorna [Result.success] si los datos son válidos o [Result.failure]
     * con la excepción correspondiente.
     */
    suspend fun guardar(perfil: PerfilTrabajoEntity): Result<Unit> {
        return try {
            validar(perfil)
            perfilTrabajoDao.guardar(perfil.copy(id = PerfilTrabajoEntity.ID_UNICO))
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validar(perfil: PerfilTrabajoEntity) {
        require(perfil.plataforma.isNotBlank()) {
            "La plataforma principal es obligatoria"
        }
        require(perfil.vehiculo.isNotBlank()) {
            "El vehículo es obligatorio"
        }
        require(perfil.ciudad.isNotBlank()) {
            "La ciudad es obligatoria"
        }
        require(perfil.diasLaboralesSet.isNotEmpty()) {
            "Debes seleccionar al menos un día de trabajo"
        }
        require(perfil.horaInicioMinutos in 0 until MINUTOS_DIA) {
            "La hora de inicio no es válida"
        }
        require(perfil.horaFinMinutos in 0 until MINUTOS_DIA) {
            "La hora de fin no es válida"
        }
        require(perfil.horaInicioMinutos < perfil.horaFinMinutos) {
            "La hora de inicio debe ser anterior a la hora de fin"
        }
        require(perfil.maxPorcentajeKmVacios in 0..100) {
            "El porcentaje de kilómetros vacíos debe estar entre 0 y 100"
        }

        validarReserva("aceite", perfil.costoAceite, perfil.intervaloAceiteKm)
        validarReserva("llantas", perfil.costoLlantas, perfil.intervaloLlantasKm)
        validarReserva("frenos", perfil.costoFrenos, perfil.intervaloFrenosKm)
        validarReserva("kit de arrastre", perfil.costoKitArrastre, perfil.intervaloKitArrastreKm)
        validarReserva("mantenimiento", perfil.costoMantenimiento, perfil.intervaloMantenimientoKm)
        validarReserva("depreciación", perfil.costoDepreciacion, perfil.intervaloDepreciacionKm)
    }

    private fun validarReserva(nombre: String, costo: Long, intervaloKm: Long) {
        require(costo >= 0) { "El costo de $nombre no puede ser negativo" }
        require(intervaloKm >= 0) { "El intervalo de $nombre no puede ser negativo" }
        require(costo == 0L || intervaloKm > 0) {
            "El intervalo de $nombre debe ser mayor que cero"
        }
    }

    private companion object {
        const val MINUTOS_DIA = 24 * 60
    }
}
