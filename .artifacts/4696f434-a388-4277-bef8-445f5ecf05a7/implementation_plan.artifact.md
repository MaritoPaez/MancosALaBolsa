# Reordenamiento Dinámico de Jugadores Seleccionados

Este plan detalla los cambios necesarios para que, en la pantalla principal, los jugadores seleccionados se muevan automáticamente a la parte superior de la lista, manteniendo el orden alfabético en ambos grupos (seleccionados y no seleccionados).

## Cambios Propuestos

### Componente: ViewModel

#### [MODIFY] [MancosViewModel.kt](file:///C:/Users/Marito/StudioProjects/MancosALaBolsa/app/src/main/java/com/mancosalabolsa/ui/viewmodel/MancosViewModel.kt)
- Añadir un nuevo `StateFlow` llamado `jugadoresOrdenados` que combine `jugadoresActivos` y `selectedPlayerIds`.
- La lógica de combinación dividirá la lista en dos grupos (seleccionados y no seleccionados), ordenará cada uno alfabéticamente por nombre y los unirá.

### Componente: UI (Pantalla de Inicio)

#### [MODIFY] [InicioScreen.kt](file:///C:/Users/Marito/StudioProjects/MancosALaBolsa/app/src/main/java/com/mancosalabolsa/ui/screen/InicioScreen.kt)
- Cambiar la fuente de datos de la lista de `jugadoresActivos` a `jugadoresOrdenados`.
- Proporcionar una clave estable (`key = { it.id }`) a la función `items` del `LazyColumn`.
- Añadir `Modifier.animateItem()` al `Row` que representa a cada jugador para que el movimiento sea fluido y no un salto repentino.

## Plan de Verificación

### Pruebas Manuales
1. Abrir la aplicación en la pantalla "A la Bolsa".
2. Seleccionar un jugador que esté en la parte inferior de la lista.
3. Verificar que el jugador se mueva hacia arriba (a la sección de seleccionados) con una animación suave.
4. Verificar que dentro de los seleccionados se mantenga el orden alfabético.
5. Deseleccionar un jugador y verificar que vuelva a la sección inferior manteniendo el orden alfabético de ese grupo.
6. Realizar un sorteo y verificar que la selección se mantiene correctamente (el diálogo de resultado debe mostrar los jugadores esperados).
