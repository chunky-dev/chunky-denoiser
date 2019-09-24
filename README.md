# Denoising Plugin

This is a plugin for [Chunky][chunky] that creates _Portable Float Map_ files (.pfm) for use with denoisers, e.g. [Intel Open Image Denoise][openimagedenoise]. The plugin was compiled for and tested with Chunky 1.4.5 as well as Chunky 2.0-beta6.

## Installation
Download the latest plugin release for your Chunky version from the [releases page](https://github.com/leMaik/chunky-denoiser/releases) and put it into `%APPDATA%\.chunky\plugins` (Windows) or `~/.chunky/plugins` (Linux). If that directory doesn't exist yet, create it.

## Usage
Just render a scene as usual. It will render three images and save them as _Portable Float Maps_.

### Denoise automatically
Edit the `chunky.json` in `%APPDATA%\.chunky` (Windows) or `~/.chunky` (Linux) and add a `oidnPath` key with the value being the `denoise` executable that can be downloaded [here][openimagedenoise-dl]. Once configured, the denoised image will be saved along the normal output image as `.denoised.png`.

```json
{
    "minecraftDir": "C:\\Users\\you\\.minecraft",
    ...
    "oidnPath": "C:\\path\\to\\oidn\\denoise.exe"
}
```

**Important:** Ensure that you don't break the JSON syntax, e.g. the last line before the `}` should not end with `,`. You can validate the syntax [here](https://jsonlint.com/).

### Invoke the denoiser manually
After the rendering is done, this plugin will save the resulting image as `scene-name.pfm` in the scene directory and start to render a normal image (saved as `scene-name.normal.pfm`) and an Albedo image (`scene-name.albedo.pfm`). These files can be used by [Intel Open Image Denoise][openimagedenoise-dl] like this:

```
./denoise -ldr scene-name.pfm -alb scene-name.albedo.pfm -nrm scene-name.normal.pfm -o output.pfm
```

To view the resulting image, it needs to be converted back to an actual image file. This can be done by the `pfm2png.py` Python 3 script included in this repository or using an online converter, e.g. [this one][convertio].

## License

Copyright 2019 Maik Marschner (leMaik)

Permission to modify and redistribute is granted under the terms of the GNU General Public License, Version 3. See the `LICENSE` file for the full license.

[chunky]: https://chunky.llbit.se/
[openimagedenoise]: https://openimagedenoise.github.io
[openimagedenoise-dl]: https://openimagedenoise.github.io/downloads.html
[convertio]: https://convertio.co/de/pfm-png/
