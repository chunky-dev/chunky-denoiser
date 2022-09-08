package de.lemaik.chunky.denoiser;

public interface Denoiser {
    /**
     * Denoise an image.
     *
     * @param width  Width of the image
     * @param height Height of the image
     * @param beauty Beauty (path traced) pass
     * @param albedo Albedo pass. Null if not rendered.
     * @param normal Normal pass. Null if not rendered.
     * @return Denoised image
     */
    float[] denoise(int width, int height, float[] beauty, float[] albedo, float[] normal)
            throws DenoisingFailedException;

    /**
     * Denoise an image.
     *
     * @param width  Width of the image
     * @param height Height of the image
     * @param beauty Beauty (path traced) pass
     * @param albedo Albedo pass. Null if not rendered.
     * @param normal Normal pass. Null if not rendered.
     * @param output Denoised output (may be the same as beauty pass)
     */
    default void denoiseDouble(int width, int height, double[] beauty, float[] albedo, float[] normal, double[] output)
            throws DenoisingFailedException {
        float[] beautyF = new float[beauty.length];
        for (int i = 0; i < beauty.length; i++)
            beautyF[i] = (float) beauty[i];

        float[] outputF = denoise(width, height, beautyF, albedo, normal);
        for (int i = 0; i < output.length; i++)
            output[i] = outputF[i];
    }

    /**
     * Initialize this denoiser and prepare it for denoising.
     */
    default void init() { }

    /**
     * Denoising failed for some reason.
     */
    final class DenoisingFailedException extends Exception {
        public DenoisingFailedException() {
            super();
        }

        public DenoisingFailedException(String message) {
            super(message);
        }

        public DenoisingFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public DenoisingFailedException(Throwable cause) {
            super(cause);
        }
    }
}
