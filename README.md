# Документация по клиент-серверному приложению для работы с матрицами


## Архитектура приложения

### Общая структура
```
project/
├── common/          # Общие интерфейсы и модели
├── server/          # Серверная часть
├── client/          # Клиентская часть
└── lib/            # Внешние библиотеки
```

## Взаимодействие компонентов

### 1. Клиент ↔ Сервер

Взаимодействие осуществляется через интерфейс `IMatrixServer`:

```kotlin
interface IMatrixServer {
    fun getAllMatrices(): List<MatrixModel>
    fun getMatrix(id: Int): MatrixModel?
    fun saveMatrix(matrix: MatrixModel): Boolean
    fun close()
}
```

#### Команды клиент-серверного взаимодействия:
- `GET_ALL` - получить все матрицы
- `GET_BY_ID` - получить матрицу по ID
- `SAVE` - сохранить новую матрицу
- `EXIT` - завершить соединение

### 2. Сервер ↔ База данных

Взаимодействие через класс `DbHelper`:

```kotlin
class DbHelper {
    fun getMatrices(): List<MatrixModel>
    fun getMatrix(id: Int): MatrixModel?
    fun saveMatrixElement(id: Int, row: Int, col: Int, value: Double)
    fun executeUpdate(sql: String)
    fun close()
}
```

#### Структура таблицы в БД:
```sql
CREATE TABLE matrix (
    id INT NOT NULL,
    rows INT NOT NULL,
    cols INT NOT NULL,
    value_ DOUBLE NOT NULL,
    PRIMARY KEY (id, rows, cols)
);
```

## Последовательность операций

### 1. Получение всех матриц
1. Клиент вызывает `getAllMatrices()`
2. `MatrixServerProxy` отправляет команду `GET_ALL`
3. Сервер получает команду и вызывает `DbHelper.getMatrices()`
4. БД возвращает все записи
5. Сервер преобразует записи в `List<MatrixModel>`
6. Результат возвращается клиенту

### 2. Получение матрицы по ID
1. Клиент вызывает `getMatrix(id)`
2. `MatrixServerProxy` отправляет команду `GET_BY_ID` и ID
3. Сервер вызывает `DbHelper.getMatrix(id)`
4. БД возвращает записи для указанного ID
5. Сервер создает `MatrixModel`
6. Результат возвращается клиенту

### 3. Сложение матриц
1. Клиент получает две матрицы через `getMatrix()`
2. Сложение выполняется на стороне клиента
3. Результат сохраняется через `saveMatrix()`
4. `MatrixServerProxy` отправляет команду `SAVE`
5. Сервер вызывает `DbHelper.saveMatrixElement()` для каждого элемента
6. БД сохраняет новые записи

## Форматы данных

### MatrixModel
```kotlin
data class MatrixModel(
    val id: Int,
    val elements: MutableMap<Pair<Int, Int>, Double>
) : Serializable
```

### Формат записи в БД
```
id    | rows | cols | value_
------|------|------|-------
1     | 1    | 1    | 1.0
1     | 1    | 2    | 2.0
1     | 2    | 1    | 3.0
1     | 2    | 2    | 4.0
```

## Обработка ошибок

1. Сетевые ошибки обрабатываются в `MatrixServerProxy`
2. Ошибки БД обрабатываются в `DbHelper`
3. Ошибки валидации (например, разные размеры матриц) обрабатываются на клиенте

## Примеры использования

### Консольный клиент
```kotlin
val server = MatrixServerProxy()
val client = MatrixClient(server)
client.displayAllMatrices()
client.displayMatrixById(1)
```

### GUI клиент
```kotlin
SwingUtilities.invokeLater {
    MainWindow(MatrixServerProxy()).isVisible = true
}
```

## Сетевое взаимодействие

1. Сервер слушает порт 8080
2. Клиент подключается через Socket
3. Обмен данными через ObjectInputStream/ObjectOutputStream
4. Все объекты должны быть Serializable

## Безопасность

1. Используются PreparedStatement для защиты от SQL-инъекций
2. Проверка размеров матриц перед операциями
3. Валидация входных данных на клиенте

