package miro.link.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

class CompressorTest {

    private static byte[] testByteArr;
    private static final Random random = new Random(new Date().getTime());

    @BeforeAll
    static void setUp(){
        int randSize = random.nextInt(1_000, 8_000_000);
        testByteArr = new byte[randSize];
        random.nextBytes(testByteArr);
    }


    @Test
    void byteArrayDoesntChangeAfterCompressionAndDecompression() throws IOException, DataFormatException {
        assertArrayEquals(testByteArr,Compressor.fastDecompress(Compressor.compress(testByteArr), testByteArr.length));
    }

    @Test
    void compressionIsFast() {
        assertTimeout(Duration.ofMillis(200), ()->Compressor.compress(testByteArr));
    }

    @Test
    void fastDecompressIsFast() throws IOException {
        byte[] compressed = Compressor.compress(testByteArr);
        assertTimeout(Duration.ofMillis(20), ()->Compressor.fastDecompress(compressed, testByteArr.length));
    }

    @Test
    void decompressionIsConsistentRegardlessOfDecompressor() throws IOException {
        byte[] compressed = Compressor.compress(testByteArr);
        assertArrayEquals(Compressor.fastDecompress(compressed, testByteArr.length), Compressor.safeDecompress(compressed));
    }

}