package de.lemaik.chunky.denoiser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.renderer.SnapshotControl;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.ChunkyFx;
import se.llbit.chunky.ui.render.RenderControlsTabTransformer;
import se.llbit.log.Log;

/**
 * This plugin renders normal and albedo maps for use with image de-noisers.
 */
public class DenoiserPlugin implements Plugin {

  @Override
  public void attach(Chunky chunky) {
    if (chunky.isHeadless()) {
      Log.warn("The denoiser plugin does not support headless mode and will not be enabled.");
      return;
    }

    Chunky.addRenderer(new AlbedoRenderer());
    Chunky.addRenderer(new NormalRenderer());
    chunky.setRenderManagerFactory(BetterRenderManager::new);

    RenderControlsTabTransformer prev = chunky.getRenderControlsTabTransformer();
    chunky.setRenderControlsTabTransformer(tabs -> {
      tabs = prev.apply(tabs);
      tabs.add(DenoiserTab.getImplementation());
      return tabs;
    });
  }

  public static void main(String[] args) throws Exception {
    // Start Chunky normally with this plugin attached.
    Chunky.loadDefaultTextures();
    Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
    new DenoiserPlugin().attach(chunky);
    ChunkyFx.startChunkyUI(chunky);
  }
}
