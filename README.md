# Memorama - Práctica 4.1

Aplicación de Memorama desarrollada en Android con Jetpack Compose, que permite jugar, guardar y cargar partidas, visualizar archivos de guardado en diferentes formatos (TXT, XML, JSON), y personalizar el tema visual de la app.

## Características

- Juego de Memorama con selección de dificultad (fácil, medio, difícil).
- Guardado y carga de partidas en formatos TXT, XML y JSON.
- Visualización interna de archivos de guardado.
- Compartir partidas guardadas con otras aplicaciones.
- Importar partidas desde archivos externos.
- Personalización de temas: IPN, ESCOM y tema por defecto, con soporte para modo claro/oscuro.
- Visualización de puntuaciones más altas por dificultad.

## Instalación

1. Clona este repositorio:
    ```bash
    git clone https://github.com/tu_usuario/Practica4_1.git
    ```
2. Abre el proyecto en Android Studio.
3. Conecta un dispositivo o usa un emulador.
4. Ejecuta la aplicación.

## Permisos

La aplicación solicita permisos de lectura y escritura en almacenamiento externo para compartir e importar archivos de partidas guardadas.

## Estructura del Proyecto

- `app/src/main/java/com/escom/practica4_1/`
  - `data/` - Manejo de archivos y repositorio de juego.
  - `model/` - Modelos de datos (por ejemplo, estado del juego, cartas).
  - `ui/` - Pantallas y componentes de la interfaz de usuario.
- `app/src/main/res/` - Recursos (layouts, strings, temas, iconos).
- `app/build.gradle.kts` - Configuración del módulo de la app.

## Uso

- Inicia la app y selecciona la dificultad.
- Juega y guarda tu partida en el formato que prefieras.
- Consulta tus puntuaciones más altas desde el menú principal.
- Personaliza el tema desde el botón de configuración (ícono de engranaje).
- Comparte o importa partidas desde la pantalla de guardado.

## Créditos

Desarrollado por sammmcv para la materia de Desarrollo de Aplicaciones Móviles en ESCOM-IPN.

## Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.