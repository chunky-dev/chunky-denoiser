package de.lemaik.chunky.denoiser;

import se.llbit.chunky.world.Material;
import se.llbit.math.Ray;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChunkyCompatHelper {

    private static Class<?> getClass(String... classNameCandidates) {
        for (String name : classNameCandidates) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new RuntimeException("No class found for " + String.join(" or ", classNameCandidates));
    }

    private static Method getMethod(Class<?> className, String methodName, Class<?>... parameterTypes) {
        try {
            return className.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get method " + methodName + " of class " + className.getName(), e);
        }
    }

    private static Field getField(Class<?> className, String fieldName) {
        try {
            return className.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not get field " + fieldName + " of class " + className.getName(), e);
        }
    }

    public static class Air {
        public static final Material INSTANCE;

        static {
            try {
                INSTANCE = (Material) ChunkyCompatHelper.getClass("se.llbit.chunky.block.Air", "se.llbit.chunky.block.minecraft.Air").getDeclaredField("INSTANCE").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("Could not get Air.INSTANCE", e);
            }
        }
    }

    public static class Water {
        public static final Material INSTANCE;

        static {
            try {
                INSTANCE = (Material) ChunkyCompatHelper.getClass("se.llbit.chunky.block.Water", "se.llbit.chunky.block.minecraft.Water").getDeclaredField("INSTANCE").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException("Could not get Water.INSTANCE", e);
            }
        }

        private static Class<?> water = ChunkyCompatHelper.getClass(
                "se.llbit.chunky.model.WaterModel",
                "se.llbit.chunky.model.minecraft.WaterModel"
        );

        private static Method doWaterDisplacementImpl = getMethod(water, "doWaterDisplacement", Ray.class);

        public static void doWaterDisplacement(se.llbit.chunky.renderer.scene.Scene scene, Ray ray) {
            try {
                doWaterDisplacementImpl.invoke(null, ray);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not invoke doWaterDisplacement", e);
            }
        }
    }

    public static class Scene {
        private static Method stillWaterEnabled;

        private static Class<?> stillWaterShader;

        private static Method getSky;

        private static Class<?> sky;

        private static Method getSkyColorInterpolated;

        private static Field width;

        private static Field height;

        static {
            try {
                stillWaterEnabled = se.llbit.chunky.renderer.scene.Scene.class.getDeclaredMethod("stillWaterEnabled");
            } catch (NoSuchMethodException e) {
                stillWaterShader = ChunkyCompatHelper.getClass("se.llbit.chunky.renderer.scene.StillWaterShader");
            }

            getSky = ChunkyCompatHelper.getMethod(se.llbit.chunky.renderer.scene.Scene.class, "sky");
            sky = ChunkyCompatHelper.getClass("se.llbit.chunky.renderer.scene.Sky", "se.llbit.chunky.renderer.scene.sky.Sky");
            getSkyColorInterpolated = ChunkyCompatHelper.getMethod(sky, "getSkyColorInterpolated", Ray.class);

            try {
                width = ChunkyCompatHelper.getField(se.llbit.chunky.renderer.scene.Scene.class, "width");
                height = ChunkyCompatHelper.getField(se.llbit.chunky.renderer.scene.Scene.class, "height");
            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof NoSuchFieldException)) {
                    throw e;
                }
            }
        }

        public static boolean isStillWaterEnabled(se.llbit.chunky.renderer.scene.Scene scene) {
            if (stillWaterEnabled != null) {
                try {
                    return (boolean) stillWaterEnabled.invoke(scene);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException("Could not invoke stillWaterEnabled()", e);
                }
            }
            return stillWaterShader.isInstance(scene.getCurrentWaterShader());
        }

        public static void getSkyColorInterpolated(se.llbit.chunky.renderer.scene.Scene scene, Ray ray) {
            try {
                getSkyColorInterpolated.invoke(getSky.invoke(scene), ray);
            } catch (InvocationTargetException | IllegalAccessException e2) {
                throw new RuntimeException("Could not invoke sky().getSkyColorInterpolated(ray)", e2);
            }
        }

        public static int getCanvasWidth(se.llbit.chunky.renderer.scene.Scene scene) {
            if (width != null) {
                try {
                    return (int) width.get(scene);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not get width", e);
                }
            }
            return scene.canvasConfig.getWidth();
        }

        public static int getCanvasHeight(se.llbit.chunky.renderer.scene.Scene scene) {
            if (height != null) {
                try {
                    return (int) height.get(scene);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not get height", e);
                }
            }
            return scene.canvasConfig.getHeight();
        }

        public static void doWaterShading(se.llbit.chunky.renderer.scene.Scene scene, Ray ray) {
            try {
                scene.getCurrentWaterShader().doWaterShading(ray, scene.getAnimationTime());
            } catch (NoSuchMethodError e) {
                Water.doWaterDisplacement(scene, ray);
            }
        }
    }
}
