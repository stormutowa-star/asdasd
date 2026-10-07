# Blue-Eyes White Dragon — mod para Minecraft 1.21.1 (Forge)

- **Carta** "Dragón Blanco de Ojos Azules" (pestaña creativa propia, o receta: 4 diamantes + 4 papel + estrella del Nether en el centro).
- **Clic derecho** apuntando al suelo: la carta queda **boca arriba en el campo** y, tras el círculo de invocación, el **dragón aparece encima** con un rayo.
- El dragón ataca a quien te ataca, a quien tú atacas, a quien te persigue y a los hostiles cercanos con **White Lightning** (carga en las fauces + rayo blanco).
- Si te alejas de la carta, el dragón te sigue. **Clic derecho / golpe a la carta** (o agachado + clic derecho al dragón): la carta vuelve a tu mano.
- Si el dragón muere, la carta vuelve a ser un objeto en el campo.

Compilar: `gradlew build` (Java 21) → `build/libs/blueeyes-1.0.0.jar`. El workflow de GitHub *Compilar Blue-Eyes White Dragon* lo hace solo y deja el jar en `dist/`.

Modelo/textura del dragón: `python3 tools/build_assets.py` regenera `BlueEyesLayer.java` y las texturas.
