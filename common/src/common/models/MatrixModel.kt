package common.models

import java.io.Serializable

data class MatrixModel(
    val id: Int,
    val elements: MutableMap<Pair<Int, Int>, Double> = mutableMapOf()
) : Serializable 