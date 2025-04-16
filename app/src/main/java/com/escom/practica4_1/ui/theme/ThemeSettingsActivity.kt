package com.escom.practica4_1.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ThemeSettingsActivity : ComponentActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        const val THEME_PREFS = "theme_prefs"
        const val THEME_TYPE = "theme_type"
        const val THEME_MODE = "theme_mode"
        
        const val MODE_LIGHT = "light"
        const val MODE_DARK = "dark"
        const val MODE_SYSTEM = "system"
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sharedPreferences = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        
        // Obtener preferencias guardadas
        val savedThemeType = sharedPreferences.getString(THEME_TYPE, ThemeType.IPN.name) ?: ThemeType.IPN.name
        val savedThemeMode = sharedPreferences.getString(THEME_MODE, MODE_SYSTEM) ?: MODE_SYSTEM
        
        setContent {
            // Usar el tema actual para la pantalla de configuración
            val currentThemeType = ThemeType.valueOf(savedThemeType)
            val isDarkTheme = when (savedThemeMode) {
                MODE_LIGHT -> false
                MODE_DARK -> true
                else -> isSystemInDarkTheme() // Use system default instead of null
            }
            
            Practica4_1Theme(
                themeType = currentThemeType,
                darkTheme = isDarkTheme // Pass non-nullable Boolean
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThemeSettingsScreen(
                        initialThemeType = currentThemeType,
                        initialThemeMode = savedThemeMode,
                        onThemeTypeSelected = { themeType ->
                            // Guardar el tipo de tema
                            sharedPreferences.edit().putString(THEME_TYPE, themeType.name).apply()
                            // Recrear la actividad para aplicar el cambio
                            recreate()
                        },
                        onThemeModeSelected = { themeMode ->
                            // Guardar el modo de tema
                            sharedPreferences.edit().putString(THEME_MODE, themeMode).apply()
                            
                            // Aplicar el modo de tema
                            when (themeMode) {
                                MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                            
                            // Recrear la actividad para aplicar el cambio
                            recreate()
                        },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    initialThemeType: ThemeType,
    initialThemeMode: String,
    onThemeTypeSelected: (ThemeType) -> Unit,
    onThemeModeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedThemeType by remember { mutableStateOf(initialThemeType) }
    var selectedThemeMode by remember { mutableStateOf(initialThemeMode) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Tema") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
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
                .padding(16.dp)
        ) {
            // Sección de tipo de tema
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tipo de Tema",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Opción IPN
                    ThemeOption(
                        text = "IPN (Guinda)",
                        selected = selectedThemeType == ThemeType.IPN,
                        onClick = {
                            selectedThemeType = ThemeType.IPN
                            onThemeTypeSelected(ThemeType.IPN)
                        }
                    )
                    
                    // Opción ESCOM
                    ThemeOption(
                        text = "ESCOM (Azul)",
                        selected = selectedThemeType == ThemeType.ESCOM,
                        onClick = {
                            selectedThemeType = ThemeType.ESCOM
                            onThemeTypeSelected(ThemeType.ESCOM)
                        }
                    )
                    
                    // Opción DEFAULT
                    ThemeOption(
                        text = "Material Design (Predeterminado)",
                        selected = selectedThemeType == ThemeType.DEFAULT,
                        onClick = {
                            selectedThemeType = ThemeType.DEFAULT
                            onThemeTypeSelected(ThemeType.DEFAULT)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección de modo de tema
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Modo de Tema",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Opción Claro
                    ThemeOption(
                        text = "Claro",
                        selected = selectedThemeMode == ThemeSettingsActivity.MODE_LIGHT,
                        onClick = {
                            selectedThemeMode = ThemeSettingsActivity.MODE_LIGHT
                            onThemeModeSelected(ThemeSettingsActivity.MODE_LIGHT)
                        }
                    )
                    
                    // Opción Oscuro
                    ThemeOption(
                        text = "Oscuro",
                        selected = selectedThemeMode == ThemeSettingsActivity.MODE_DARK,
                        onClick = {
                            selectedThemeMode = ThemeSettingsActivity.MODE_DARK
                            onThemeModeSelected(ThemeSettingsActivity.MODE_DARK)
                        }
                    )
                    
                    // Opción Sistema
                    ThemeOption(
                        text = "Seguir sistema",
                        selected = selectedThemeMode == ThemeSettingsActivity.MODE_SYSTEM,
                        onClick = {
                            selectedThemeMode = ThemeSettingsActivity.MODE_SYSTEM
                            onThemeModeSelected(ThemeSettingsActivity.MODE_SYSTEM)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}