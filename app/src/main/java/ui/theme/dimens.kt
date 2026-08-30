package com.app.habitus.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado, radio de esquina y elevación centralizados.
 *
 * Antes cada pantalla usaba valores `dp` sueltos y ligeramente distintos
 * para lo mismo (8.dp aquí, 10.dp allá, 12.dp más allá...). Centralizarlos
 * aquí da consistencia real entre pantallas y hace que cambiar la "densidad"
 * general de la app sea un cambio en un solo sitio.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Radius {
    val sm = 10.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
    val pill = 999.dp
}

object Elevation {
    val flat = 0.dp
    val low = 2.dp
    val medium = 6.dp
    val high = 12.dp
}