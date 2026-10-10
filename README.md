![Mod-Logo](./docs/logo.png)

# Liquid Package

## Overview

Installed mod with some greate fuel but oh no they are liquid.
This mod will help you to compress them into solid items.

Mod adds Sublimator block and Empty Shell item. Configure you mod, pour some fluids in Sublimator, add Empty Shell and let magic do the thing. After sublimation process you'll get solid packed fuel.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/)
2. Install matching [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `liquid-package-*.jar` into your `mods/` (both server and client)
4. Launch the game or server once to generate the default config at
   `config\liquid-package.json`

It is important to have the same configuration on client and server

## Configuration

On first run, Liquid Package writes a default config to:

```
config\liquid-package.json
```

With the next content:

```
{
  "emptyShellFuelTime": 200,
  "shells": [
    {
      "liquidId": "minecraft:water",
      "liquidPerPackage": 15,
      "fuelTime": 0
    },
    {
      "liquidId": "minecraft:lava",
      "liquidPerPackage": 1,
      "fuelTime": 20000
    }
  ]
}
```
### Configuration guide:

Empty Shell also can be used as fuel, time of it's burning specified in `emptyShellFuelTime`

Configure `shells` items to support fluids you need according to the next table

| Option | Value | Description |
|---|---|---|
| emptyShellFuelTime | Integer | Burning time of empty shell (in ticks, 20 ticks is 1 second). Defaults to coal |
| shells[0].liquidId | String | Makes fluid avalable for packaging |
| shells[0].liquidPerPackage | Integer | Specify how many buckets need to create one shell |
| shells[0].fuelTime | Integer | Burning time of filled shell (in ticks, 20 ticks is 1 second) |

## Usage in game
1. Craft Sublimator block and place it
2. Add something hot 1 block below (like fire, lava or magma cube)
3. Pour some fluid using bucket or pipe (on top)
4. Insert Empty Shell (or transport them from left or right)
5. If Sublimator is hot enough it will start sublimation process
6. When the process ends, you will get packed fuel (transportable from back side)

## License

This project is licensed under the [MIT License](LICENSE).
