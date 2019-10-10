package de.lemaik.chunky.denoiser;

import se.llbit.chunky.block.Block;
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
            ray.setCurrentMaterial(Block.WATER, 0);
        } else {
            ray.setCurrentMaterial(Block.AIR, 0);
        }

        while (true) {
            if (!PreviewRayTracer.nextIntersection(scene, ray)) {
                if (ray.getPrevMaterial().isWater()) {
                    // set water color to white
                    ray.color.set(1, 1, 1, 1);
                } else if (ray.depth == 0) {
                    // direct sky hit
                    if (!scene.transparentSky()) {
                        scene.sky().getSkyColorInterpolated(ray);
                    }
                }
                // ignore indirect sky hits
                break;
            }

            if (!Block.AIR.isSameMaterial(ray.getCurrentMaterial()) && ray.color.w > 0.0D) {
                break;
            }

            ray.o.scaleAdd(1.0E-4D, ray.d);
        }
    }
}
