package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PreviewRayTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;

import java.util.Random;

public class NormalTracer implements RayTracer {
    @Override
    public void trace(Scene scene, WorkerState state) {
        Ray ray = state.ray;
        if (PreviewRayTracer.nextIntersection(scene, ray)) {
            ray.color.set(ray.n.x, ray.n.y, ray.n.z, 1);
        }
    }
}
