package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.renderer.RenderContext;
import se.llbit.chunky.renderer.RenderManager;
import se.llbit.chunky.renderer.RenderMode;
import se.llbit.chunky.renderer.RenderStatusListener;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.renderer.scene.Scene;

import java.io.*;
import java.nio.ByteOrder;

public class BetterRenderManager extends RenderManager {
    public BetterRenderManager(RenderContext context, boolean headless, CombinedRayTracer rayTracer) {
        super(context, headless);
        this.addRenderListener(new RenderStatusListener() {
            int oldTargetSpp = 0;

            @Override
            public void setSpp(int spp) {
                if (spp >= getBufferedScene().getTargetSpp()) {
                    Scene scene = context.getChunky().getSceneManager().getScene();
                    if (rayTracer.getRayTracer() instanceof PathTracer) {
                        oldTargetSpp = scene.getTargetSpp();
                        writePfmImage(new File(context.getSceneDirectory(), scene.name + ".pfm"), true);
                        rayTracer.setRayTracer(new NormalTracer());
                        scene.haltRender();
                        scene.setTargetSpp(1);
                        scene.startRender();
                    } else if (rayTracer.getRayTracer() instanceof NormalTracer) {
                        writePfmImage(new File(context.getSceneDirectory(), scene.name + ".normal.pfm"), false);
                        rayTracer.setRayTracer(new AlbedoTracer());
                        scene.haltRender();
                        scene.setTargetSpp(1);
                        scene.startRender();
                    } else if (rayTracer.getRayTracer() instanceof AlbedoTracer) {
                        writePfmImage(new File(context.getSceneDirectory(), scene.name + ".albedo.pfm"), true);
                        rayTracer.setRayTracer(new PathTracer());
                        scene.setTargetSpp(oldTargetSpp);
                        scene.haltRender();
                    }
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
            }
        });
    }

    private void writePfmImage(File file, boolean applyToneMap) {
        Scene scene = getBufferedScene();
        double[] samples = scene.getSampleBuffer();
        double[] pixels = new double[samples.length];

        if (applyToneMap) {
            for (int y = 0; y < scene.height; y++) {
                for (int x = 0; x < scene.width; x++) {
                    double[] result = new double[3];
                    scene.postProcessPixel(x, y, result);
                    pixels[(y * scene.width + x) * 3] = Math.min(1.0, result[0]);
                    pixels[(y * scene.width + x) * 3 + 1] = Math.min(1.0, result[1]);
                    pixels[(y * scene.width + x) * 3 + 2] = Math.min(1.0, result[2]);
                }
            }
        } else {
            for (int i = 0; i < samples.length; i++) {
                pixels[i] = Math.min(1.0, samples[i]);
            }
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            PortableFloatMap.writeImage(pixels, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
