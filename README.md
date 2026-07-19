# Multi Crosshair

A mod made by Nikita9724.

A Fabric mod for Minecraft 1.20.1 that renders as many crosshairs as you want
on screen, arranged in a smooth spiral pattern around the center. Everything
is controlled live with chat commands — no restart needed.

## Commands

All commands are **client-side** (they work in singleplayer and on any
server, even without the mod installed server-side).

| Command | Effect |
|---|---|
| `/crosshair count <0-2000>` | How many crosshairs to draw |
| `/crosshair range <0-2000>` | Radius (pixels) of the area they spread across |
| `/crosshair density <0.05-10>` | How they're distributed within that range: `1` = even spread, `>1` = clustered toward center, `<1` = pushed out toward the edge |
| `/crosshair size <1-64>` | Length of each crosshair's arms (pixels) |
| `/crosshair thickness <1-16>` | Thickness of each crosshair's arms (pixels) |
| `/crosshair color <hex>` | Color, e.g. `FFFFFF` (white) or `FF00FF00` (opaque green, ARGB) |
| `/crosshair reset` | Restore all defaults |
| `/crosshair info` | Show current settings |

Try it out:
```
/crosshair count 50
/crosshair range 120
/crosshair density 0.6
```
This scatters 50 crosshairs evenly across a 120px-radius ring pushed toward
the outer edge. Bump density up past 1 to pull them back toward the center
into a tight cluster.

Settings persist across game restarts in `config/crosshairmod.properties`.

## How positioning works

Positions are generated with a golden-angle spiral (the same "sunflower
seed" pattern used to distribute points evenly in a circle):

```
theta_i = i * 137.5°
r_i     = range * (i / count) ^ (1 / density)
```

This keeps the layout looking natural and evenly spaced at any count, rather
than a rigid grid.

## Building

Requires JDK 17+ and an internet connection (Gradle needs to download
Minecraft, mappings, and Fabric API on first run).

```bash
./gradlew build
```
(On first use, generate the wrapper with `gradle wrapper --gradle-version 8.8`
if `gradlew` isn't present, or just open the project in IntelliJ IDEA with the
Fabric plugin — it will bootstrap Loom automatically.)

The built mod jar will be in `build/libs/crosshairmod-1.0.0.jar`.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1.
2. Download/build [Fabric API](https://modrinth.com/mod/fabric-api) for 1.20.1
   and drop it in your `mods` folder.
3. Drop `crosshairmod-1.0.0.jar` in the same `mods` folder.
4. Launch the Fabric profile and type `/crosshair count 20` in-game.

## Project layout

```
src/main/java/com/example/crosshairmod/
  CrosshairMod.java              common entrypoint
  config/CrosshairConfig.java    settings + persistence
src/client/java/com/example/crosshairmod/
  client/CrosshairModClient.java client entrypoint (registers everything)
  client/CrosshairHudRenderer.java HUD drawing logic
  command/CrosshairCommand.java  /crosshair command tree
```
