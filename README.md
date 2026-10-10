# CleanHud

A client-side Fabric mod for Minecraft 1.21.11 that adds a fully customizable, modern armor HUD.

## Features

- Sleek armor HUD with multiple themes (Minimal, Glass, Feather, Lunar, Outline)
- Interactive editor with live preview
- Drag-and-drop repositioning anywhere on screen
- Rotation, scale, spacing, and alignment controls
- Durability display modes: percentage, numeric, or hidden
- Optional durability progress bars
- Hide vanilla armor bar option

## Usage

1. Install Fabric Loader 0.19.3+ and Fabric API for 1.21.11
2. Place the mod jar in your `mods` folder
3. In-game, press **H** to open the Armor HUD Editor
4. Adjust settings in the left panel and preview changes on the right
5. Click **Move HUD** to drag the HUD anywhere on screen
6. Click **Done** to save

Config is saved to `config/cleanhud.json`.

## Development

```bash
./gradlew build
./gradlew runClient
```
