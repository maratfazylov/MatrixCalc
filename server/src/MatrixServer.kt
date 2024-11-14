import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.sql.DriverManager
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import common.models.MatrixModel
import common.models.MatrixElementModel
import common.IMatrixServer

class MatrixServer(
    private val dbHelper: DbHelper = DbHelper(),
    private val port: Int = 8080
) : IMatrixServer {
    private var serverSocket: ServerSocket? = null
    private var running = false

    fun start() {
        try {
            serverSocket = ServerSocket(port)
            running = true
            println("Сервер запущен на порту $port")
            
            while (running) {
                try {
                    val clientSocket = serverSocket?.accept()
                    handleClient(clientSocket)
                } catch (e: Exception) {
                    if (running) {
                        println("Ошибка при обработке клиента: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка при запуске сервера: ${e.message}")
            throw e
        }
    }

    private fun handleClient(clientSocket: Socket?) {
        clientSocket?.use { socket ->
            val input = ObjectInputStream(socket.getInputStream())
            val output = ObjectOutputStream(socket.getOutputStream())
            
            try {
                while (running) {
                    when (val command = input.readObject() as String) {
                        "GET_ALL" -> {
                            val matrices = getAllMatrices()
                            output.writeObject(matrices)
                        }
                        "GET_BY_ID" -> {
                            val id = input.readObject() as Int
                            val matrix = getMatrix(id)
                            output.writeObject(matrix)
                        }
                        "SAVE" -> {
                            val matrix = input.readObject() as MatrixModel
                            val success = saveMatrix(matrix)
                            output.writeObject(success)
                        }
                        "EXIT" -> break
                        else -> println("Неизвестная команда: $command")
                    }
                    output.flush()
                }
            } catch (e: Exception) {
                println("Ошибка при обработке команды: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun getAllMatrices(): List<MatrixModel> {
        return try {
            dbHelper.getMatrices()
        } catch (e: Exception) {
            println("Ошибка при получении всех матриц: ${e.message}")
            emptyList()
        }
    }

    override fun getMatrix(id: Int): MatrixModel? {
        return try {
            dbHelper.getMatrix(id)
        } catch (e: Exception) {
            println("Ошибка при получении матрицы #$id: ${e.message}")
            null
        }
    }

    override fun saveMatrix(matrix: MatrixModel): Boolean {
        return try {
            matrix.elements.forEach { (pos, value) ->
                val sql = """
                    INSERT INTO `matrix` (`id`, `rows`, `cols`, `value_`) 
                    VALUES ('${matrix.id}', '${pos.first}', '${pos.second}', '${value}')
                """.trimIndent()
                
                dbHelper.executeUpdate(sql)
            }
            true
        } catch (e: Exception) {
            println("Ошибка при сохранении матрицы: ${e.message}")
            false
        }
    }

    override fun close() {
        running = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            println("Ошибка при закрытии сервера: ${e.message}")
        } finally {
            dbHelper.close()
        }
    }
}

