import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.sql.ResultSet
import common.models.MatrixModel
import common.models.MatrixElementModel


class DbHelper(
    private val host: String = "localhost",
    private val port: Int = 3306,//порт
    private val user: String = "db_user",
    private val password: String = "12345",
) {
    private var conn: Connection? = null
    
    private fun ensureConnection() {
        if (conn?.isValid(5) != true) {
            try {
                conn?.close()
                conn = DriverManager.getConnection(
                    "jdbc:mysql://$host:$port/matrix?autoReconnect=true",
                    user,
                    password
                )
            } catch (e: Exception) {
                throw Exception("Не удалось подключиться к БД: ${e.message}")
            }
        }
    }

    fun getMatrices(): List<MatrixModel> {
        ensureConnection()
        
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        val matrixElements = mutableListOf<MatrixElementModel>()
        
        try {
            val sql = "SELECT * FROM matrix ORDER BY id"
            statement = conn?.createStatement()
            resultSet = statement?.executeQuery(sql)

            while (resultSet?.next() == true) {
                try {
                    val matrixId = resultSet.getInt("id")
                    val row = resultSet.getInt("rows")
                    val col = resultSet.getInt("cols")
                    val value = resultSet.getDouble("value_")
                    
                    matrixElements.add(MatrixElementModel(matrixId, row, col, value))
                } catch (e: Exception) {
                    println("Ошибка при чтении элемента: ${e.message}")
                    continue
                }
            }
        } catch (e: Exception) {
            println("Ошибка при получении данных: ${e.message}")
            throw e
        } finally {
            resultSet?.close()
            statement?.close()
        }

        return matrixElements
            .groupBy { it.matrixId }
            .map { (matrixId, elements) ->
                MatrixModel(matrixId).apply {
                    elements.forEach { element ->
                        this.elements[Pair(element.row, element.col)] = element.value
                    }
                }
            }
    }

    fun getMatrix(matrixId: Int): MatrixModel? {
        ensureConnection()
        
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        val matrix = MatrixModel(matrixId)
        
        try {
            val sql = "SELECT * FROM matrix WHERE id = $matrixId"
            statement = conn?.createStatement()
            resultSet = statement?.executeQuery(sql)

            while (resultSet?.next() == true) {
                try {
                    val row = resultSet.getInt("rows")
                    val col = resultSet.getInt("cols")
                    val value = resultSet.getDouble("value_")
                    
                    matrix.elements[Pair(row, col)] = value
                } catch (e: Exception) {
                    println("Ошибка при чтении элемента: ${e.message}")
                    continue
                }
            }
        } catch (e: Exception) {
            println("Ошибка при получении данных: ${e.message}")
            throw e
        } finally {
            resultSet?.close()
            statement?.close()
        }

        return if (matrix.elements.isEmpty()) null else matrix
    }

    fun close() {
        try {
            conn?.close()
        } catch (e: Exception) {
            println("Ошибка при закрытии соединения: ${e.message}")
        } finally {
            conn = null
        }
    }

    fun executeUpdate(sql: String) {
        ensureConnection()
        var statement: Statement? = null
        try {
            statement = conn?.createStatement()
            statement?.executeUpdate(sql)
        } finally {
            statement?.close()
        }
    }
}
