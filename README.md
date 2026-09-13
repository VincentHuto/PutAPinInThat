# PutAPinInThat

A client-side Forge 1.20.1 addon for JEI 15.49.0.191 that keeps up to four recipes visible on the left side of the HUD. Pinning a fifth recipe removes the oldest pin and appends the new one.

- Click the pin above JEI's recipe-bookmark button, or press `P` while hovering a recipe.
- Synthetic JEI categories without registry-backed recipes, including furnace fuel, can be pinned and restored too.
- Press `P` over a recipe bookmark to pin or unpin it.
- Use the pin button beside JEI's bottom-left bookmark controls to clear every pin.
- Pins are saved globally in `config/putapinthat/pins.json`.

Build with `gradlew build`. The output jar is written to `build/libs`.
