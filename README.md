# Denoising Plugin

**Note: This README is for an development version of the plugin that is not yet released. Click [here](https://github.com/chunky-dev/chunky-denoiser/blob/4d4db51a4a81f77c24cc1def717686c9df67a95d/README.md) for the README for the current version of the plugin, which is 0.4.0.**

This is a plugin for [Chunky][chunky] that creates _Portable Float Map_ files (.pfm) for use with denoisers, e.g. [Intel Open Image Denoise][openimagedenoise].

Please use [version 0.3.2](https://github.com/chunky-dev/chunky-denoiser/releases/tag/v0.3.2) for Chunky 1.x and the [latest version](https://github.com/chunky-dev/chunky-denoiser/releases/latest) for Chunky 2.4.0 or later.

## Installation

Download the latest plugin release for your Chunky version from the [releases page](https://github.com/leMaik/chunky-denoiser/releases). In the Chunky Launcher, click on _Manage plugins_ and then on _Add_ and select the `.jar` file you just downloaded. Click on `Save` to store the updated configuration, then start Chunky as usual.

Download the Intel Open Image Denoiser [here][openimagedenoise-dl]. After unpacking the archive in a safe location, you can configure the denoiser executable (`denoiser.exe` on Windows, `denoiser` on Linux) in the `Denoiser` tab inside Chunky.

## Usage

Select the `DenoisedPathTracer` in the `Advanced` tab:

![image](https://user-images.githubusercontent.com/42661490/147403029-54d291c2-8142-4a36-b6ea-4485156f9484.png)

Then render the scene as usual. It will automatically render all passes and denoise the final image.

### Denoising an Existing Render

Existing renders can be denoised by clicking on the `Denoise Current Render` button in the `Denoiser` tab:

![image](https://user-images.githubusercontent.com/42661490/147403139-67f3661c-1575-407f-af05-1d8780f68c73.png)

**WARNING: this will overwrite your existing render.**

It will automatically render all passes and denoise the final image.

### Denoising Outside Chunky

By checking `Save albedo map` and `Save normal map`, the denoised renderers will automatically save the albedo and normal maps as `.pfm` files inside the scene directory.

![image](https://user-images.githubusercontent.com/42661490/147403108-78aa1b33-5549-46de-8194-3f33d2e799a0.png)

These files can be used by [Intel Open Image Denoise][openimagedenoise-dl] like this:

```
./denoise -ldr scene-name.pfm -alb scene-name.albedo.pfm -nrm scene-name.normal.pfm -o output.pfm
```

# Development

It is recommended to use [IntelliJ](https://www.jetbrains.com/idea/). Install the Java17 JDK ([Temurin](https://adoptium.net/) is the recommended distribution).
Then, [clone](https://www.jetbrains.com/help/idea/set-up-a-git-repository.html#clone-repo) the Chunky repository and let IntelliJ index the project.
Navigate to `src/main/java/de/lemaik/chunky/denoiser/DenoiserPlugin` and click the green play button next to `public class DenoiserPlugin implements Plugin {` to build and run the denoiser plugin.

To build the plugin externally, run the `gradlew` script in the project root directory. Gradle is setup with a few main tasks:

* `gradlew pluginJar` - Build the denoiser plugin Jar
* `gradlew clean` - Cleans the project. Removes old builds.

# License

Copyright 2019-2021 Maik Marschner (leMaik)

Permission to modify and redistribute is granted under the terms of the GNU General Public License, Version 3. See the `LICENSE` file for the full license.

[chunky]: https://chunky.llbit.se/
[openimagedenoise]: https://openimagedenoise.github.io
[openimagedenoise-dl]: https://openimagedenoise.github.io/downloads.html
[convertio]: https://convertio.co/de/pfm-png/
