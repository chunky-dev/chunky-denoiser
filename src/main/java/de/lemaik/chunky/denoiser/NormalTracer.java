package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.WaterShadingStrategy;
import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PreviewRayTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;
import se.llbit.math.Vector3;

public class NormalTracer implements RayTracer {
    /**
     * If true, all values are mapped to positive values so that they can be displayed on the rendered
     * image.
     */
    private final static boolean MAP_POSITIVE = false;

    @Override
    public void trace(Scene scene, WorkerState state) {
        Ray ray = state.ray;
        if (PreviewRayTracer.nextIntersection(scene, ray)) {
            if (DenoiserSettings.isWaterDisplacementEnabled(scene) && scene.getWaterShadingStrategy() != WaterShadingStrategy.STILL
                    && ray.getCurrentMaterial().isWater()) {
                scene.getCurrentWaterShader().doWaterShading(ray, scene.getAnimationTime());
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
