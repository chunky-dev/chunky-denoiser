package de.lemaik.chunky.denoiser.pfm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PortableFloatMap {
    public static void writeImage(double[] pixels, int width, int height, ByteOrder byteOrder, OutputStream out) throws IOException {
        out.write("PF".getBytes(StandardCharsets.US_ASCII));
        out.write(0x0a);
        out.write((width + " " + height).getBytes(StandardCharsets.US_ASCII));
        out.write(0x0a);
        out.write((byteOrder == ByteOrder.LITTLE_ENDIAN ? "-1.0" : "1.0").getBytes(StandardCharsets.US_ASCII));
        out.write(0x0a);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                out.write(ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putFloat((float) pixels[(y * width + x) * 3])
                        .array());
                out.write(ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putFloat((float) pixels[(y * width + x) * 3 + 1])
                        .array());
                out.write(ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putFloat((float) pixels[(y * width + x) * 3 + 2])
                        .array());
            }
        }
    }
}
