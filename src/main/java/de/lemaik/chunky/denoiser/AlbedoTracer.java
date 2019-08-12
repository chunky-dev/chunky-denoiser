package de.lemaik.chunky.denoiser;

import se.llbit.chunky.block.Air;
import se.llbit.chunky.block.Water;
import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PreviewRayTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;

public class AlbedoTracer implements RayTracer {
    @Override
    public void trace(Scene scene, WorkerState state) {
        Ray ray = state.ray;
        if (scene.isInWater(ray)) {
            ray.setCurrentMaterial(Water.INSTANCE, 0);
        } else {
            ray.setCurrentMaterial(Air.INSTANCE, 0);
        }

        while (true) {
            if (!PreviewRayTracer.nextIntersection(scene, ray)) {
                break;
            }

            if (ray.getCurrentMaterial() != Air.INSTANCE && ray.color.w > 0.0D) {
                break;
            }

            ray.o.scaleAdd(1.0E-4D, ray.d);
        }
    }
}
