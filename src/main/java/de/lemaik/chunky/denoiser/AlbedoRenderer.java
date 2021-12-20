package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.PathTracingRenderer;

public class AlbedoRenderer extends PathTracingRenderer {

  public static final String ID = "ALBEDO";

  public AlbedoRenderer() {
    super(ID, "Albedo map", "Renderer for albedo maps (used for denoising)",
        new AlbedoTracer());
  }
}