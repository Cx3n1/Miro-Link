package miro.link.utils;

import javafx.util.Pair;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    static ServerSocket serverSocket;
    static String address;

    @BeforeAll
    @SneakyThrows
    static void setUp() {
        serverSocket = new ServerSocket(2424);
        address = Inet4Address.getLocalHost().getHostAddress();
    }

    @AfterAll
    @SneakyThrows
    static void tearDown() {
        serverSocket.close();
    }

    @Test
    void createServerSocketOnFreePortAndNotifyStatusWatchmanAboutIt_DoesItCreateServerSocketAndNotifyWatchman() throws IOException {
        Utils.createServerSocketOnFreePortAndNotifyStatusWatchmanAboutIt();
        assertAll(
                () -> assertNotNull(StatusWatchman.SERVER_SOCKET),
                () -> assertTrue(StatusWatchman.isServerRunning()),
                () -> assertNotNull(StatusWatchman.getIP()),
                () -> assertNotEquals(0, StatusWatchman.getPORT())
        );
    }

    @Test
    void establishConnectionWithClient_DoesItConnect() throws IOException {
        //Setup
        StatusWatchman.SERVER_SOCKET = serverSocket;
        StatusWatchman.reportServerStart(2424, address);
        Socket client = new Socket(address, 2424);

        //tests
        assertTimeout(Duration.ofMillis(100), Utils::establishConnectionWithClient);
        assertTrue(StatusWatchman.isClientConnected());

        client.close();
    }

    @Test
    void getOutputStreamToClientAndStoreItInStatusWatchman_DoesItStoreDataOutputStreamCorrectly() throws IOException {

        try (Socket accept = new Socket(address, 2424);) {
            Utils.getOutputStreamToClientAndStoreItInStatusWatchman(accept);
        }

        assertNotNull(StatusWatchman.SEND_STREAM);
    }

    @Test
    void sendCompressedByteArrayAndDecompressedLengthAndCompressedLengthThroughSendStream_DoesClientReceiveSameInfo() throws IOException {
        Socket client = new Socket(address, 2424);
        Socket accept = serverSocket.accept();

        StatusWatchman.SEND_STREAM = new DataOutputStream(accept.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(client.getInputStream());

        Random random = new Random(new Date().getTime());

        int randSize = random.nextInt(1_000, 100_000);
        byte[] testByteArr = new byte[randSize];
        random.nextBytes(testByteArr);

        int testLength = Math.abs(random.nextInt());
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Utils.sendCompressedByteArrayAndDecompressedLengthAndCompressedLengthThroughSendStream(new Pair<>(testByteArr, testLength));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }).start();


        int receivedLen = dataInputStream.readInt();
        int receivedArrLen = dataInputStream.readInt();

        byte[] receivedArray = new byte[randSize];
        dataInputStream.readFully(receivedArray);

        assertAll(
                () -> assertEquals(testLength, receivedLen),
                () -> assertEquals(testByteArr.length, receivedArrLen),
                () -> assertArrayEquals(testByteArr, receivedArray)
        );

        client.close();
        //renew used server socket
        serverSocket.close();
        serverSocket = new ServerSocket(2424);
    }

    @Test
    void createPairOfCompressedByteArrayOfScreenshotAndLengthOfDecompressedArray_DoesItCreateNonEmptyActuallyCompressedResult() throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();

        Pair<byte[], Integer> pair = Utils.createPairOfCompressedByteArrayOfScreenshotAndLengthOfDecompressedArray(screenSize, robot);

        assertAll(
                () -> assertNotNull(pair),                                 //doesn't return null
                () -> assertNotNull(pair.getKey()),                        //byte array is not null
                () -> assertNotEquals(0, pair.getValue()),                 //decompressed length is not 0
                () -> assertNotEquals(0, pair.getKey().length),            //compressed length is not 0
                () -> assertTrue((pair.getKey().length < pair.getValue())),//compressed array is smaller than decompressed
                () -> assertEquals(Compressor.safeDecompress(pair.getKey()).length, pair.getValue()) //returned decompressed length is actually length of decompressed array
        );
    }

    @Test
    void createCompressedByteArrayRepresentationOfScreenshot() throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();

        byte[] arr = Utils.createCompressedByteArrayRepresentationOfScreenshot(screenSize, robot);

        assertAll(
                () -> assertNotNull(arr),                                              //doesn't return null
                () -> assertNotEquals(0, arr.length),                                  //length is not 0
                () -> assertTrue((arr.length < Compressor.safeDecompress(arr).length)) //is actually compressed
        );
    }

    @Test
    void convertBufferedImageToByteArray_DoesItReturnSameAsIfUsingStreams() throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();

        BufferedImage screenCapture = robot.createScreenCapture(screenSize);

        byte[] fromStream = Utils.convertBufferedImageToByteArrayUsingStream(screenCapture);

        //removing "garbage" values that might differ (these values don't contribute in our case)
        for (int i = 34; i < 54 ; i++) {
            fromStream[i] = 0;
        }

        assertArrayEquals(fromStream, Utils.convertBufferedImageToByteArray(screenCapture));
    }

}