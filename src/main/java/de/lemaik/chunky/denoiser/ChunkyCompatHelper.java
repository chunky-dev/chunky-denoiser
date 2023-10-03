package de.lemaik.chunky.denoiser;

import se.llbit.chunky.world.Material;
import se.llbit.math.Ray;

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
            throw new RuntimeException("Could not invoke " + methodName + " on class " + className.getName(), e);
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

        public static void doWaterDisplacement(Ray ray) {
            try {
                doWaterDisplacementImpl.invoke(null, ray);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not invoke doWaterDisplacement", e);
            }
        }
    }
}
