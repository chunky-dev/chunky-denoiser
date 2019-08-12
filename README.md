# Denoising Plugin

This is a plugin for [Chunky][chunky] that creates _Portable Float Map_ files (.pfm) for use with denoisers, e.g. [Intel Open Image Denoise][openimagedenoise]. The plugin was compiled for and tested with Chunky 2.0-beta6.

## Usage
Just render a scene as usual. After the rendering is done, this plugin will save the resulting image as `scene-name.pfm` in the scene directory and start to render a normal image (saved as `scene-name.normal.pfm`) and an Albedo image (`scene-name.albedo.pfm`).

These files can be used by the [Intel Open Image Denoise][openimagedenoise] like this:

```
./denoise -ldr scene-name.pfm -alb scene-name.albedo.pfm -nrm scene-name.normal.pfm -o output.pfm
```

To view the resulting image, it needs to be converted back to an actual image file. This can be done by the `pfm2png.py` Python 3 script included in this repository or using an online converter, e.g. [this one][convertio].

## License

Copyright 2019 Maik Marschner (leMaik)

Permission to modify and redistribute is granted under the terms of the GNU General Public License, Version 3. See the `LICENSE` file for the full license.

[chunky]: https://chunky.llbit.se/
[openimagedenoise]: https://openimagedenoise.github.io
[convertio]: https://convertio.co/de/pfm-png/
