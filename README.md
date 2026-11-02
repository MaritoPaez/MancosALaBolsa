# Mancos a la Bolsa (v2.5)

Aplicación Android desarrollada en Kotlin y Jetpack Compose para realizar sorteos inteligentes de parejas de pádel de forma equitativa, flexible y divertida.

## 🚀 Novedades en la Versión 2.5
- **Resultados más limpios:** Se eliminó el detalle de la categoría (ej: `[C8]`) de los listados de resultados y compartido, mostrando de forma directa las posiciones y nombres de los jugadores.
- **Evitar repetición de parejas:** El motor de sorteo analiza automáticamente el historial reciente (últimos 3 sorteos) e intenta no repetir parejas que ya hayan jugado juntas, aplicando un sistema de penalización inteligente que mantiene flexibilidad si las opciones son escasas.

## ✨ Características Principales
- **Gestión de Jugadores:** Altas, bajas, modificación de nombres, posición (Drive `D`, Revés `R`, Ambos `X`) y categoría (`C1` a `C9`).
- **Exclusiones:** Configuración de duplas prohibidas que el sistema respetará automáticamente al sortear.
- **Nivelación Automática / Sorteo Inteligente:** Algoritmo de optimización que busca equilibrar el nivel de las parejas (suma objetivo ideal de categorías) y respetar las posiciones preferidas.
- **Historial de Sorteos:** Registro completo de los sorteos realizados en la base de datos local (Room).
- **Compartir Resultados:** Formato optimizado para enviar los resultados por WhatsApp o cualquier red social.

## 🛠️ Stack Tecnológico
- **Kotlin** & **Jetpack Compose** (UI declarativa moderna)
- **Material 3**
- **Room Database** (Persistencia local)
- **Coroutines & Flow** (Gestión asincrónica reactiva)
- **Navigation Compose**
