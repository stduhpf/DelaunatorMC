# Delaunator

Welcome to the Delaunator project! This project is currently a work in progress, and as such, it may be unstable.

## About the Project

This mod is intended to be used to convert images into map art. It can output directly a map in the [Minecraft NBT format](https://minecraft.gamepedia.com/NBT), but it can also generate a .litematic file (that can be opened with the [Litematica](https://github.com/maruohon/litematica) mod) describing the block placements required to build the map, or place the blocks directly in the world (assuming the player have `/setblock` rights).

This is related to my older [delaunator](https://github.com/stduhpf/delaunator) python project.

## Installation

### Requirements

- Minecraft  1.21.1
- [Fabric](https://fabricmc.net/)
- [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

### Download

You can download the latest build from the Artifacts on the [lastest workflow page](${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}). Just Click on the "Artifacs" link, and it will download the latest build. You need to be logged in to GitHub to download the artifacts.

### Installation.

Once you've downloaded `Artifacts.zip`, just extract it and copy `delaunator-1.0.0.jar` to your `mods` folder.

## Usage

(Note that this is subject to change and this section could be out of to date)

- When in game, press the `B` key to open the GUI. Then, if your `images` folder is empty, you can press `open img folder` to open the folder in your OS file manager, where you can paste the images you want to convert.
- Once it's done, exit the GUI and open it again, and you'll see the preview of your images on the left you can navigate through.
- Click `Chose` once you've selected the image you want to use.
- You can resize the image to fit the map with the controls on the right side.
- Chose the palette type you want to with the `Type` button. (`Flat`: map can be built with all blocks on the same y height, `Staircase`: map uses elevation changes to allow for more shades. `Full`: uses shades that are impossible to obtain in vanilla Minecraft (so no litematic nor placing blocks, only map items))
- Press `Convert` to apply the palette to the image.
- Chose the `Dither` type (I recommend to stick with `IGN`), and change the strength of the dithering with the slider util you're happy with the result.
- Then you can either `Print to map item`, `Save as schematic`, or `Place into world`(not recommended if you care about the world you're in, this needs a rework)

## How it works

Uses a [Delaunay tetrahedrisation](https://en.wikipedia.org/wiki/Delaunay_triangulation#d-dimensional_Delaunay) of the Minecraft map color pallette in a linear color space (YCoCg for now). 
Each pixel in the original image is mapped to that color space, and it will land inside a single tetrahedral cell. The tetrahedral cell is defined by 4 map colors that can be combined in a way to reproduce the original color (using the barycentric coordinates of the pixel inside the cell).
Then we use dithering to chose which color to use for each pixel, according to the coordinates of the pixel inside the cell.
(TODO: clarify)

## Building the Project

### Requirements

- Java 21
- Maven 3.8.4
- Gradle 7.3.3

### Build

To build the project, just run `gradlew build`.

## Contributing

I'll welcome contributions to this project! If you find any issues or have suggestions for improvements, please feel free to open an issue or submit a pull request.

## Disclaimer

Please note that this project is a work in progress, and as such, it may be unstable. I appreciate your understanding and patience as I continue to develop and improve this project.

## License

This project is licensed under the [MIT License](LICENSE).