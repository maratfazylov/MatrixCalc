fun main() {
    val server = MatrixServer()
    
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\nЗакрытие сервера...")
        server.close()
    })
    
    try {
        server.start()
    } catch (e: Exception) {
        println("Критическая ошибка сервера: ${e.message}")
    }
}