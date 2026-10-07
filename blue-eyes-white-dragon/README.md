# Blue-Eyes White Dragon — mod para Minecraft 1.21.1 (Forge)

- **Carta** "Dragón Blanco de Ojos Azules" (pestaña creativa propia, o receta: 4 diamantes + 4 papel + estrella del Nether en el centro).
- **Clic derecho** apuntando al suelo: la carta queda **boca arriba en el campo** y, tras el círculo de invocación, el **dragón aparece encima** con un rayo.
- El dragón ataca a quien te ataca, a quien tú atacas, a quien te persigue y a los hostiles cercanos con **White Lightning** (carga en las fauces + rayo blanco).
- Si te alejas de la carta, el dragón te sigue.
- **Desactivar:** clic derecho a la carta del campo, agachado + clic derecho al dragón, o escribe `desactivar` / `regresa` en el chat → el dragón se encoge, vuelve a la carta convertido en luz y la carta se voltea **boca abajo**.
- **Activar otra vez:** clic derecho a la carta boca abajo, o escribe `ojos azules` / `invoco` / `activar` en el chat (3 s de espera tras desactivar).
- **Recoger:** agachado + clic derecho a la carta (o golpearla) → vuelve a tu mano.
- Si el dragón es destruido, la carta queda boca abajo y no puede activarse durante 30 s.

Compilar: `gradlew build` (Java 21) → `build/libs/blueeyes-1.0.0.jar`. El workflow de GitHub *Compilar Blue-Eyes White Dragon* lo hace solo y deja el jar en `dist/`.

Modelo/textura del dragón: `python3 tools/build_assets.py` regenera `BlueEyesLayer.java` y las texturas.
