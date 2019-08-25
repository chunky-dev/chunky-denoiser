package de.lemaik.chunky.denoiser;

import java.io.File;
import java.io.IOException;

public class OidnBinaryDenoiser {
    public static final void denoise(
            String oidnPath,
            File image,
            File albedo,
            File normal,
            File output
    ) throws IOException, InterruptedException {
        new ProcessBuilder().command(oidnPath,
                "-ldr", image.getAbsolutePath(),
                "-alb", albedo.getAbsolutePath(),
                "-nrm", normal.getAbsolutePath(),
                "-o", output.getAbsolutePath())
                .inheritIO()
                .start()
                .waitFor();
    }
}
