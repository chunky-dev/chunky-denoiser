package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PreviewRayTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;
import se.llbit.math.Vector3;

public class NormalTracer implements RayTracer {
    /**
     * If true, all values are mapped to positive values so that they can be displayed on the rendered image.
     */
    public static final boolean MAP_POSITIVE = false;

    @Override
    public void trace(Scene scene, WorkerState state) {
        Ray ray = state.ray;
        if (PreviewRayTracer.nextIntersection(scene, ray)) {
            if (MAP_POSITIVE) {
                Vector3 normal = new Vector3(ray.n);
                normal.normalize();
                ray.color.set((normal.x + 1) / 2, (normal.y + 1) / 2, (normal.z + 1) / 2, 1);
            } else {
                ray.color.set(ray.n.x, ray.n.y, ray.n.z, 1);
            }
        }
    }
}
