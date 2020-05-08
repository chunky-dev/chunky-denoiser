# Denoising Plugin

This is a plugin for [Chunky][chunky] that creates _Portable Float Map_ files (.pfm) for use with denoisers, e.g. [Intel Open Image Denoise][openimagedenoise]. The plugin was compiled for and tested with Chunky 1.4.5 as well as Chunky 2.0-beta6.

Please use `chunky-denoiser-chunky1.jar` for Chunky 1.x and `chunky-denoiser-chunky2.jar` for Chunky 2.x (i.e. all Chunky versions for Minecraft 1.13 or later).

## Installation
Download the latest plugin release for your Chunky version from the [releases page](https://github.com/leMaik/chunky-denoiser/releases). In the Chunky Launcher, click on _Manage plugins_ and then on _Add_ and select the `.jar` file you just downloaded. Click on `Save` to store the updated configuration, then start Chunky as usual.

## Usage
Just render a scene as usual. It will render three images and save them as _Portable Float Maps_.

### Denoise automatically
The Intel Open Image Denoiser can be downloaded [here][openimagedenoise-dl]. After unpacking the archive, you can configure the denoiser executable (`denoiser.exe` on Windows, `denoiser` on Linux) in the _Denoiser_ tab inside Chunky.

### Invoke the denoiser manually
After the rendering is done, the plugin will save the resulting image as `scene-name.pfm` in the scene directory and start to render a normal image (saved as `scene-name.normal.pfm`) and an Albedo image (`scene-name.albedo.pfm`). These files can be used by [Intel Open Image Denoise][openimagedenoise-dl] like this:

```
./denoise -ldr scene-name.pfm -alb scene-name.albedo.pfm -nrm scene-name.normal.pfm -o output.pfm
```

To view the resulting image, it needs to be converted back to an actual image file. This can be done by the `pfm2png.py` Python 3 script included in this repository or using an online converter, e.g. [this one][convertio].

## License

Copyright 2019-2020 Maik Marschner (leMaik)

Permission to modify and redistribute is granted under the terms of the GNU General Public License, Version 3. See the `LICENSE` file for the full license.

[chunky]: https://chunky.llbit.se/
[openimagedenoise]: https://openimagedenoise.github.io
[openimagedenoise-dl]: https://openimagedenoise.github.io/downloads.html
[convertio]: https://convertio.co/de/pfm-png/
