package de.lemaik.chunky.denoiser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OidnBinaryDenoiser {
    public static final void denoise(
            String oidnPath,
            File image,
            File albedo,
            File normal,
            File output
    ) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(oidnPath);
        args.add("-ldr");
        args.add(image.getAbsolutePath());
        if (albedo != null) {
            args.add("-alb");
            args.add(albedo.getAbsolutePath());
        }
        if (normal != null) {
            args.add("-nrm");
            args.add(normal.getAbsolutePath());
        }
        args.add("-o");
        args.add(output.getAbsolutePath());

        new ProcessBuilder().command(args)
                .inheritIO()
                .start()
                .waitFor();
    }
}
