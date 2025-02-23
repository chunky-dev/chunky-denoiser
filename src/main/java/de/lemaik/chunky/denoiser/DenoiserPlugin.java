package de.lemaik.chunky.denoiser;

import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.ui.ChunkyFx;
import se.llbit.chunky.ui.render.RenderControlsTabTransformer;

/**
 * This plugin renders normal and albedo maps for use with image de-noisers.
 */
public class DenoiserPlugin implements Plugin {
    public static final String DENOISER_RENDERER_ID = "DenoiserPasses";

    @Override
    public void attach(Chunky chunky) {
        Denoiser denoiser = new OidnBinaryDenoiser();

        DenoisedPathTracingRenderer denoisedPathTracer = new DenoisedPathTracingRenderer(
                denoiser,
                "DenoisedPathTracer",
                "DenoisedPathTracer",
                "DenoisedPathTracer",
                new PathTracer()
        );
        Chunky.addRenderer(denoisedPathTracer);

        DenoiserPassRenderer inPlaceDenoisingRenderer = new DenoiserPassRenderer(
                denoiser,
                DENOISER_RENDERER_ID,
                "DenoiserPasses",
                "Renders the denoiser passes."
        );
        Chunky.addRenderer(inPlaceDenoisingRenderer);

        RenderControlsTabTransformer prev = chunky.getRenderControlsTabTransformer();
        chunky.setRenderControlsTabTransformer(tabs -> {
            tabs = prev.apply(tabs);
            tabs.add(new DenoiserTabImpl());
            return tabs;
        });
    }

    public static void main(String[] args) {
        // Start Chunky normally with this plugin attached.
        Chunky.loadDefaultTextures();
        Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
        new DenoiserPlugin().attach(chunky);
        ChunkyFx.startChunkyUI(chunky);
    }
}
