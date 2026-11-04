# Walkthrough - Mancos a la Bolsa v2.5.3

Resumen de las características y mejoras implementadas en las versiones recientes de la aplicación.

## 🌟 Funcionalidades Principales (v2.5.3)

### 1. 🖼️ Compartir Resultados como Imagen (Tarjeta tipo comprobante)
- **Implementación:** Al presionar el botón **Compartir** en el diálogo de resultados del sorteo, se genera dinámicamente un archivo PNG (`sorteo_mancos.png`) utilizando `Canvas` y `TextPaint`.
- **Diseño Visual:** Fondo oscuro (`#2E2E2E`), tarjeta central verde oscuro (`#0D3311`) con borde verde fluo (`#D4F05C`), y líneas formateadas con colores distintivos por posición (*Drive* celeste, *Revés* rojo, *Ambos* blanco, números verde fluo).
- **Ancho Dinámico:** El ancho de la imagen se calcula midiendo el texto real de forma dinámica (acotado entre 720px y 1080px), evitando espacios vacíos innecesarios en formato horizontal.
- **Compartir Seguro:** Utiliza `FileProvider` con `content://` URIs para enviar la imagen junto con el texto descriptivo por WhatsApp u otras apps.

### 2. 🛡️ Motor de Sorteo Inteligente con Anti-Repetición
- **Historial Reciente:** El algoritmo analiza los últimos 3 sorteos registrados en la base de datos local (Room).
- **Penalización Suave:** Si una pareja ya jugó junta recientemente, se aplica una penalización en el puntaje de desvío del sorteo para evitar repeticiones innecesarias, manteniendo flexibilidad si hay pocas opciones disponibles.

### 3. 📂 Respaldo y Exportación Inteligente
- **Nombres Automáticos:** Las copias de seguridad de la base de datos se exportan con el formato de nombre `MMddyy_HHmmhs_##_Mancos.json` (fecha, hora, cantidad de jugadores y sufijo del proyecto).

### 4. 🎚️ Categorías y Accesibilidad
- **Categorías Ampliadas:** Selector de categorías extendido de C1 hasta **C13**.
- **Accesibilidad TalkBack:** Configuración de semántica fonética para pronunciar correctamente términos como "Taibrek".

---

## 🧪 Pruebas y Verificación
- Compilación y pruebas locales exitosas con Gradle (`assembleRelease` / `assembleDebug`).
- Versión actual: **v2.5.3** (`versionCode = 77`).
- Release y APK (`MancosALaBolsa-v2.5.3.apk`) publicados y adjuntados exitosamente en GitHub.
