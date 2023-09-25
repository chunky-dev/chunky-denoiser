package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.TileBasedRenderer;
import se.llbit.chunky.renderer.scene.Camera;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;

public abstract class MultiPassRenderer extends TileBasedRenderer {
    protected int renderPass(DefaultRenderManager manager, int spp, int passSamples, RayTracer tracer, float[] buffer) throws InterruptedException {
        Scene scene = manager.bufferedScene;
        double[] sampleBuffer = scene.getSampleBuffer();
        int width = scene.width;
        int height = scene.height;

        Camera cam = scene.camera();
        double halfWidth = width / (2.0 * height);
        double invHeight = 1.0 / height;

        double sinv = 1.0 / (passSamples + spp);

        submitTiles(manager, (state, pixel) -> {
            int x = pixel.firstInt();
            int y = pixel.secondInt();

            double[] srgb = new double[3];

            for (int k = 0; k < passSamples; k++) {
                double ox = state.random.nextDouble();
                double oy = state.random.nextDouble();

                    cam.calcViewRay(state.ray, state.random,
                            -halfWidth + (x + ox) * invHeight,
                            -0.5 + (y + oy) * invHeight);

                    scene.rayTrace(tracer, state);
                    srgb[0] += state.ray.color.x;
                    srgb[1] += state.ray.color.y;
                    srgb[2] += state.ray.color.z;
            }

            int offset = 3 * (y*width + x);
            for (int i = 0; i < 3; i++) {
                if (buffer == null) {
                    sampleBuffer[offset + i] = (sampleBuffer[offset + i] * spp + srgb[i]) * sinv;
                } else {
                    buffer[offset + i] = (float) ((buffer[offset + i] * spp + srgb[i]) * sinv);
                }
            }
        });

        manager.pool.awaitEmpty();
        return spp + passSamples;
    }
}
