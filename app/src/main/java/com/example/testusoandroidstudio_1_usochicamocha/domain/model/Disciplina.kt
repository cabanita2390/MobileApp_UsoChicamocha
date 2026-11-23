package com.example.testusoandroidstudio_1_usochicamocha.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Una disciplina de Subestaciones y lo que hace en el móvil, tal como la sirve el backend
 * (GET /substation/disciplinas). El backend es la única fuente: la app no tiene nombres ni reglas
 * de disciplina escritos en su código. Se guarda en Room para trabajar sin señal y se actualiza
 * cada vez que se sincroniza el catálogo.
 */
data class Disciplina(
    val codigo: String,
    val nombre: String,
    /** Letra corta ("E"). */
    val sigla: String,
    val orden: Int,
    /** La app permite registrar visitas de esta disciplina; las demás se ven en solo consulta. */
    val capturaMovil: Boolean,
    /** [CONJUNTOS_NO], [CONJUNTOS_OPCIONAL] o [CONJUNTOS_OBLIGATORIO]. */
    val conjuntos: String,
    val usaElementos: Boolean,
    val usaEquipos: Boolean,
    val usaControles: Boolean
) {
    val usaConjuntos: Boolean get() = conjuntos == CONJUNTOS_OPCIONAL || conjuntos == CONJUNTOS_OBLIGATORIO

    /** El formulario tiene el paso «¿Qué se intervino?». */
    val usaIntervenido: Boolean get() = usaConjuntos || usaElementos

    companion object {
        const val CONJUNTOS_NO = "NO"
        const val CONJUNTOS_OPCIONAL = "OPCIONAL"
        const val CONJUNTOS_OBLIGATORIO = "OBLIGATORIO"
    }
}

/** La disciplina con ese código, o null si el backend no la conoce. */
fun List<Disciplina>.de(codigo: String?): Disciplina? = firstOrNull { it.codigo == codigo }

/** Se puede registrar desde el móvil. Una disciplina desconocida no se registra. */
fun List<Disciplina>.capturaMovil(codigo: String?): Boolean = de(codigo)?.capturaMovil == true

/** Su formulario tiene el paso «¿Qué se intervino?». */
fun List<Disciplina>.usaIntervenido(codigo: String?): Boolean = de(codigo)?.usaIntervenido == true

/** Las que se registran desde el móvil, en el orden del backend. */
fun List<Disciplina>.conCaptura(): List<Disciplina> = filter { it.capturaMovil }.sortedBy { it.orden }

/**
 * Las disciplinas vigentes en memoria. Las llena el repositorio de Subestaciones desde Room al
 * arrancar y después de cada sincronización. Las pantallas que deciden con ellas (captura habilitada,
 * «¿Qué se intervino?») las observan con [flujo] para redibujarse cuando cambian.
 */
object DisciplinasVigentes {
    private val _flujo = MutableStateFlow<List<Disciplina>>(emptyList())
    val flujo: StateFlow<List<Disciplina>> = _flujo.asStateFlow()

    val actual: List<Disciplina> get() = _flujo.value

    fun actualizar(disciplinas: List<Disciplina>) {
        _flujo.value = disciplinas.sortedBy { it.orden }
    }
}
