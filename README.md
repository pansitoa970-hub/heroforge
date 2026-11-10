# HeroForge (Fabric, Minecraft 26.3)

Mod con **8 héroes ORIGINALES** (no son personajes de DC/Marvel), cada uno con traje de 4 piezas, suero de poder y 2 habilidades con tecla.

## Cómo se juega
1. Mina **Mena de Heronita** (capas -60 a 20), fúndela en horno/alto horno -> **Lingote de Heronita**.
2. Crea la **Mesa de Forja Heroica** (2 lingotes + 2 tablones: `HH / PP`).
3. Craftea el **Suero** del héroe y úsalo (click derecho). El héroe queda guardado y sobrevive a la muerte.
4. Craftea el **traje** completo: sus **pasivas** solo funcionan con las 4 piezas puestas.
5. **R** = Poder 1, **G** = Poder 2 (cambiables en Controles > HeroForge).

| Héroe | Poder 1 (R) | Poder 2 (G) | Pasiva del traje | Ingrediente especial |
|---|---|---|---|---|
| Relámpago | Oleada de velocidad | Dash | Velocidad II + salto | Lingote de oro |
| Centinela Alado | Alternar vuelo | Impulso ascendente | Caída lenta | Pluma |
| Tejedor | Disparo de telaraña | Gancho (te lanza al bloque mirado) | Salto II + velocidad | Hilo |
| Bastión | Golpe sísmico (área) | Salto colosal | Fuerza + resistencia | Bloque de hierro |
| Espectro | Invisibilidad | Parpadeo (teletransporte) | Visión nocturna | Perla de ender |
| Ígnea | Bola de fuego | Anillo de llamas | Resistencia al fuego | Polvo de blaze |
| Glaciar | Nova de escarcha | Ráfaga de bolas de nieve | Respirar bajo el agua + gracia del delfín | Hielo compacto |
| Voltio | Rayo dirigido | Descarga en cadena | Prisa II | Bloque de redstone |

## Compilar
Requiere **JDK 25** y Gradle 9.8+ (o IntelliJ). Sin wrapper incluido:
```
gradle wrapper --gradle-version 9.8.2
./gradlew build        # el .jar queda en build/libs/
```
Valores de `gradle.properties` (loader, loom, fabric-api) verifícalos en https://fabricmc.net/develop

## Texturas
Todas generadas por `tools/gen_assets.py` (ejecútalo para regenerar). Son pixel-art procedural original; puedes reemplazar
cualquier PNG en `src/main/resources/assets/heroforge/textures/` por tus propios diseños.

## Pendiente (no incluido aún)
- Estructuras y enemigos propios (requieren modelos/renderers de entidad y NBT de estructura).
- Receta restringida a la Mesa de Forja (ahora las recetas también funcionan en la mesa normal; la Mesa de Forja usa el mismo 3x3).
- Este código NO se ha compilado: la API de 26.x cambia mucho y no pude descargar dependencias aquí.
