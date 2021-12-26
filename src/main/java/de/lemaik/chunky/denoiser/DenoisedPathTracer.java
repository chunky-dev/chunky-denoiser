package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.export.PfmExportFormat;
import se.llbit.chunky.renderer.export.PictureExportFormats;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;
import se.llbit.util.TaskTracker;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

public class DenoisedPathTracer extends MultiPassRenderer {
    protected final String id;
    protected final String name;
    protected final String description;
    protected RayTracer tracer;

    protected static final RayTracer albedoTracer = new AlbedoTracer();
    protected static final RayTracer normalTracer = new NormalTracer();

    private boolean hiddenPasses = false;

    public boolean enableAlbedo = true;
    public int albedoSpp = 16;
    public boolean enableNormal = true;
    public int normalSpp = 16;

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
        boolean aborted = false;

        int originalSpp = scene.spp;
        int sceneTarget = scene.getTargetSpp();

        int maxSpp = Math.max(sceneTarget, Math.max(albedoSpp, normalSpp));
        scene.setTargetSpp(maxSpp);

        RayTracer[] tracers = new RayTracer[] {tracer, normalTracer, albedoTracer};
        float[][] buffers = new float[][] {null,
                enableNormal ? new float[sampleBuffer.length] : null,
                enableAlbedo ? new float[sampleBuffer.length] : null};
        boolean[] tracerMask = new boolean[] {true, enableNormal, enableAlbedo};

        if (enableNormal || enableAlbedo) {
            scene.spp = 0;
        }

        while (scene.spp < maxSpp) {
            tracerMask[0] = scene.spp >= originalSpp && scene.spp < sceneTarget;
            hiddenPasses = !tracerMask[0];
            tracerMask[1] = scene.spp < normalSpp;
            tracerMask[2] = scene.spp < albedoSpp;
            renderPass(manager, manager.context.sppPerPass(), tracers, buffers, tracerMask);
            if (scene.spp < maxSpp && postRender.getAsBoolean()) {
                aborted = true;
                break;
            }
        }

        if (!aborted && enableNormal) {
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                PortableFloatMap.writeImage(buffers[1], scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save normal pass", e);
            }
        }

        if (!aborted && enableAlbedo) {
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                PortableFloatMap.writeImage(buffers[2], scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save normal pass", e);
            }
        }

        // Denoise
        if (!aborted) {
            String denoiserPath = PersistentSettings.settings.getString("oidnPath", null);
            if (denoiserPath != null) {
                File denoisedPfm = manager.context.getSceneFile(scene.name + ".denoised.pfm");
                try {
                    File beauty = manager.context.getSceneFile(scene.name + ".beauty.pfm");
                    scene.saveFrame(beauty,
                            PictureExportFormats.getFormat("PFM").orElseGet(PfmExportFormat::new),
                            TaskTracker.NONE, manager.pool.threads);

                    OidnBinaryDenoiser.denoise(denoiserPath,
                            beauty,
                            enableAlbedo ? manager.context.getSceneFile(scene.name + ".albedo.pfm") : null,
                            enableNormal ? manager.context.getSceneFile(scene.name + ".normal.pfm") : null,
                            denoisedPfm
                    );

                    float[] denoisedBuffer = PortableFloatMap.readImage(
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

        if (scene.spp < originalSpp) {
            scene.spp = originalSpp;
        } else if (scene.spp > sceneTarget) {
            scene.spp = sceneTarget;
        }
        scene.setTargetSpp(sceneTarget);
        postRender.getAsBoolean();
    }

    @Override
    public boolean autoPostProcess() {
        return !hiddenPasses;
    }
}
