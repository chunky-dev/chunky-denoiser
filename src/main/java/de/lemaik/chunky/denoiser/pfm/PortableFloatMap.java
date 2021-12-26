package de.lemaik.chunky.denoiser.pfm;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PortableFloatMap {
    public static void writeImage(float[] pixels, int width, int height, ByteOrder byteOrder, OutputStream out) throws IOException {
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
                        .putFloat(pixels[(y * width + x) * 3 + 0])
                        .array());
                out.write(ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putFloat(pixels[(y * width + x) * 3 + 1])
                        .array());
                out.write(ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putFloat(pixels[(y * width + x) * 3 + 2])
                        .array());
            }
        }
    }

    public static float[] readImage(InputStream in) throws IOException {
        try (DataInputStream din = new DataInputStream(in)) {
            if (!readLine(din).equals("PF")) {
                throw new IOException("Invalid Header");
            }
            String[] size = readLine(din).split(" ");
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            float scale = Float.parseFloat(readLine(din));

            float[] outputBuffer = new float[3 * width * height];
            byte[] buff = new byte[12];
            ByteBuffer buffer = ByteBuffer.wrap(buff)
                    .order(scale < 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            scale = Math.abs(scale);
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    din.readFully(buff, 0, 12);
                    int offset = (y * width + x) * 3;
                    outputBuffer[offset + 0] = scale * buffer.getFloat(0);
                    outputBuffer[offset + 1] = scale * buffer.getFloat(4);
                    outputBuffer[offset + 2] = scale * buffer.getFloat(8);
                }
            }
            return outputBuffer;
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        byte[] buff = new byte[1];
        buff[0] = (byte) in.read();
        while (buff[0] != 0x0a) {
            line.append(new String(buff, StandardCharsets.US_ASCII));
            buff[0] = (byte) in.read();
        }
        return line.toString();
    }
}
