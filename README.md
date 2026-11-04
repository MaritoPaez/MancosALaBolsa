# Mancos a la Bolsa (v2.5.3)

Aplicación Android desarrollada en Kotlin y Jetpack Compose para realizar sorteos inteligentes de parejas de pádel de forma equitativa, flexible y divertida.

## 🚀 Novedades en la Versión 2.5.3
- **Compartir como Imagen (Estilo Comprobante):** Al compartir los resultados de un sorteo, la app genera automáticamente una imagen PNG profesional con diseño de tarjeta oscura, detalles en verde fluo y colores distintivos por posición (*Drive* en celeste, *Revés* en rojo, *Ambos* en blanco), con un ancho adaptado dinámicamente al texto.
- **Evitar repetición de parejas:** El motor de sorteo analiza automáticamente el historial reciente (últimos 3 sorteos) e intenta no repetir parejas que ya hayan jugado juntas.
- **Nombres de respaldos dinámicos:** Los archivos de exportación de la base de datos se nombran automáticamente con el formato `MMddyy_HHmmhs_##_Mancos.json` (ej: fecha, hora y cantidad de jugadores).
- **Categorías ampliadas hasta C13** y soporte de configuración de Tiebreak con semántica optimizada para lectores de pantalla (TalkBack).

## ✨ Características Principales
- **Gestión de Jugadores:** Altas, bajas, modificación de nombres, posición (Drive `D`, Revés `R`, Ambos `X`) y categorías ampliadas (`C1` a `C13`).
- **Exclusiones:** Configuración de duplas prohibidas que el sistema respetará automáticamente al sortear.
- **Nivelación Automática / Sorteo Inteligente:** Algoritmo de optimización que busca equilibrar el nivel de las parejas y respetar las posiciones preferidas.
- **Historial de Sorteos:** Registro completo de los sorteos realizados en la base de datos local (Room).
- **Compartir Resultados:** Exportación instantánea en formato de imagen lista para enviar por WhatsApp.

## 🛠️ Stack Tecnológico
- **Kotlin** & **Jetpack Compose** (UI declarativa moderna)
- **Material 3**
- **Room Database** (Persistencia local con KSP)
- **Coroutines & Flow** (Gestión asincrónica reactiva)
- **Navigation Compose**
