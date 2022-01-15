package com.example.my_first_app.utils;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;
import java.util.zip.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Decompressor {

    private final static LZ4FastDecompressor FAST_DECOMPRESSOR;

    static{
        FAST_DECOMPRESSOR = LZ4Factory.fastestInstance().fastDecompressor();
    }

    public static byte[] fastDecompress(byte[] compressed, int decompressedLength) {
        byte[] restored = new byte[decompressedLength];
        FAST_DECOMPRESSOR.decompress(compressed, 0, restored, 0, decompressedLength);
        return restored;
    }

    public static byte[] old_decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[131_071];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        return output;
    }
}