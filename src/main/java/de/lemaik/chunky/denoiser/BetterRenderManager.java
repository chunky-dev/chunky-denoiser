package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.renderer.RenderContext;
import se.llbit.chunky.renderer.RenderManager;
import se.llbit.chunky.renderer.RenderMode;
import se.llbit.chunky.renderer.RenderStatusListener;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.resources.BitmapImage;
import se.llbit.png.PngFileWriter;
import se.llbit.util.TaskTracker;

import java.io.*;
import java.nio.ByteOrder;

public class BetterRenderManager extends RenderManager {
    private static final int ALBEDO_SPP = 100;
    private static final int NORMAL_SPP = 100;
    private boolean isFirst = true;

    public BetterRenderManager(RenderContext context, boolean headless, CombinedRayTracer rayTracer) {
        super(context, headless);
        this.addRenderListener(new RenderStatusListener() {
            int oldTargetSpp = 0;

            @Override
            public void setSpp(int spp) {
                if (!isFirst && spp >= getBufferedScene().getTargetSpp()) {
                    Scene scene = context.getChunky().getSceneManager().getScene();
                    if (rayTracer.getRayTracer() instanceof NormalTracer) {
                        writeNormalPfmImage(new File(context.getSceneDirectory(), scene.name + ".normal.pfm"));
                        rayTracer.setRayTracer(new AlbedoTracer());
                        scene.haltRender();
                        scene.setTargetSpp(ALBEDO_SPP);
                        scene.startRender();
                    } else if (rayTracer.getRayTracer() instanceof AlbedoTracer) {
                        writePfmImage(new File(context.getSceneDirectory(), scene.name + ".albedo.pfm"));
                        rayTracer.setRayTracer(new PathTracer());
                        scene.haltRender();
                        scene.setTargetSpp(oldTargetSpp);
                        scene.startRender();
                    } else if (rayTracer.getRayTracer() instanceof PathTracer) {
                        writePfmImage(new File(context.getSceneDirectory(), scene.name + ".pfm"));

                        String denoiserPath = PersistentSettings.settings.getString("oidnPath", null);
                        if (denoiserPath != null) {
                            File denoisedPfm = new File(context.getSceneDirectory(), scene.name + ".denoised.pfm");
                            try {
                                OidnBinaryDenoiser.denoise(denoiserPath,
                                        new File(context.getSceneDirectory(), scene.name + ".pfm"),
                                        new File(context.getSceneDirectory(), scene.name + ".albedo.pfm"),
                                        new File(context.getSceneDirectory(), scene.name + ".normal.pfm"),
                                        denoisedPfm
                                );
                                BitmapImage img = PortableFloatMap.readToRgbImage(new FileInputStream(denoisedPfm));
                                try (PngFileWriter pngWriter = new PngFileWriter(new File(context.getSceneDirectory(), scene.name + "-" + oldTargetSpp + ".denoised.png"))) {
                                    pngWriter.write(img.data, img.width, img.height, TaskTracker.Task.NONE);
                                }
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (isFirst) {
                    isFirst = false;
                    rayTracer.setRayTracer(new NormalTracer());
                    Scene scene = context.getChunky().getSceneManager().getScene();
                    scene.haltRender();
                    scene.setTargetSpp(NORMAL_SPP);
                    scene.startRender();
                }
            }

            @Override
            public void setRenderTime(long l) {
            }

            @Override
            public void setSamplesPerSecond(int i) {
            }

            @Override
            public void renderStateChanged(RenderMode renderMode) {
                if (renderMode == RenderMode.RENDERING) {
                    isFirst = true;
                    oldTargetSpp = context.getChunky().getSceneManager().getScene().getTargetSpp();
                }
            }
        });
    }

    private void writePfmImage(File file) {
        Scene scene = getBufferedScene();
        double[] samples = scene.getSampleBuffer();
        double[] pixels = new double[samples.length];

        for (int y = 0; y < scene.height; y++) {
            for (int x = 0; x < scene.width; x++) {
                double[] result = new double[3];
                scene.postProcessPixel(x, y, result);
                pixels[(y * scene.width + x) * 3] = Math.min(1.0, result[0]);
                pixels[(y * scene.width + x) * 3 + 1] = Math.min(1.0, result[1]);
                pixels[(y * scene.width + x) * 3 + 2] = Math.min(1.0, result[2]);
            }
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            PortableFloatMap.writeImage(pixels, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNormalPfmImage(File file) {
        Scene scene = getBufferedScene();
        double[] samples = scene.getSampleBuffer();
        double[] pixels = NormalTracer.MAP_POSITIVE ? new double[samples.length] : samples;
        if (NormalTracer.MAP_POSITIVE) {
            for (int i = 0; i < samples.length; i++) {
                pixels[i] = Math.min(1.0, samples[i]) * 2 - 1;
            }
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            PortableFloatMap.writeImage(pixels, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
