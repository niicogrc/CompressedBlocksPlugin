# CompressedBlocks

A Paper plugin for Minecraft **26.1.2** that lets players compress stacks of blocks/items into denser, tiered versions using vanilla crafting — and decompress them back.

## What it does

- **Compress**: put 9 of a configured material in a crafting grid (3x3) to get 1 *Compressed* version of it.
- **Stack tiers**: 9 *Compressed* items craft into 1 *Double Compressed*, then *Triple Compressed*, *Quadruple Compressed*, and so on, up to a configurable number of tiers.
- **Decompress**: place a single compressed item alone in the crafting grid to get back 9 of the previous tier.
- **World-safe**: compressed blocks can be placed and mined without losing their identity — the plugin tracks placed compressed blocks per-chunk using persistent data, so breaking one drops the correct compressed item back (not the raw base material).
- **Configurable**: choose which materials are compressible and how many tiers to generate, via `config.yml`.
- **No resource pack required**: compressed items reuse their base material's texture but are distinguished by name, lore, and `CustomModelData` (so you can hook up a resource pack later if you want unique textures).

### Example

With the default config, for `STONE`:

```
9x Stone                     -> 1x Compressed Stone
9x Compressed Stone          -> 1x Double Compressed Stone
9x Double Compressed Stone   -> 1x Triple Compressed Stone
9x Triple Compressed Stone   -> 1x Quadruple Compressed Stone
```

Each step is reversible by placing a single item of that tier alone in the crafting grid.

## Configuration

`src/main/resources/config.yml`:

```yaml
# Materials that can be compressed. Must be valid Bukkit Material enum names.
compressible-materials:
  - STONE
  - COBBLESTONE
  - IRON_INGOT
  - GOLD_INGOT
  - DIAMOND
  - EMERALD
  - NETHERITE_INGOT

# How many compression tiers to generate above the base material.
max-tier: 4
```

Add or remove materials freely (any valid `org.bukkit.Material` name works). Reload changes in-game without restarting the server:

```
/compressedblocks reload
```

(alias: `/cb`, requires the `compressedblocks.admin` permission, granted to server operators by default)

## Requirements

- **Minecraft / Paper 26.1.2**
- **Java 25** (required by Paper 26.1.x)

## Building from source

The project ships with the Gradle wrapper, so no local Gradle install is needed — only a JDK 25.

```bash
./gradlew build
```

The compiled plugin jar will be at `build/libs/CompressedBlocks-1.0.0.jar`.

> Gradle itself needs a JVM 17+ to run. If your only local JDK is older, install a JDK 25 (e.g. [Eclipse Temurin](https://adoptium.net/)) and point `JAVA_HOME` at it before building — the project's `settings.gradle.kts` also declares the Foojay toolchain resolver, so Gradle can auto-provision a JDK 25 toolchain for compilation on its own.

## Installation

1. Download or build `CompressedBlocks-1.0.0.jar`.
2. Drop it into your server's `plugins/` folder.
3. Start (or restart) the server — a Paper 26.1.2 server running Java 25.

## How it works internally

- Recipes are registered as vanilla `ShapelessRecipe`s (`Bukkit.addRecipe`) — no custom GUI or crafting table replacement.
- Each compressed item stores its base material and tier in its `PersistentDataContainer`, which is how the plugin recognizes it as an ingredient and reconstructs it when needed.
- When a compressed item is placed as a block, its material/tier is recorded in the containing **chunk's** `PersistentDataContainer`, keyed by block coordinates. When that block is broken, the plugin looks up the record, cancels the default drop, and drops the correctly-tiered item instead — then clears the record.

## License

No license specified — all rights reserved by the author unless stated otherwise.
