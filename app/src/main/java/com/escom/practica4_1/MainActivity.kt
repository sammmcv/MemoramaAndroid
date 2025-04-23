package com.escom.practica4_1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log  // Añadir esta importación
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.escom.practica4_1.data.FileManager
import com.escom.practica4_1.data.GameStateManager
//import com.escom.practica4_1.model.GameState
import com.escom.practica4_1.ui.game.GameScreen
import com.escom.practica4_1.ui.menu.DifficultyScreen
import com.escom.practica4_1.ui.menu.HighScoresScreen
import com.escom.practica4_1.ui.menu.MainMenuScreen
import com.escom.practica4_1.ui.save.JsonFileViewerScreen
import com.escom.practica4_1.ui.save.SaveGameScreen
import com.escom.practica4_1.ui.save.TextFileViewerScreen
import com.escom.practica4_1.ui.save.XmlFileViewerScreen
import com.escom.practica4_1.ui.theme.Practica4_1Theme
import com.escom.practica4_1.ui.theme.ThemeSettingsActivity
import com.escom.practica4_1.ui.theme.ThemeType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Cargar preferencias de tema
        val sharedPreferences = getSharedPreferences(ThemeSettingsActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val themeType = sharedPreferences.getString(ThemeSettingsActivity.THEME_TYPE, ThemeType.IPN.name)
        val themeMode = sharedPreferences.getString(ThemeSettingsActivity.THEME_MODE, ThemeSettingsActivity.MODE_SYSTEM)
        
        // Aplicar modo de tema (claro/oscuro/sistema)
        when (themeMode) {
            ThemeSettingsActivity.MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeSettingsActivity.MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        
        setContent {
            // Convertir el string a enum ThemeType
            val currentThemeType = try {
                ThemeType.valueOf(themeType ?: ThemeType.IPN.name)
            } catch (e: Exception) {
                ThemeType.IPN
            }
            
            // Determinar si usar tema oscuro basado en el modo seleccionado
            val isDarkTheme = when (themeMode) {
                ThemeSettingsActivity.MODE_LIGHT -> false
                ThemeSettingsActivity.MODE_DARK -> true
                else -> null // null para seguir el sistema
            }
            
            Practica4_1Theme(
                darkTheme = isDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme(),
                themeType = currentThemeType
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoramaAppWithThemeSettings()
                }
            }
        }
    }
}

@Composable
fun MemoramaAppWithThemeSettings() {
    val context = LocalContext.current
    
    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, ThemeSettingsActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración de Tema"
                )
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MemoramaApp()
        }
    }
}

@Composable
fun MemoramaApp() {
    val navController = rememberNavController()
    val context = LocalContext.current // Obtener el contexto aquí
    val fileManager = remember { FileManager(context) } // Crear instancia de FileManager

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }

        composable("difficulty") {
            DifficultyScreen(
                onDifficultySelected = { difficulty ->
                    navController.navigate("game/$difficulty")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("high_scores") {
            HighScoresScreen(navController)
        }

        composable(
            route = "game/{difficulty}",
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "easy"
            GameScreen(
                difficulty = difficulty,
                onBackToMenuClick = {
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                },
                onSaveGameClick = {
                    // Asegúrate de que GameStateManager.currentGameState se establece antes de navegar
                    // Esto normalmente se haría en GameScreen antes de llamar a onSaveGameClick
                    navController.navigate("save_game")
                }
            )
        }

        composable("save_game") {
            val gameState = GameStateManager.currentGameState // Recuperar el estado actual
            if (gameState != null) {
                SaveGameScreen(
                    gameState = gameState,
                    onBackClick = { navController.popBackStack() },
                    onLoadGame = { loadedGameState ->
                        GameStateManager.currentGameState = loadedGameState // Guardar estado cargado
                        navController.navigate("game/${loadedGameState.difficulty}") {
                            popUpTo("main_menu") // Volver al menú principal antes de ir al juego
                        }
                    },
                    // Modificar lambdas para navegar a la nueva ruta unificada
                    onViewTextFile = { fileName, _ -> // El contenido no se pasa por navegación
                        navController.navigate("view_file/$fileName/${FileManager.FORMAT_TXT}")
                    },
                    onViewJsonFile = { fileName, _ ->
                        navController.navigate("view_file/$fileName/${FileManager.FORMAT_JSON}")
                    },
                    onViewXmlFile = { fileName, _ ->
                        navController.navigate("view_file/$fileName/${FileManager.FORMAT_XML}")
                    }
                )
            } else {
                // Opcional: Manejar el caso donde gameState es null, quizás volver atrás o mostrar mensaje
                Log.e("MemoramaApp", "Error: GameState es null al navegar a save_game.")
                // navController.popBackStack() // Ejemplo: Volver si no hay estado
            }
        }

        // Nueva ruta unificada para visualizar archivos
        composable(
            route = "view_file/{fileName}/{format}",
            arguments = listOf(
                navArgument("fileName") { type = NavType.StringType },
                navArgument("format") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: "unknown"
            val format = backStackEntry.arguments?.getString("format") ?: FileManager.FORMAT_TXT
            
            // Leer el contenido del archivo usando FileManager
            // Es importante manejar el caso donde la lectura falla
            val fileContent = try {
                fileManager.readTextFileContent(fileName, format)
            } catch (e: Exception) {
                Log.e("MemoramaApp", "Error leyendo archivo $fileName.$format: ${e.message}")
                "Error al leer el archivo: ${e.message}" // Mostrar mensaje de error
            }

            when (format) {
                FileManager.FORMAT_TXT -> TextFileViewerScreen(
                    fileName = fileName,
                    fileContent = fileContent,
                    onBackClick = { navController.popBackStack() }
                )
                FileManager.FORMAT_JSON -> JsonFileViewerScreen(
                    fileName = fileName,
                    jsonContent = fileContent,
                    onBackClick = { navController.popBackStack() }
                )
                FileManager.FORMAT_XML -> XmlFileViewerScreen(
                    fileName = fileName,
                    xmlContent = fileContent,
                    onBackClick = { navController.popBackStack() }
                )
                else -> {
                    // Opcional: Mostrar un mensaje de formato no soportado o volver
                    Log.e("MemoramaApp", "Formato de archivo no soportado: $format")
                    navController.popBackStack()
                }
            }
        }
    }
}
