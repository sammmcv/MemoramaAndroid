package com.escom.practica4_1.ui.save

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.escom.practica4_1.MainActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.escom.practica4_1.R
import com.escom.practica4_1.data.FileManager
import com.escom.practica4_1.data.SavedGameInfo
import com.escom.practica4_1.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveGameScreen(
    gameState: GameState,
    onBackClick: () -> Unit,
    onLoadGame: (GameState) -> Unit,
    onViewTextFile: (String, String) -> Unit = { _, _ -> },
    onViewJsonFile: (String, String) -> Unit = { _, _ -> },
    onViewXmlFile: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    var selectedFormat by remember { mutableStateOf(FileManager.FORMAT_JSON) }
    var fileName by remember { mutableStateOf("game_save") }
    var savedGames by remember { mutableStateOf(fileManager.listSavedGames()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<SavedGameInfo?>(null) }
    
    // Launcher para seleccionar archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (fileManager.importGameFromUri(uri)) {
                // Actualizar la lista de partidas guardadas
                savedGames = fileManager.listSavedGames()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.save_load_game)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_menu)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botones para guardar/cargar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { showSaveDialog = true }) {
                    Text(stringResource(R.string.save_game))
                }
                
                Button(onClick = { showLoadDialog = true }) {
                    Text(stringResource(R.string.load_game))
                }
                
                // Nuevo botón para importar partidas
                Button(onClick = { 
                    filePickerLauncher.launch("*/*")
                }) {
                    Text("Importar")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de partidas guardadas
            Text(
                text = stringResource(R.string.saved_games),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dentro de LazyColumn en SaveGameScreen
                items(savedGames) { game ->
                    SavedGameItem(
                        game = game,
                        onLoadClick = {
                            val loadedGame = fileManager.loadGame(game.name)
                            if (loadedGame != null) {
                                onLoadGame(loadedGame)
                            }
                        },
                        onDeleteClick = {
                            fileManager.deleteSavedGame(game.name)
                            savedGames = fileManager.listSavedGames()
                        },
                        onOpenExternallyClick = {
                            fileManager.openSavedGameExternally(
                                game.name, 
                                game.format,
                                onViewTextFile = if (game.format == FileManager.FORMAT_TXT) onViewTextFile else null,
                                onViewJsonFile = if (game.format == FileManager.FORMAT_JSON) onViewJsonFile else null,
                                onViewXmlFile = if (game.format == FileManager.FORMAT_XML) onViewXmlFile else null
                            )
                        },
                        // Añadir este parámetro que faltaba
                        onShareClick = {
                            // Implementar la funcionalidad de compartir
                            // Puedes usar el método shareGame que añadimos al FileManager
                            fileManager.shareGame(game.name, game.format)
                        },
                        onViewTextFile = onViewTextFile,
                        onViewJsonFile = onViewJsonFile,
                        onViewXmlFile = onViewXmlFile
                    )
                }
            }
        }
        
        // Diálogo para guardar partida
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text(stringResource(R.string.save_game)) },
                text = {
                    Column {
                        TextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text(stringResource(R.string.file_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(stringResource(R.string.select_format))
                        
                        FormatOption(
                            text = "Texto plano (.txt)",
                            selected = selectedFormat == FileManager.FORMAT_TXT,
                            onClick = { selectedFormat = FileManager.FORMAT_TXT }
                        )
                        
                        FormatOption(
                            text = "XML (.xml)",
                            selected = selectedFormat == FileManager.FORMAT_XML,
                            onClick = { selectedFormat = FileManager.FORMAT_XML }
                        )
                        
                        FormatOption(
                            text = "JSON (.json)",
                            selected = selectedFormat == FileManager.FORMAT_JSON,
                            onClick = { selectedFormat = FileManager.FORMAT_JSON }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                fileManager.saveGame(gameState, selectedFormat, fileName)
                                savedGames = fileManager.listSavedGames()
                                showSaveDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    Button(onClick = { showSaveDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        
        // Diálogo para cargar partida
        if (showLoadDialog) {
            AlertDialog(
                onDismissRequest = { showLoadDialog = false },
                title = { Text(stringResource(R.string.load_game)) },
                text = {
                    LazyColumn {
                        items(savedGames) { game ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedGame == game,
                                    onClick = { selectedGame = game }
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Formato: ${game.format.uppercase()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = formatDate(game.timestamp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedGame?.let { game ->
                                val loadedGame = fileManager.loadGame(game.name)
                                if (loadedGame != null) {
                                    onLoadGame(loadedGame)
                                    showLoadDialog = false
                                }
                            }
                        },
                        enabled = selectedGame != null
                    ) {
                        Text(stringResource(R.string.load))
                    }
                },
                dismissButton = {
                    Button(onClick = { showLoadDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun FormatOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedGameItem(
    game: SavedGameInfo,
    onLoadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenExternallyClick: () -> Unit,
    onShareClick: () -> Unit, // Nuevo parámetro
    onViewTextFile: (String, String) -> Unit,
    onViewJsonFile: (String, String) -> Unit,
    onViewXmlFile: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onLoadClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Formato: ${game.format.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatDate(game.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para compartir
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share, // Necesitamos importar este ícono
                        contentDescription = "Compartir"
                    )
                }
                
                // Botón para abrir externamente
                IconButton(onClick = onOpenExternallyClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Abrir externamente"
                    )
                }
                
                // Botón para eliminar
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
