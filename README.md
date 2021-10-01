# Denoising Plugin

This is a plugin for [Chunky][chunky] that creates _Portable Float Map_ files (.pfm) for use with denoisers, e.g. [Intel Open Image Denoise][openimagedenoise].

Please use [version 0.3.2](https://github.com/chunky-dev/chunky-denoiser/releases/tag/v0.3.2) for Chunky 1.x and the [latest version](https://github.com/chunky-dev/chunky-denoiser/releases/latest) for Chunky 2.4.0 or later.

## Installation

Download the latest plugin release for your Chunky version from the [releases page](https://github.com/leMaik/chunky-denoiser/releases). In the Chunky Launcher, click on _Manage plugins_ and then on _Add_ and select the `.jar` file you just downloaded. Click on `Save` to store the updated configuration, then start Chunky as usual.

**Compatibility note:** If you are using the [Discord plugin](https://github.com/leMaik/chunky-discord), make sure that it is loaded _after_ the Denoising plugin, i.e. use the _Down_ button to move it below it in the plugin list. Otherwise the denoiser plugin will not work.

## Usage

Just render a scene as usual. It will render three images and save them as _Portable Float Maps_.

### Denoise automatically

The Intel Open Image Denoiser can be downloaded [here][openimagedenoise-dl]. After unpacking the archive, you can configure the denoiser executable (`denoiser.exe` on Windows, `denoiser` on Linux) in the _Denoiser_ tab inside Chunky. If you do this, it will output the denoised image alongside the original image in the scene's snapshots directory.

### Invoke the denoiser manually

After the rendering is done, the plugin will save the resulting image as `scene-name.pfm` in the scene directory and start to render a normal image (saved as `scene-name.normal.pfm`) and an Albedo image (`scene-name.albedo.pfm`). These files can be used by [Intel Open Image Denoise][openimagedenoise-dl] like this:

```
./denoise -ldr scene-name.pfm -alb scene-name.albedo.pfm -nrm scene-name.normal.pfm -o output.pfm
```

To view the resulting image, it needs to be converted back to an actual image file. This can be done by the `pfm2png.py` Python 3 script included in this repository or using an online converter, e.g. [this one][convertio].

## License

Copyright 2019-2021 Maik Marschner (leMaik)

Permission to modify and redistribute is granted under the terms of the GNU General Public License, Version 3. See the `LICENSE` file for the full license.

[chunky]: https://chunky.llbit.se/
[openimagedenoise]: https://openimagedenoise.github.io
[openimagedenoise-dl]: https://openimagedenoise.github.io/downloads.html
[convertio]: https://convertio.co/de/pfm-png/
