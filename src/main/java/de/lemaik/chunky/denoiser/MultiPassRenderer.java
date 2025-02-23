package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.TileBasedRenderer;
import se.llbit.chunky.renderer.scene.Camera;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;

public abstract class MultiPassRenderer extends TileBasedRenderer {
    protected void renderPass(DefaultRenderManager manager, int passSpp, RayTracer[] tracers, float[][] renderBuffers, boolean[] tracerMask) throws InterruptedException {
        Scene scene = manager.bufferedScene;
        double[] sampleBuffer = scene.getSampleBuffer();
        int width = scene.canvasConfig.getWidth();

        int fullWidth = scene.canvasConfig.getCropWidth();
        int fullHeight = scene.canvasConfig.getCropHeight();
        int cropX = scene.canvasConfig.getCropX();
        int cropY = scene.canvasConfig.getCropY();

        Camera cam = scene.camera();
        double halfWidth = fullWidth / (2.0 * fullHeight);
        double invHeight = 1.0 / fullHeight;

        int spp = scene.spp;
        double passinv = 1.0 / passSpp;
        double sinv = 1.0 / (passSpp + spp);

        submitTiles(manager, (state, pixel) -> {
            int sx = pixel.firstInt();
            int sy = pixel.secondInt();
            int x = sx + cropX;
            int y = sy + cropY;

            double[] srgb = new double[tracers.length * 3];

            for (int k = 0; k < passSpp; k++) {
                double ox = state.random.nextDouble();
                double oy = state.random.nextDouble();

                for (int i = 0; i < tracers.length; i++) {
                    if (tracerMask[i]) {
                        cam.calcViewRay(state.ray, state.random,
                                -halfWidth + (x + ox) * invHeight,
                                -0.5 + (y + oy) * invHeight);

                        scene.rayTrace(tracers[i], state);
                        srgb[i * 3 + 0] += state.ray.color.x;
                        srgb[i * 3 + 1] += state.ray.color.y;
                        srgb[i * 3 + 2] += state.ray.color.z;
                    }
                }
            }

            int offset = 3 * (sy*width + sx);
            for (int i = 0; i < tracers.length; i++) {
                if (tracerMask[i]) {
                    float[] buffer = renderBuffers[i];
                    double r = srgb[i * 3 + 0] * passinv;
                    double g = srgb[i * 3 + 1] * passinv;
                    double b = srgb[i * 3 + 2] * passinv;

                    if (buffer == null) {
                        sampleBuffer[offset + 0] = (sampleBuffer[offset + 0] * spp + r) * sinv;
                        sampleBuffer[offset + 1] = (sampleBuffer[offset + 1] * spp + g) * sinv;
                        sampleBuffer[offset + 2] = (sampleBuffer[offset + 2] * spp + b) * sinv;
                    } else {
                        buffer[offset + 0] = (float) ((buffer[offset + 0] * spp + r) * sinv);
                        buffer[offset + 1] = (float) ((buffer[offset + 1] * spp + g) * sinv);
                        buffer[offset + 2] = (float) ((buffer[offset + 2] * spp + b) * sinv);
                    }
                }
            }
        });

        manager.pool.awaitEmpty();
        scene.spp += passSpp;
    }
}
