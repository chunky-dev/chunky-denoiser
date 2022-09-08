package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.PersistentSettings;
import se.llbit.log.Log;

import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class OidnBinaryDenoiser implements Denoiser {
    private String oidnPath;

    public OidnBinaryDenoiser() {
        loadPath();
    }

    public void setOidnPath(String oidnPath) {
        this.oidnPath = oidnPath;
    }

    public void loadPath() {
        setOidnPath(PersistentSettings.settings.getString("oidnPath", ""));
    }

    @Override
    public void init() {
        this.loadPath();
    }

    @Override
    public float[] denoise(int width, int height, float[] beauty, float[] albedo, float[] normal)
            throws DenoisingFailedException{
        File beautyFile;
        File outputFile;
        File albedoFile = null;
        File normalFile = null;
        try {
            beautyFile = File.createTempFile("chunky-denoiser", ".beauty.pfm");
            beautyFile.deleteOnExit();
            outputFile = File.createTempFile("chunky-denoiser", ".denoised.pfm");
            outputFile.deleteOnExit();
            if (albedo != null) {
                albedoFile = File.createTempFile("chunky-denoiser", ".albedo.pfm");
                albedoFile.deleteOnExit();
            }
            if (normal != null) {
                normalFile = File.createTempFile("chunky-denoiser", ".normal.pfm");
                normalFile.deleteOnExit();
            }
        } catch (IOException e) {
            throw new DenoisingFailedException("Failed to create temporary files.", e);
        }

        try {
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(beautyFile))) {
                PortableFloatMap.writeImage(beauty, width, height, ByteOrder.LITTLE_ENDIAN, os);
            }
            if (albedo != null) {
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(albedoFile))) {
                    PortableFloatMap.writeImage(albedo, width, height, ByteOrder.LITTLE_ENDIAN, os);
                }
            }
            if (normal != null) {
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(normalFile))) {
                    PortableFloatMap.writeImage(normal, width, height, ByteOrder.LITTLE_ENDIAN, os);
                }
            }
            callDenoise(oidnPath, beautyFile, albedoFile, normalFile, outputFile);

            try (InputStream is = new BufferedInputStream(new FileInputStream(outputFile))) {
                return PortableFloatMap.readImage(is);
            }
        } catch (IOException | InterruptedException e) {
            throw new DenoisingFailedException(e);
        } finally {
            if (!beautyFile.delete()) Log.warnf("Could not delete file: %s", beautyFile.getAbsolutePath());
            if (!outputFile.delete()) Log.warnf("Could not delete file: %s", outputFile.getAbsolutePath());
            if (albedoFile != null && !albedoFile.delete()) Log.warnf("Could not delete file: %s", albedoFile.getAbsolutePath());
            if (normalFile != null && !normalFile.delete()) Log.warnf("Could not delete file: %s", normalFile.getAbsolutePath());
        }
    }

    private static void callDenoise(String oidnPath, File beauty, File albedo, File normal, File output)
            throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(oidnPath);

        args.add("-ldr");
        args.add(beauty.getAbsolutePath());

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
