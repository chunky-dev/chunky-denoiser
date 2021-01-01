package de.lemaik.chunky.denoiser;

import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.renderer.RendererFactory;
import se.llbit.chunky.renderer.SnapshotControl;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.ChunkyFx;
import se.llbit.chunky.ui.render.RenderControlsTabTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This plugin renders normal and albedo maps for use with image de-noisers.
 */
public class DenoiserPlugin implements Plugin {
    @Override
    public void attach(Chunky chunky) {
        CombinedRayTracer rayTracer = new CombinedRayTracer();
        try {
            Field f = chunky.getClass().getDeclaredField("rendererFactory");
            f.setAccessible(true);
            f.set(chunky, (RendererFactory) (context, headless) -> new BetterRenderManager(context, headless, rayTracer));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        chunky.setRayTracerFactory(() -> rayTracer);

        RenderControlsTabTransformer prev = chunky.getRenderControlsTabTransformer();
        chunky.setRenderControlsTabTransformer(tabs -> {
            tabs = prev.apply(tabs);
            tabs.add(DenoiserTab.getImplementation());
            return tabs;
        });

        // Replace SnapshotControl.DEFAULT to prevent saving snapshots when rendering normal or albedo maps
        try {
            SnapshotControl defaultSnapshotControl = SnapshotControl.DEFAULT;
            Field f = SnapshotControl.class.getDeclaredField("DEFAULT");
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.set(null, new SnapshotControl() {
                @Override
                public boolean saveSnapshot(Scene scene, int nextSpp) {
                    if (rayTracer.getRayTracer() instanceof PathTracer) {
                        return defaultSnapshotControl.saveSnapshot(scene, nextSpp);
                    }
                    return false;
                }

                @Override
                public boolean saveRenderDump(Scene scene, int nextSpp) {
                    if (rayTracer.getRayTracer() instanceof PathTracer) {
                        return defaultSnapshotControl.saveRenderDump(scene, nextSpp);
                    }
                    return false;
                }
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // Start Chunky normally with this plugin attached.
        Chunky.loadDefaultTextures();
        Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
        new DenoiserPlugin().attach(chunky);
        ChunkyFx.startChunkyUI(chunky);
    }
}
