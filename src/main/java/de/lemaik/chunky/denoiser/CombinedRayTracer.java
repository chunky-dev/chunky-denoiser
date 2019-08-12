package de.lemaik.chunky.denoiser;

import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.PathTracer;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;

public class CombinedRayTracer implements RayTracer {
    private RayTracer rayTracer = new PathTracer();

    @Override
    public void trace(Scene scene, WorkerState workerState) {
        this.rayTracer.trace(scene, workerState);
    }

    public RayTracer getRayTracer() {
        return rayTracer;
    }

    public void setRayTracer(RayTracer rayTracer) {
        this.rayTracer = rayTracer;
    }
}
