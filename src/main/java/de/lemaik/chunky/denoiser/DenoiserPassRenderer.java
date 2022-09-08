package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;
import se.llbit.util.TaskTracker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class DenoiserPassRenderer extends MultiPassRenderer {
    protected final Denoiser denoiser;

    protected final String id;
    protected final String name;
    protected final String description;

    protected final RayTracer albedoTracer = new AlbedoTracer();
    protected final RayTracer normalTracer;

    public DenoiserPassRenderer(Denoiser denoiser,
                                String id, String name, String description) {
        this.denoiser = denoiser;

        this.normalTracer = new NormalTracer();

        this.id = id;
        this.name = name;
        this.description = description;
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
    public boolean autoPostProcess() {
        return false;
    }

    @Override
    public void render(DefaultRenderManager manager) throws InterruptedException {
        Scene scene = manager.bufferedScene;
        DenoiserSettings settings = new DenoiserSettings();
        settings.loadFromScene(scene);

        double[] sampleBuffer = scene.getSampleBuffer();

        boolean albedoEnable = settings.renderAlbedo.get();
        int albedoTarget = settings.albedoSpp.get();
        float[] albedoBuffer = albedoEnable ? new float[sampleBuffer.length] : null;

        boolean normalEnable = settings.renderNormal.get();
        int normalTarget = settings.normalSpp.get();
        float[] normalBuffer = normalEnable ? new float[sampleBuffer.length] : null;

        if (albedoEnable || normalEnable) {
            int targetSpp = Math.max(albedoTarget, normalTarget);
            scene.spp = 0;

            while (scene.spp < targetSpp) {
                if (albedoEnable && scene.spp < albedoTarget) {
                    this.renderPass(manager, scene.spp, 1, albedoTracer, albedoBuffer);
                }
                if (normalEnable && scene.spp < normalTarget) {
                    this.renderPass(manager, scene.spp, 1, normalTracer, normalBuffer);
                }
                scene.spp += 1;
                if (scene.spp < targetSpp && postRender.getAsBoolean()) {
                    // Canceled
                    return;
                }
            }
        }

        if (settings.saveBeauty.get()) {
            File out = manager.context.getSceneFile(scene.name + ".beauty.pfm");
            scene.saveFrame(out, PortableFloatMap.getPfmExportFormat(), TaskTracker.NONE);
        }

        if (settings.saveAlbedo.get() && albedoBuffer != null) {
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(albedoBuffer, scene.canvasConfig.getWidth(), scene.canvasConfig.getHeight(), ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save albedo pass", e);
            }
        }

        if (settings.saveNormal.get() && normalBuffer != null) {
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(normalBuffer, scene.canvasConfig.getWidth(), scene.canvasConfig.getHeight(), ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save normal pass", e);
            }
        }

        try {
            manager.getRenderTask().update("Denoising", scene.getTargetSpp(), scene.spp);
            denoiser.init();
            denoiser.denoiseDouble(scene.canvasConfig.getWidth(), scene.canvasConfig.getHeight(), sampleBuffer, albedoBuffer, normalBuffer, sampleBuffer);
        } catch (Denoiser.DenoisingFailedException e) {
            Log.error("Failed to denoise", e);
        }

        postRender.getAsBoolean();
    }
}
