package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;
import se.llbit.util.TaskTracker;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class DenoisedPathTracingRenderer extends MultiPassRenderer {
    protected final DenoiserSettings settings;
    protected final Denoiser denoiser;

    protected final String id;
    protected final String name;
    protected final String description;
    protected final RayTracer tracer;

    protected final AlbedoTracer albedoTracer = new AlbedoTracer();
    protected final NormalTracer normalTracer;

    public DenoisedPathTracingRenderer(DenoiserSettings settings, Denoiser denoiser,
                                       String id, String name, String description, RayTracer tracer) {
        this.settings = settings;
        this.denoiser = denoiser;

        this.normalTracer = new NormalTracer(settings);

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

        boolean albedoEnable = settings.renderAlbedo.get();
        int albedoTarget = settings.albedoSpp.get();
        int albedoSampleScale = (int) Math.ceil((double) albedoTarget / scene.getTargetSpp());
        int albedoSamples = 0;
        float[] albedoBuffer = albedoEnable ? new float[sampleBuffer.length] : null;

        boolean normalEnable = settings.renderNormal.get();
        int normalTarget = settings.normalSpp.get();
        int normalSampleScale = (int) Math.ceil((double) normalTarget / scene.getTargetSpp());
        int normalSamples = 0;
        float[] normalBuffer = normalEnable ? new float[sampleBuffer.length] : null;

        while (scene.spp < scene.getTargetSpp()) {
            if (albedoEnable && albedoSamples < albedoTarget) {
                int samples = Math.min(albedoSampleScale, albedoTarget - albedoSamples);
                albedoSamples = this.renderPass(manager, albedoSamples, samples, albedoTracer, albedoBuffer);
            }
            if (normalEnable && normalSamples < normalTarget) {
                int samples = Math.max(normalSampleScale, normalTarget - normalSamples);
                normalSamples = this.renderPass(manager, normalSamples, samples, normalTracer, normalBuffer);
            }
            scene.spp = renderPass(manager, scene.spp, 1, tracer, null);

            if (scene.spp < scene.getTargetSpp() && postRender.getAsBoolean()) {
                // Canceled
                return;
            }
        }

        if (settings.saveBeauty.get()) {
            File out = manager.context.getSceneFile(scene.name + ".beauty.pfm");
            scene.saveFrame(out, PortableFloatMap.getPfmExportFormat(),
                    TaskTracker.NONE, manager.context.numRenderThreads());
        }

        if (settings.saveAlbedo.get()) {
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(albedoBuffer, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save albedo pass", e);
            }
        }

        if (settings.saveNormal.get()) {
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(normalBuffer, scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save normal pass", e);
            }
        }

        try {
            manager.getRenderTask().update("Denoising", scene.getTargetSpp(), scene.spp);
            denoiser.init();
            denoiser.denoiseDouble(scene.width, scene.height, sampleBuffer, albedoBuffer, normalBuffer, sampleBuffer);
        } catch (Denoiser.DenoisingFailedException e) {
            Log.error("Failed to denoise", e);
        }

        postRender.getAsBoolean();
    }
}
