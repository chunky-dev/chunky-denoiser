package de.lemaik.chunky.denoiser;

import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.renderer.RendererFactory;
import se.llbit.chunky.ui.ChunkyFx;

import java.lang.reflect.Field;

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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        chunky.setRayTracerFactory(() -> rayTracer);
    }

    public static void main(String[] args) throws Exception {
        // Start Chunky normally with this plugin attached.
        Chunky.loadDefaultTextures();
        Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
        new DenoiserPlugin().attach(chunky);
        ChunkyFx.startChunkyUI(chunky);
    }
}
