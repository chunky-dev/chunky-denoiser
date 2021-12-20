package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.PathTracingRenderer;

public class NormalRenderer extends PathTracingRenderer {

  public static final String ID = "NORMAL";

  /**
   * If true, all values are mapped to positive values so that they can be displayed on the rendered
   * image.
   */
  static final boolean MAP_POSITIVE = false;

  public NormalRenderer() {
    super(ID, "Normal map", "Renderer for normal maps (used for denoising)",
        new NormalTracer());
  }
}