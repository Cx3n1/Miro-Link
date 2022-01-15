package miro.link.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Compressor {

    public static LZ4FastDecompressor fastDecompressor = LZ4Factory.fastestInstance().fastDecompressor();
    public static LZ4SafeDecompressor safeDecompressor = LZ4Factory.fastestInstance().safeDecompressor();

//    public static void main(String[] args) throws AWTException, IOException, DataFormatException {
//        Robot robot = new Robot();
//        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//        BufferedImage bufferedImage = robot.createScreenCapture(screenSize);
//
//        File fout = new File("saved.jpg");
//        ImageIO.write(bufferedImage, "jpg", fout);
//        File fout1 = new File("saved.png");
//        ImageIO.write(bufferedImage, "png", fout1);
//        File fout2 = new File("saved.bmp");
//        ImageIO.write(bufferedImage, "bmp", fout2);
//
//
//        //TODO optimisation: try to send jpg files and if BitmapFactory can decode pnjs into bitmap that
//        // would reduce time significantly
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        long currS = System.currentTimeMillis();
//        ImageIO.write(bufferedImage, "jpg", baos);
//        byte[] bytes1 = baos.toByteArray();
//        byte[] coomp = compress(bytes1);
//        long currE = System.currentTimeMillis();
//
//
//        byte[] bytes = Utils.convertBufferedImageToByteArray(bufferedImage);
//
//        byte[] Scoomp = compress(bytes);
///*        for (int i = 0; i < bytes.length; i++) {
//            if(i%30 == 0) System.out.println();
//
//            System.out.printf("%4d ", bytes[i]);
//        }
//
//        System.out.println("******************************************");
//
//        for (int i = 0; i < bytes1.length; i++) {
//            if(i%30 == 0) System.out.println();
//
//            System.out.printf("%4d ",bytes1[i]);
//        }*/
//
//        System.out.println("\n*******");
//        System.out.println(bytes.length);
//        System.out.println(bytes1.length);
//        System.out.println(coomp.length);
//        System.out.println(Scoomp.length);
//        System.out.println("current" + (currE - currS)/1000.0 + "s" );
//
//        /*long currS = System.currentTimeMillis();
//        byte[] curr =  compress(bytes);
//        long currE = System.currentTimeMillis();
//
//        long oldS = System.currentTimeMillis();
//        byte[] old = old_compress(bytes);
//        long oldE = System.currentTimeMillis();
//
//
//        long currDS = System.currentTimeMillis();
//        fastDecompress(curr, bytes.length);
//        long currDE = System.currentTimeMillis();
//
//        long currUDS = System.currentTimeMillis();
//        unknownDecompress(curr);
//        long currUDE = System.currentTimeMillis();
//
//        long oldDS = System.currentTimeMillis();
//        decompress(old);
//        long oldDE = System.currentTimeMillis();
//
//
//
//        System.out.println("current\nLen: " + curr.length + "\nCompTime: " + (currE - currS)/1000.0 + "s" );
//        System.out.println("Decomp time:" + (currDE - currDS)/1000.0 + "s" );
//        System.out.println("Decomp2 time:" + (currUDE - currUDS)/1000.0 + "s" );
//
//        System.out.println("old\nLen: " + old.length + "\nTime: " + (oldE - oldS)/1000.0 + "s" );
//        System.out.println("Decomp time:" + (oldDE - oldDS)/1000.0 + "s" );*/
//    }

    public static byte[] compress(byte[] data) throws IOException {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4Compressor compressor = factory.fastCompressor();

        final int decompressedLength = data.length;

        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);

        byte[] result = new byte[compressedLength];


        System.arraycopy(compressed, 0, result, 0, compressedLength);

        return result;
    }

    /**
     * quickly decompresses array compressed using LZ4
     * algorithm provided that decompressed length is known
     * @param compressed - compressed array compressed by LZ4 algorithm
     * @param decompressedLength - length of original decompressed array
     * @return restored - decompressed array
     * @throws IOException
     */
    public static byte[] fastDecompress(byte[] compressed, int decompressedLength) throws IOException {
        byte[] restored = new byte[decompressedLength];

        int compressedLength = fastDecompressor.decompress(compressed, 0, restored, 0, decompressedLength);

        return restored;
    }


    public static byte[] old_compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[131_071];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[131_071];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * decompresses array which is compressed using LZ4 algorithm without
     * knowing length of original decompressed array at cost of speed
     *
     * Disclaimer: only decompresses arrays with the length less than 8,000,000!
     * @param compressed - array compressed using LZ4 algorithm
     * @return restored - decompressed array
     */
    public static byte[] safeDecompress(byte[] compressed){
        byte[] buffer = new byte[8_000_000];

        int decompressedLength = safeDecompressor.decompress(compressed, 0, compressed.length, buffer, 0);

        byte[] restored = new byte[decompressedLength];

        System.arraycopy(buffer, 0, restored, 0, decompressedLength);

        return restored;
    }

}