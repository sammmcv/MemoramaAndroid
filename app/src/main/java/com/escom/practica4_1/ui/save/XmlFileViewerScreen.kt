package com.escom.practica4_1.ui.save

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XmlFileViewerScreen(
    fileName: String,
    xmlContent: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "XML: $fileName") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Intentamos formatear el XML para mejor visualización
            val formattedXml = try {
                prettyPrintXml(xmlContent)
            } catch (e: Exception) {
                xmlContent
            }
            
            Text(
                text = formattedXml,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Función para formatear XML
private fun prettyPrintXml(xml: String): String {
    try {
        val xmlInput = StreamSource(StringReader(xml))
        val stringWriter = StringWriter()
        val xmlOutput = StreamResult(stringWriter)
        
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        
        // Configurar el formato
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        
        transformer.transform(xmlInput, xmlOutput)
        
        return stringWriter.toString()
    } catch (e: Exception) {
        return xml // Si hay error, devolver el XML original
    }
}