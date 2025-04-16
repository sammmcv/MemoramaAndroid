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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.escom.practica4_1.data.FileManager
import com.escom.practica4_1.data.GameStateManager
import com.escom.practica4_1.model.GameState
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
    
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }
        
        // Pantalla de selección de dificultad
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
        
        // Pantalla de puntuaciones altas
        composable("high_scores") {
            HighScoresScreen(navController)
        }
        
        // Pantalla del juego
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
                    navController.navigate("save_game")
                }
            )
        }
        
        // Pantalla para guardar/cargar juego
        composable("save_game") {
            val gameState = GameStateManager.currentGameState
            if (gameState != null) {
                SaveGameScreen(
                    gameState = gameState,
                    onBackClick = { navController.popBackStack() },
                    onLoadGame = { loadedGameState ->
                        GameStateManager.currentGameState = loadedGameState
                        navController.navigate("game/${loadedGameState.difficulty}") {
                            popUpTo("main_menu")
                        }
                    },
                    onViewTextFile = { fileName, content ->
                        navController.navigate("view_text/$fileName/${content.hashCode()}")
                    },
                    onViewJsonFile = { fileName, content ->
                        navController.navigate("view_json/$fileName/${content.hashCode()}")
                    },
                    onViewXmlFile = { fileName, content ->
                        navController.navigate("view_xml/$fileName/${content.hashCode()}")
                    }
                )
            }
        }
    }
}
