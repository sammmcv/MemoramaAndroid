package com.escom.practica4_1.data

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.escom.practica4_1.model.Card
import com.escom.practica4_1.model.GameState
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class FileManager(private val context: Context) {

// Método para compartir un juego guardado
fun shareGame(fileName: String, format: String): Boolean {
    val file = File(filesDir, "$fileName.$format")
    if (!file.exists()) return false
    
    return try {
        // Crear un archivo temporal en el directorio de caché externo para compartirlo
        val cacheDir = context.externalCacheDir ?: return false
        val tempFile = File(cacheDir, "$fileName.$format")
        
        // Copiar el contenido del archivo interno al archivo temporal
        file.inputStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        // Crear un URI para el archivo temporal usando FileProvider
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        
        // Crear un intent para compartir el archivo
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = getMimeType(format)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Crear un chooser para seleccionar la aplicación
        val chooser = Intent.createChooser(
            shareIntent, 
            "Compartir partida guardada"
        )
        
        // Iniciar la actividad
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        true
    } catch (e: Exception) {
        Log.e("FileManager", "Error compartiendo archivo: ${e.message}")
        Toast.makeText(
            context,
            "Error al compartir el archivo: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
        false
    }
}
    companion object {
        const val FORMAT_TXT = "txt"
        const val FORMAT_XML = "xml"
        const val FORMAT_JSON = "json"
    }
    
    private val filesDir = context.filesDir
    
    // Guardar partida en el formato seleccionado
    fun saveGame(gameState: GameState, format: String, fileName: String = "game_save"): Boolean {
        return when (format) {
            FORMAT_TXT -> saveAsTxt(gameState, fileName)
            FORMAT_XML -> saveAsXml(gameState, fileName)
            FORMAT_JSON -> saveAsJson(gameState, fileName)
            else -> false
        }
    }
    
    // Cargar partida desde cualquier formato
    fun loadGame(fileName: String): GameState? {
    val txtFile = File(filesDir, "$fileName.$FORMAT_TXT")
    val xmlFile = File(filesDir, "$fileName.$FORMAT_XML")
    val jsonFile = File(filesDir, "$fileName.$FORMAT_JSON")
    
    val result = when {
        txtFile.exists() -> {
            Log.d("FileManager", "Cargando desde TXT: $fileName")
            loadFromTxt(fileName)
        }
        xmlFile.exists() -> {
            Log.d("FileManager", "Cargando desde XML: $fileName")
            loadFromXml(fileName)
        }
        jsonFile.exists() -> {
            Log.d("FileManager", "Cargando desde JSON: $fileName")
            loadFromJson(fileName)
        }
        else -> {
            Log.d("FileManager", "No se encontró ningún archivo para: $fileName")
            null
        }
    }
    
    // Log del resultado para depuración
    if (result != null) {
        Log.d("FileManager", "Partida cargada: ${result.difficulty}, Pares: ${result.matchedPairs}/${result.totalPairs}, Cartas: ${result.cards.size}")
    } else {
        Log.d("FileManager", "Error al cargar la partida")
    }
    
    return result
    }
    
    // Guardar como texto plano
    private fun saveAsTxt(gameState: GameState, fileName: String): Boolean {
        val file = File(filesDir, "$fileName.$FORMAT_TXT")
        return try {
            FileWriter(file).use { writer ->
                // Información básica
                writer.append("difficulty=${gameState.difficulty}\n")
                writer.append("totalPairs=${gameState.totalPairs}\n")
                writer.append("matchedPairs=${gameState.matchedPairs}\n")
                writer.append("moves=${gameState.moves}\n")
                writer.append("timeElapsed=${gameState.timeElapsed}\n")
                writer.append("isGameOver=${gameState.isGameOver}\n")
                writer.append("score=${gameState.score}\n")
                writer.append("gameMode=${gameState.gameMode}\n")
                writer.append("timeLimit=${gameState.timeLimit}\n")
                writer.append("isTimeUp=${gameState.isTimeUp}\n")
                
                // Guardar cartas
                writer.append("cards=")
                gameState.cards.forEachIndexed { index, card ->
                    writer.append("${card.id},${card.pairId},${card.isFlipped},${card.isMatched}")
                    if (index < gameState.cards.size - 1) writer.append(";")
                }
                writer.append("\n")
                
                // Historial de movimientos (ejemplo simple)
                writer.append("moveHistory=Movimiento 1;Movimiento 2;Movimiento 3\n")
                
                true
            }
        } catch (e: IOException) {
            Log.e("FileManager", "Error guardando archivo TXT: ${e.message}")
            false
        }
    }
    
    // Guardar como XML
    private fun saveAsXml(gameState: GameState, fileName: String): Boolean {
        val file = File(filesDir, "$fileName.$FORMAT_XML")
        return try {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            val doc: Document = docBuilder.newDocument()
            
            // Elemento raíz
            val rootElement: Element = doc.createElement("gameState")
            doc.appendChild(rootElement)
            
            // Información básica
            addXmlElement(doc, rootElement, "difficulty", gameState.difficulty)
            addXmlElement(doc, rootElement, "totalPairs", gameState.totalPairs.toString())
            addXmlElement(doc, rootElement, "matchedPairs", gameState.matchedPairs.toString())
            addXmlElement(doc, rootElement, "moves", gameState.moves.toString())
            addXmlElement(doc, rootElement, "timeElapsed", gameState.timeElapsed.toString())
            addXmlElement(doc, rootElement, "isGameOver", gameState.isGameOver.toString())
            addXmlElement(doc, rootElement, "score", gameState.score.toString())
            addXmlElement(doc, rootElement, "gameMode", gameState.gameMode)
            addXmlElement(doc, rootElement, "timeLimit", gameState.timeLimit.toString())
            addXmlElement(doc, rootElement, "isTimeUp", gameState.isTimeUp.toString())
            
            // Cartas
            val cardsElement = doc.createElement("cards")
            rootElement.appendChild(cardsElement)
            
            gameState.cards.forEach { card ->
                val cardElement = doc.createElement("card")
                addXmlElement(doc, cardElement, "id", card.id.toString())
                addXmlElement(doc, cardElement, "pairId", card.pairId.toString())
                addXmlElement(doc, cardElement, "isFlipped", card.isFlipped.toString())
                addXmlElement(doc, cardElement, "isMatched", card.isMatched.toString())
                cardsElement.appendChild(cardElement)
            }
            
            // Historial de movimientos
            val historyElement = doc.createElement("moveHistory")
            addXmlElement(doc, historyElement, "move", "Movimiento 1")
            addXmlElement(doc, historyElement, "move", "Movimiento 2")
            addXmlElement(doc, historyElement, "move", "Movimiento 3")
            rootElement.appendChild(historyElement)
            
            // Escribir a archivo
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val source = DOMSource(doc)
            val result = StreamResult(file)
            transformer.transform(source, result)
            
            true
        } catch (e: Exception) {
            Log.e("FileManager", "Error guardando archivo XML: ${e.message}")
            false
        }
    }
    
    // Función auxiliar para añadir elementos XML
    private fun addXmlElement(doc: Document, parent: Element, name: String, value: String) {
        val element = doc.createElement(name)
        element.appendChild(doc.createTextNode(value))
        parent.appendChild(element)
    }
    
    // Guardar como JSON
    private fun saveAsJson(gameState: GameState, fileName: String): Boolean {
        val file = File(filesDir, "$fileName.$FORMAT_JSON")
        return try {
            val jsonObject = JSONObject()
            
            // Información básica
            jsonObject.put("difficulty", gameState.difficulty)
            jsonObject.put("totalPairs", gameState.totalPairs)
            jsonObject.put("matchedPairs", gameState.matchedPairs)
            jsonObject.put("moves", gameState.moves)
            jsonObject.put("timeElapsed", gameState.timeElapsed)
            jsonObject.put("isGameOver", gameState.isGameOver)
            jsonObject.put("score", gameState.score)
            jsonObject.put("gameMode", gameState.gameMode)
            jsonObject.put("timeLimit", gameState.timeLimit)
            jsonObject.put("isTimeUp", gameState.isTimeUp)
            
            // Cartas
            val cardsArray = JSONArray()
            gameState.cards.forEach { card ->
                val cardObject = JSONObject()
                cardObject.put("id", card.id)
                cardObject.put("pairId", card.pairId)
                cardObject.put("isFlipped", card.isFlipped)
                cardObject.put("isMatched", card.isMatched)
                cardsArray.put(cardObject)
            }
            jsonObject.put("cards", cardsArray)
            
            // Historial de movimientos
            val historyArray = JSONArray()
            historyArray.put("Movimiento 1")
            historyArray.put("Movimiento 2")
            historyArray.put("Movimiento 3")
            jsonObject.put("moveHistory", historyArray)
            
            FileWriter(file).use { it.write(jsonObject.toString(4)) }
            true
        } catch (e: Exception) {
            Log.e("FileManager", "Error guardando archivo JSON: ${e.message}")
            false
        }
    }
    
    // Cargar desde texto plano
    private fun loadFromTxt(fileName: String): GameState? {
        val file = File(filesDir, "$fileName.$FORMAT_TXT")
        if (!file.exists()) return null
        
        try {
            val lines = file.readLines()
            val properties = mutableMapOf<String, String>()
            
            // Leer propiedades
            lines.forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    properties[parts[0]] = parts[1]
                }
            }
            
            // Crear GameState
            val difficulty = properties["difficulty"] ?: return null
            val totalPairs = properties["totalPairs"]?.toIntOrNull() ?: return null
            
            val gameState = GameState(difficulty, totalPairs)
            gameState.matchedPairs = properties["matchedPairs"]?.toIntOrNull() ?: 0
            gameState.moves = properties["moves"]?.toIntOrNull() ?: 0
            gameState.timeElapsed = properties["timeElapsed"]?.toLongOrNull() ?: 0L
            gameState.isGameOver = properties["isGameOver"]?.toBoolean() ?: false
            gameState.score = properties["score"]?.toIntOrNull() ?: 0
            gameState.gameMode = properties["gameMode"] ?: "classic"
            gameState.timeLimit = properties["timeLimit"]?.toLongOrNull() ?: 30000L
            gameState.isTimeUp = properties["isTimeUp"]?.toBoolean() ?: false
            
            // Cargar cartas
            val cardsStr = properties["cards"]
            if (cardsStr != null) {
                val cardsList = mutableListOf<Card>()
                cardsStr.split(";").forEach { cardStr ->
                    val cardProps = cardStr.split(",")
                    if (cardProps.size == 4) {
                        val id = cardProps[0].toIntOrNull() ?: 0
                        val pairId = cardProps[1].toIntOrNull() ?: 0
                        val isFlipped = cardProps[2].toBoolean()
                        val isMatched = cardProps[3].toBoolean()
                        cardsList.add(Card(id, pairId, isFlipped, isMatched))
                    }
                }
                gameState.cards = cardsList
            }
            
            return gameState
        } catch (e: Exception) {
            Log.e("FileManager", "Error cargando archivo TXT: ${e.message}")
            return null
        }
    }
    
    // Cargar desde XML
    private fun loadFromXml(fileName: String): GameState? {
        val file = File(filesDir, "$fileName.$FORMAT_XML")
        if (!file.exists()) return null
        
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(file)
            doc.documentElement.normalize()
            
            val difficulty = getXmlElementValue(doc, "difficulty") ?: return null
            val totalPairs = getXmlElementValue(doc, "totalPairs")?.toIntOrNull() ?: return null
            
            val gameState = GameState(difficulty, totalPairs)
            gameState.matchedPairs = getXmlElementValue(doc, "matchedPairs")?.toIntOrNull() ?: 0
            gameState.moves = getXmlElementValue(doc, "moves")?.toIntOrNull() ?: 0
            gameState.timeElapsed = getXmlElementValue(doc, "timeElapsed")?.toLongOrNull() ?: 0L
            gameState.isGameOver = getXmlElementValue(doc, "isGameOver")?.toBoolean() ?: false
            gameState.score = getXmlElementValue(doc, "score")?.toIntOrNull() ?: 0
            gameState.gameMode = getXmlElementValue(doc, "gameMode") ?: "classic"
            gameState.timeLimit = getXmlElementValue(doc, "timeLimit")?.toLongOrNull() ?: 30000L
            // Corregir el nombre de la propiedad de isTimeUp (era "timeUp")
            gameState.isTimeUp = getXmlElementValue(doc, "isTimeUp")?.toBoolean() ?: false
            
            // Cargar cartas
            val cardNodes = doc.getElementsByTagName("card")
            val cardsList = mutableListOf<Card>()
            
            for (i in 0 until cardNodes.length) {
                val cardElement = cardNodes.item(i) as Element
                val id = cardElement.getElementsByTagName("id").item(0).textContent.toIntOrNull() ?: 0
                val pairId = cardElement.getElementsByTagName("pairId").item(0).textContent.toIntOrNull() ?: 0
                val isFlipped = cardElement.getElementsByTagName("isFlipped").item(0).textContent.toBoolean()
                val isMatched = cardElement.getElementsByTagName("isMatched").item(0).textContent.toBoolean()
                cardsList.add(Card(id, pairId, isFlipped, isMatched))
            }
            
            gameState.cards = cardsList
            return gameState
        } catch (e: Exception) {
            Log.e("FileManager", "Error cargando archivo XML: ${e.message}")
            return null
        }
    }
    
    // Función auxiliar para obtener valores de elementos XML
    private fun getXmlElementValue(doc: Document, tagName: String): String? {
        val nodeList = doc.getElementsByTagName(tagName)
        if (nodeList.length > 0) {
            return nodeList.item(0).textContent
        }
        return null
    }
    
    // Cargar desde JSON
    private fun loadFromJson(fileName: String): GameState? {
        val file = File(filesDir, "$fileName.$FORMAT_JSON")
        if (!file.exists()) return null
        
        try {
            val jsonString = file.readText()
            val jsonObject = JSONObject(jsonString)
            
            val difficulty = jsonObject.getString("difficulty")
            val totalPairs = jsonObject.getInt("totalPairs")
            
            val gameState = GameState(difficulty, totalPairs)
            gameState.matchedPairs = jsonObject.getInt("matchedPairs")
            gameState.moves = jsonObject.getInt("moves")
            gameState.timeElapsed = jsonObject.getLong("timeElapsed")
            gameState.isGameOver = jsonObject.getBoolean("isGameOver")
            gameState.score = jsonObject.getInt("score")
            gameState.gameMode = jsonObject.getString("gameMode")
            gameState.timeLimit = jsonObject.getLong("timeLimit")
            gameState.isTimeUp = jsonObject.getBoolean("isTimeUp")
            
            // Cargar cartas
            val cardsArray = jsonObject.getJSONArray("cards")
            val cardsList = mutableListOf<Card>()
            
            for (i in 0 until cardsArray.length()) {
                val cardObject = cardsArray.getJSONObject(i)
                val id = cardObject.getInt("id")
                val pairId = cardObject.getInt("pairId")
                val isFlipped = cardObject.getBoolean("isFlipped")
                val isMatched = cardObject.getBoolean("isMatched")
                cardsList.add(Card(id, pairId, isFlipped, isMatched))
            }
            
            gameState.cards = cardsList
            return gameState
        } catch (e: Exception) {
            Log.e("FileManager", "Error cargando archivo JSON: ${e.message}")
            return null
        }
    }
    
    // Listar archivos guardados
    fun listSavedGames(): List<SavedGameInfo> {
        val savedGames = mutableListOf<SavedGameInfo>()
        
        filesDir.listFiles()?.forEach { file ->
            val name = file.nameWithoutExtension
            val format = file.extension
            
            if (format in listOf(FORMAT_TXT, FORMAT_XML, FORMAT_JSON)) {
                savedGames.add(SavedGameInfo(name, format, file.lastModified()))
            }
        }
        
        return savedGames
    }
    
    // Eliminar un juego guardado
    fun deleteSavedGame(fileName: String): Boolean {
        var deleted = false
        
        for (format in listOf(FORMAT_TXT, FORMAT_XML, FORMAT_JSON)) {
            val file = File(filesDir, "$fileName.$format")
            if (file.exists()) {
                deleted = file.delete() || deleted
            }
        }
        
        return deleted
    }
    
    // Abrir archivo de guardado con una aplicación externa
    fun readTextFileContent(fileName: String, format: String): String {
        val file = File(filesDir, "$fileName.$format")
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                Log.e("FileManager", "Error leyendo archivo: ${e.message}")
                "Error al leer el archivo: ${e.message}"
            }
        } else {
            "El archivo no existe"
        }
    }
    
    fun openSavedGameExternally(
        fileName: String, 
        format: String, 
        onViewTextFile: ((String, String) -> Unit)? = null,
        onViewJsonFile: ((String, String) -> Unit)? = null,
        onViewXmlFile: ((String, String) -> Unit)? = null
    ): Boolean {
        val file = File(filesDir, "$fileName.$format")
        if (!file.exists()) return false
        
        // Si es un archivo de texto y se proporciona el callback, usar el visor interno
        if (format == FORMAT_TXT && onViewTextFile != null) {
            val content = readTextFileContent(fileName, format)
            onViewTextFile(fileName, content)
            return true
        }
        
        // Si es un archivo JSON y se proporciona el callback, usar el visor interno
        if (format == FORMAT_JSON && onViewJsonFile != null) {
            val content = readTextFileContent(fileName, format)
            onViewJsonFile(fileName, content)
            return true
        }
        
        // Si es un archivo XML y se proporciona el callback, usar el visor interno
        if (format == FORMAT_XML && onViewXmlFile != null) {
            val content = readTextFileContent(fileName, format)
            onViewXmlFile(fileName, content)
            return true
        }
        
        return try {
            // Crear un archivo temporal en el directorio de caché externo para compartirlo
            val cacheDir = context.externalCacheDir ?: return false
            val tempFile = File(cacheDir, "$fileName.$format")
            
            // Copiar el contenido del archivo interno al archivo temporal
            file.inputStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Crear un URI para el archivo temporal usando FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            
            // Crear un intent para abrir el archivo
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, getMimeType(format))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Verificar si hay aplicaciones que puedan manejar este tipo de archivo
            if (intent.resolveActivity(context.packageManager) != null) {
                // Iniciar la actividad
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // Mostrar un mensaje si no hay aplicaciones disponibles
                Toast.makeText(
                    context,
                    "No hay aplicaciones disponibles para abrir este tipo de archivo",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        } catch (e: Exception) {
            Log.e("FileManager", "Error abriendo archivo externamente: ${e.message}")
            Toast.makeText(
                context,
                "Error al abrir el archivo: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
    
    // Determinar el tipo MIME según el formato del archivo
    private fun getMimeType(format: String): String {
        return when (format) {
            FORMAT_TXT -> "text/plain"
            FORMAT_XML -> "text/xml"
            FORMAT_JSON -> "application/json"
            else -> "*/*"
        }
    }
    fun importGameFromUri(uri: android.net.Uri): Boolean {
        try {
            // Obtener el nombre del archivo
            val fileName = getFileNameFromUri(uri) ?: return false
            val extension = fileName.substringAfterLast('.', "")
            
            // Verificar que la extensión sea válida
            val format = when (extension.lowercase()) {
                FORMAT_TXT -> FORMAT_TXT
                FORMAT_XML -> FORMAT_XML
                FORMAT_JSON -> FORMAT_JSON
                else -> return false
            }
            
            // Nombre base sin extensión
            val baseName = fileName.substringBeforeLast('.')
            
            // Crear un archivo en el directorio de la aplicación
            val targetFile = File(filesDir, "$baseName.$format")
            
            // Copiar el contenido del URI al archivo
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Verificar que el archivo se pueda cargar como un estado de juego válido
            val gameState = loadGame(baseName)
            if (gameState != null) {
                Toast.makeText(context, "Partida importada correctamente", Toast.LENGTH_SHORT).show()
                return true
            } else {
                // Si no se pudo cargar, eliminar el archivo
                targetFile.delete()
                Toast.makeText(context, "El archivo no contiene una partida válida", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Log.e("FileManager", "Error importando archivo: ${e.message}")
            Toast.makeText(context, "Error al importar el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    
    // Función auxiliar para obtener el nombre del archivo desde un URI
    private fun getFileNameFromUri(uri: android.net.Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
}

// Clase para representar información de juegos guardados
data class SavedGameInfo(
    val name: String,
    val format: String,
    val timestamp: Long
)
