package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.TileBasedRenderer;
import se.llbit.chunky.renderer.export.PfmExportFormat;
import se.llbit.chunky.renderer.export.PictureExportFormat;
import se.llbit.chunky.renderer.export.PictureExportFormats;
import se.llbit.chunky.renderer.scene.Camera;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;
import se.llbit.util.TaskTracker;

import java.io.*;
import java.util.Arrays;

public class DenoisedPathTracer extends TileBasedRenderer {
    protected final String id;
    protected final String name;
    protected final String description;
    protected RayTracer tracer;

    protected static final RayTracer albedoTracer = new AlbedoTracer();
    protected static final RayTracer normalTracer = new NormalTracer();

    public static boolean enableAlbedo = true;
    public static int albedoSpp = 16;
    public static boolean enableNormal = true;
    public static int normalSpp = 16;

    public DenoisedPathTracer(String id, String name, String description, RayTracer tracer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tracer = tracer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void render(DefaultRenderManager manager) throws InterruptedException {
        Scene scene = manager.bufferedScene;
        double[] sampleBuffer = scene.getSampleBuffer();
        int sppPerPass = manager.context.sppPerPass();

        int originalSpp = scene.spp;
        int originalTarget = scene.getTargetSpp();
        double[] originalBuffer = originalSpp > 0 ? Arrays.copyOf(sampleBuffer, sampleBuffer.length) : null;

        boolean dirtyScene = false;
        boolean rendering = true;

        // Render albedo pass
        if (enableAlbedo && albedoSpp > 0) {
            dirtyScene = true;
            scene.spp = 0;
            scene.setTargetSpp(albedoSpp);
            while (scene.spp < albedoSpp) {
                renderPass(manager, sppPerPass, albedoTracer);
                if (postRender.getAsBoolean()) {
                    rendering = false;
                    break;
                }
            }
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            scene.saveFrame(out, getPfmExportFormat(), TaskTracker.NONE, manager.pool.threads);
        }

        // Render normal pass
        if (rendering && enableNormal && normalSpp > 0) {
            dirtyScene = true;
            scene.spp = 0;
            scene.setTargetSpp(normalSpp);
            while (scene.spp < normalSpp) {
                renderPass(manager, sppPerPass, normalTracer);
                if (postRender.getAsBoolean()) {
                    rendering = false;
                    break;
                }
            }
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            scene.saveFrame(out, getPfmExportFormat(), TaskTracker.NONE, manager.pool.threads);
        }

        // Restore scene if dirty
        if (dirtyScene) {
            scene.spp = originalSpp;
            scene.setTargetSpp(originalTarget);
            if (originalBuffer != null)
                Arrays.setAll(sampleBuffer, i -> originalBuffer[i]);
        }

        // Render beauty pass
        if (rendering) {
            while (scene.spp < scene.getTargetSpp()) {
                renderPass(manager, sppPerPass, tracer);
                if (postRender.getAsBoolean()) {
                    rendering = false;
                    break;
                }
            }
            File out = manager.context.getSceneFile(scene.name + ".beauty.pfm");
            scene.saveFrame(out, getPfmExportFormat(), TaskTracker.NONE, manager.pool.threads);
        }

        // Denoise
        if (rendering) {
            String denoiserPath = PersistentSettings.settings.getString("oidnPath", null);
            if (denoiserPath != null) {
                File denoisedPfm = manager.context.getSceneFile(scene.name + ".denoised.pfm");
                try {
                    OidnBinaryDenoiser.denoise(denoiserPath,
                            manager.context.getSceneFile(scene.name + ".beauty.pfm"),
                            enableAlbedo ? manager.context.getSceneFile(scene.name + ".albedo.pfm") : null,
                            enableNormal ? manager.context.getSceneFile(scene.name + ".normal.pfm") : null,
                            denoisedPfm
                    );

                    float[] denoisedBuffer = PortableFloatMap.readToFloatBuffer(
                            new BufferedInputStream(new FileInputStream(denoisedPfm)));
                    if (denoisedBuffer.length != sampleBuffer.length) {
                        throw new RuntimeException("Denoised dimensions do not match render dimensions.");
                    }
                    Arrays.setAll(sampleBuffer, i -> (double) denoisedBuffer[i]);
                } catch (IOException | RuntimeException e) {
                    Log.error(e);
                }
            }
        }

        postRender.getAsBoolean();
    }

    protected void renderPass(DefaultRenderManager manager, int passSpp, RayTracer passTracer) throws InterruptedException {
        Scene scene = manager.bufferedScene;
        double[] sampleBuffer = scene.getSampleBuffer();
        int width = scene.width;
        int height = scene.height;

        Camera cam = scene.camera();
        double halfWidth = width / (2.0 * height);
        double invHeight = 1.0 / height;

        int spp = scene.spp;
        double passinv = 1.0 / passSpp;
        double sinv = 1.0 / (passSpp + spp);

        submitTiles(manager, (state, pixel) -> {
            int x = pixel.firstInt();
            int y = pixel.secondInt();

            double sr = 0;
            double sg = 0;
            double sb = 0;

            for (int k = 0; k < passSpp; k++) {
                double ox = state.random.nextDouble();
                double oy = state.random.nextDouble();

                cam.calcViewRay(state.ray, state.random,
                        -halfWidth + (x + ox) * invHeight,
                        -0.5 + (y + oy) * invHeight);
                scene.rayTrace(passTracer, state);

                sr += state.ray.color.x;
                sg += state.ray.color.y;
                sb += state.ray.color.z;
            }

            int offset = 3 * (y*width + x);
            sampleBuffer[offset + 0] = (sampleBuffer[offset + 0] * spp + (sr * passinv)) * sinv;
            sampleBuffer[offset + 1] = (sampleBuffer[offset + 1] * spp + (sg * passinv)) * sinv;
            sampleBuffer[offset + 2] = (sampleBuffer[offset + 2] * spp + (sb * passinv)) * sinv;
        });

        manager.pool.awaitEmpty();
        scene.spp += passSpp;
    }

    protected static PictureExportFormat getPfmExportFormat() {
        return PictureExportFormats.getFormat("PFM").orElse(new PfmExportFormat());
    }
}
