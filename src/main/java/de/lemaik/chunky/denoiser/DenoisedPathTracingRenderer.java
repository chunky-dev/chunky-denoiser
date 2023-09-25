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

public class DenoisedPathTracingRenderer extends MultiPassRenderer {
    protected final DenoiserSettings settings;
    protected final Denoiser denoiser;

    protected final String id;
    protected final String name;
    protected final String description;
    protected final RayTracer tracer;

    protected final AlbedoTracer albedoTracer = new AlbedoTracer();
    protected final NormalTracer normalTracer;

    private boolean hiddenPasses = false;

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
        boolean aborted = false;

        int originalSpp = scene.spp;
        int sceneTarget = scene.getTargetSpp();

        int maxSpp = Math.max(sceneTarget, Math.max(settings.albedoSpp.get(), settings.normalSpp.get()));
        scene.setTargetSpp(maxSpp);

        RayTracer[] tracers = new RayTracer[]{albedoTracer, normalTracer, tracer};
        float[][] buffers = new float[][]{
                settings.renderAlbedo.get() ? new float[sampleBuffer.length] : null,
                settings.renderNormal.get() ? new float[sampleBuffer.length] : null,
                null};
        boolean[] tracerMask = new boolean[3];
        scene.spp = 0;

        while (scene.spp < maxSpp) {
            tracerMask[0] = settings.renderAlbedo.get() && scene.spp < settings.albedoSpp.get();
            tracerMask[1] = settings.renderNormal.get() && scene.spp < settings.normalSpp.get();
            tracerMask[2] = scene.spp >= originalSpp && scene.spp < sceneTarget;
            hiddenPasses = !tracerMask[2];
            renderPass(manager, manager.context.sppPerPass(), tracers, buffers, tracerMask);
            if (scene.spp < maxSpp && postRender.getAsBoolean()) {
                aborted = true;
                break;
            }
        }

        if (!aborted && settings.saveBeauty.get()) {
            File out = manager.context.getSceneFile(scene.name + ".beauty.pfm");
            scene.saveFrame(out, PortableFloatMap.getPfmExportFormat(), TaskTracker.NONE);
        }

        if (!aborted && settings.saveAlbedo.get()) {
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(buffers[0], scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save albedo pass", e);
            }
        }

        if (!aborted && settings.saveNormal.get()) {
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out.toPath()))) {
                PortableFloatMap.writeImage(buffers[1], scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save normal pass", e);
            }
        }

        if (!aborted) {
            if (denoiser instanceof OidnBinaryDenoiser)
                ((OidnBinaryDenoiser) denoiser).loadPath();

            try {
                denoiser.denoiseDouble(scene.width, scene.height, sampleBuffer,
                        buffers[0], buffers[1], sampleBuffer);
            } catch (Denoiser.DenoisingFailedException e) {
                Log.error("Failed to denoise", e);
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
