package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.PathTracingRenderer;
import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PreviewRayTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;
import se.llbit.math.Vector3;

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

  private static class NormalTracer implements RayTracer {

    @Override
    public void trace(Scene scene, WorkerState state) {
      Ray ray = state.ray;
      if (PreviewRayTracer.nextIntersection(scene, ray)) {
        if (BetterRenderManager.NORMAL_WATER_DISPLACEMENT && !ChunkyCompatHelper.Scene.isStillWaterEnabled(scene)
            && ray.getCurrentMaterial().isWater()) {
          ChunkyCompatHelper.Water.doWaterDisplacement(ray);
        }

        if (MAP_POSITIVE) {
          Vector3 normal = new Vector3(ray.getNormal());
          normal.normalize();
          ray.color.set((normal.x + 1) / 2, (normal.y + 1) / 2, (normal.z + 1) / 2, 1);
        } else {
          Vector3 normal = ray.getNormal();
          ray.color.set(normal.x, normal.y, normal.z, 1);
        }
      }
    }
  }
}