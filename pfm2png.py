from PIL import Image
import os
import struct
import argparse

parser = argparse.ArgumentParser(description="Convert pfm files to png images.")
parser.add_argument("pfm", type=str, help="The path of a pfm file")
parser.add_argument("--out", type=str, help="The output path for the png image", required=True)
args = parser.parse_args()

def readline(f):
    line = ""
    byte = f.read(1)[0]
    while (byte != 0x0a):
        line += bytes([byte]).decode('ascii')
        byte = f.read(1)[0]
    return line

dir_path = os.path.dirname(os.path.realpath(__file__))
with open(args.pfm, "rb") as f:
    while (f.read(1)[0] != 0x0a):
        pass # skip PF
    size = readline(f).split(" ")
    width = int(size[0])
    height = int(size[1])
    endianess = float(readline(f)) # negative is little endian, positive is big endian

    pixels = struct.unpack(('<' if endianess < 0 else '>')+('f'*width*height*3), f.read(4*width*height*3))

    im = Image.new('RGB', (width, height))
    im_pixels = im.load()

    for y in range(0, height):
        for x in range(0, width):
            im_pixels[x, height - 1 - y] = (
                int(pixels[(y * width + x) * 3 + 0] * 255),
                int(pixels[(y * width + x) * 3 + 1] * 255),
                int(pixels[(y * width + x) * 3 + 2] * 255)
            )

    im.save(args.out)
