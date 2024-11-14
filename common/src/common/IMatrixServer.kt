package common

import common.models.MatrixModel

interface IMatrixServer {
    fun getAllMatrices(): List<MatrixModel>
    fun getMatrix(id: Int): MatrixModel?
    fun saveMatrix(matrix: MatrixModel): Boolean
    fun close()
} 