package de.lemaik.chunky.denoiser;

import de.lemaik.chunky.denoiser.pfm.PortableFloatMap;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;
import se.llbit.util.TaskTracker;

import java.io.*;
import java.nio.ByteOrder;

public class DenoiserPassRenderer extends MultiPassRenderer {
    protected final DenoiserSettings settings;
    protected final Denoiser denoiser;

    protected final String id;
    protected final String name;
    protected final String description;

    protected static final RayTracer albedoTracer = new AlbedoTracer();
    protected static final RayTracer normalTracer = new NormalTracer();

    public DenoiserPassRenderer(DenoiserSettings settings, Denoiser denoiser,
                                String id, String name, String description) {
        this.settings = settings;
        this.denoiser = denoiser;

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
        double[] sampleBuffer = scene.getSampleBuffer();
        boolean aborted = false;

        scene.setTargetSpp(Math.max(settings.getAlbedoSpp(), settings.getNormalSpp()));

        RayTracer[] tracers = new RayTracer[] {albedoTracer, normalTracer};
        float[][] buffers = new float[][] {
                settings.getRenderAlbedo() ? new float[sampleBuffer.length] : null,
                settings.getRenderNormal() ? new float[sampleBuffer.length] : null,
        };
        boolean[] tracerMask = new boolean[2];
        scene.spp = 0;

        while (scene.spp < scene.getTargetSpp()) {
            tracerMask[0] = settings.getRenderAlbedo() && scene.spp < settings.getAlbedoSpp();
            tracerMask[1] = settings.getRenderNormal() && scene.spp < settings.getNormalSpp();
            renderPass(manager, manager.context.sppPerPass(), tracers, buffers, tracerMask);
            if (scene.spp < scene.getTargetSpp() && postRender.getAsBoolean()) {
                aborted = true;
                break;
            }
        }

        if (!aborted && settings.getSaveBeauty()) {
            File out = manager.context.getSceneFile(scene.name + ".beauty.pfm");
            scene.saveFrame(out, PortableFloatMap.getPfmExportFormat(),
                    TaskTracker.NONE, manager.context.numRenderThreads());
        }

        if (!aborted && settings.getSaveAlbedo()) {
            File out = manager.context.getSceneFile(scene.name + ".albedo.pfm");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                PortableFloatMap.writeImage(buffers[0], scene.width, scene.height, ByteOrder.LITTLE_ENDIAN, os);
            } catch (IOException e) {
                Log.error("Failed to save albedo pass", e);
            }
        }

        if (!aborted && settings.getSaveNormal()) {
            File out = manager.context.getSceneFile(scene.name + ".normal.pfm");
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
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

                scene.spp = scene.getTargetSpp();
                postRender.getAsBoolean();
            } catch (Denoiser.DenoisingFailedException e) {
                Log.error("Failed to denoise", e);
            }
        }
    }
}
