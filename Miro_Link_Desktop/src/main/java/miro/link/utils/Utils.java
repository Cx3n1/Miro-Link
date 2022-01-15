package miro.link.utils;


import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miro.link.db.DatabaseUpdater;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(StatusWatchman.LOG_CONSOLE_TIME_FORMAT);
        return sdf.format(cal.getTime());
    }


    //****ServerRunnable Utility****\\

    //***Main Loops***\\
    private static void mainLoop() throws AWTException, IOException {

        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();

        log.info("Mirroring data transfer initiated");

        //send verification message
        //we send current port as a first message to verify that connection is right
        StatusWatchman.SEND_STREAM.writeInt(StatusWatchman.getPORT());

        //DatabaseUpdater.startUpdater();

        while (!StatusWatchman.SERVER_SHOULD_STOP) {
            takeScreenshotAndSendItToClient(screenSize, robot);
        }

        //DatabaseUpdater.stopUpdater();

        log.info("Mirroring data transfer stopped");

        StatusWatchman.SERVER_SHOULD_STOP = false;
    }

    /**
     * Used only for debugging purposes
     */
    private static void dbg_TimedMainLoop() throws Exception {
        long cycles = 0; //TODO do something here

        long timeElapsed = 0;
        long screenshot = 0;
        long conversion = 0;
        long compression = 0;
        long writing = 0;
        long len = 0;

        long start;
        long tmpScreenshot;
        long tmpConversion;
        long tmpCompression;
        long tmpWriting;


        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();
        BufferedImage bufferedImage;

        while (!StatusWatchman.SERVER_SHOULD_STOP) {
            cycles++;

            start = System.currentTimeMillis();

            //*** ScreenShot ***\\
            bufferedImage = robot.createScreenCapture(screenSize);

            tmpScreenshot = System.currentTimeMillis();

            //*** Conversion ***\\
            byte[] bytes = convertBufferedImageToByteArray(bufferedImage);

            tmpConversion = System.currentTimeMillis();

            //*** Compression ***\\
            byte[] compressedData = Compressor.compress(bytes);
            //byte[] compressedData = Compressor.old_compress(bytes);

            tmpCompression = System.currentTimeMillis();

            //*** Data Transfer ***\\
            StatusWatchman.SEND_STREAM.writeInt(bytes.length);
            StatusWatchman.SEND_STREAM.writeInt(compressedData.length);
            StatusWatchman.SEND_STREAM.write(compressedData);

            tmpWriting = System.currentTimeMillis();

            //*** debug data recording ***\\
            len += compressedData.length;
            timeElapsed += (tmpWriting - start) / 1000;
            screenshot += (tmpScreenshot - start) / 1000;
            conversion += (tmpConversion - tmpScreenshot) / 1000;
            compression += (tmpCompression - tmpConversion) / 1000;
            writing += (tmpWriting - tmpCompression) / 1000;
        }


        log.debug("Average size: " + len / cycles);
        log.debug("Average time elapsed: " + timeElapsed / cycles + "s");
        log.debug("Average screenshot: " + screenshot / cycles + "s");
        log.debug("Average conversion: " + conversion / cycles + "s");
        log.debug("Average compression: " + compression / cycles + "s");
        log.debug("Average writing: " + writing / cycles + "s");
    }


    //***Other Utility***\\
    public static void createServerSocketOnFreePortAndNotifyStatusWatchmanAboutIt() throws IOException {
        StatusWatchman.SERVER_SOCKET = new ServerSocket(0);

        log.info("Server Launched");
        log.debug("Server address: " + Inet4Address.getLocalHost().getHostAddress());
        log.debug("Server port: " + StatusWatchman.SERVER_SOCKET.getLocalPort());

        StatusWatchman.reportServerStart(
                StatusWatchman.SERVER_SOCKET.getLocalPort(),
                Inet4Address.getLocalHost().getHostAddress()
        );
    }

    public static Socket establishConnectionWithClient() throws IOException {
        log.info("Server is waiting for connection");

        Socket accept = StatusWatchman.SERVER_SOCKET.accept();
        StatusWatchman.reportClientConnected(true);

        log.info("Server established connection");
        log.debug("Client address: " + accept.getInetAddress().toString());

        return accept;
    }

    public static void getOutputStreamToClientAndStoreItInStatusWatchman(Socket accept) throws IOException {
        assert accept != null;
        StatusWatchman.SEND_STREAM = new DataOutputStream(accept.getOutputStream());
    }

    public static void startMainLoopAccordingToDebugModeDeclaredInStatusWatchman() throws Exception {
        if (StatusWatchman.DEBUG_MODE)
            dbg_TimedMainLoop();
        else
            mainLoop();
    }

    public static void takeScreenshotAndSendItToClient(Rectangle screenSize, Robot robot) throws IOException {
        Pair<byte[], Integer> dataLengthPair =
                createPairOfCompressedByteArrayOfScreenshotAndLengthOfDecompressedArray(screenSize, robot);
        sendCompressedByteArrayAndDecompressedLengthAndCompressedLengthThroughSendStream(dataLengthPair);
    }

    public static void sendCompressedByteArrayAndDecompressedLengthAndCompressedLengthThroughSendStream(Pair<byte[], Integer> dataLengthPair) throws IOException {
        //Decompressed Length
        StatusWatchman.SEND_STREAM.writeInt(dataLengthPair.getValue());
        //Compressed Length
        StatusWatchman.SEND_STREAM.writeInt(dataLengthPair.getKey().length);
        //Compressed array
        StatusWatchman.SEND_STREAM.write(dataLengthPair.getKey());
    }

    public static Pair<byte[], Integer> createPairOfCompressedByteArrayOfScreenshotAndLengthOfDecompressedArray(Rectangle screenSize, Robot robot) throws IOException {
        BufferedImage bufferedImage = robot.createScreenCapture(screenSize);

        byte[] bytes = Utils.convertBufferedImageToByteArray(bufferedImage);

        byte[] compressed = Compressor.compress(bytes);

        return new Pair<>(compressed, bytes.length);
    }

    public static byte[] createCompressedByteArrayRepresentationOfScreenshot(Rectangle screenSize, Robot robot) throws IOException {
        BufferedImage bufferedImage = robot.createScreenCapture(screenSize);

        byte[] bytes = convertBufferedImageToByteArray(bufferedImage);

        return Compressor.compress(bytes);
    }

    public static byte[] convertBufferedImageToByteArray(BufferedImage bufferedImage) {
        byte[] bitmap = util_FillInTheBodyOfBitmapFromBufferedImage(bufferedImage);
        util_AddHeaderToBitmap(bitmap, bufferedImage.getWidth(), bufferedImage.getHeight());
        return bitmap;
    }

    public static byte[] convertBufferedImageToByteArrayUsingStream(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "bmp", baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }

    private static void util_AddHeaderToBitmap(byte[] bitmap, int width, int height) {
        // -- FILE HEADER -- //

        // bitmap signature
        bitmap[0] = 'B';
        bitmap[1] = 'M';

        // file size
        byte[] sizeBytes = util_DismemberIntToBytes(bitmap.length);
        bitmap[2] = sizeBytes[0];
        bitmap[3] = sizeBytes[1];
        bitmap[4] = sizeBytes[2];
        bitmap[5] = sizeBytes[3];

        // reserved field (in hex. 00 00 00 00)
        for (int i = 6; i < 10; i++) bitmap[i] = 0;

        // File offset to Raster Data
        bitmap[10] = 54;
        for (int i = 11; i < 14; i++) bitmap[i] = 0;

        // -- BITMAP HEADER -- //

        // header size
        bitmap[14] = 40;
        for (int i = 15; i < 18; i++) bitmap[i] = 0;

        // width of the image
        byte[] widthHeightBytes = util_DismemberIntToBytes(width);
        bitmap[18] = widthHeightBytes[0];
        bitmap[19] = widthHeightBytes[1];
        bitmap[20] = widthHeightBytes[2];
        bitmap[21] = widthHeightBytes[3];

        // height of the image
        widthHeightBytes = util_DismemberIntToBytes(height);
        bitmap[22] = widthHeightBytes[0];
        bitmap[23] = widthHeightBytes[1];
        bitmap[24] = widthHeightBytes[2];
        bitmap[25] = widthHeightBytes[3];

        // number of panes default is 1
        bitmap[26] = 1;
        bitmap[27] = 0;

        // number of bits per pixel
        //bit count 1 -> numColors = 2, 4 -> numColors = 16, 8 -> numColors = 256, 16 -> numColors ~ 65536, 24 -> numColors = 16M
        bitmap[28] = 24;
        bitmap[29] = 0;

        // compression method (no compression)
        for (int i = 30; i < 34; i++) bitmap[i] = 0;

        // size of compressed image size (0 if no compression)
        bitmap[34] = 0;
        bitmap[35] = 0;
        bitmap[36] = 0;
        bitmap[37] = 0;

        // horizontal resolution of the image - pixels per meter (no need to write this)
        bitmap[38] = 0;
        bitmap[39] = 0;
        bitmap[40] = 0;
        bitmap[41] = 0;

        // vertical resolution of the image - pixels per meter (no need to write this)
        bitmap[42] = 0;
        bitmap[43] = 0;
        bitmap[44] = 0;
        bitmap[45] = 0;

        // color used - 0 (no need to write this)
        for (int i = 46; i < 50; i++) bitmap[i] = 0;

        // number of important colors - 0 (which means all)
        for (int i = 50; i < 54; i++) bitmap[i] = 0;

    }

    private static byte[] util_DismemberIntToBytes(int size) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (size & 0b11111111); //bitwise and to get lower byte from 4 byte int
        bytes[1] = (byte) (size >> 8 & 0b11111111); //shift left and then bitwise and to get second byte in 4 byte int
        bytes[2] = (byte) (size >> 16 & 0b11111111); //you know what I'm doing
        bytes[3] = (byte) (size >> 24 & 0b11111111);//btw .bmp format doesn't care about sign so 11111111 is 255 not -1
        return bytes;
    }

    private static byte[] util_FillInTheBodyOfBitmapFromBufferedImage(BufferedImage bufferedImage) {
        int rowLength = (int) Math.ceil(bufferedImage.getWidth() * 3 * 8 / 32.0) * 4;
        byte[] bitmap = new byte[bufferedImage.getHeight() * rowLength + 54];
        int pixelLoc = 54; //starting pixel location
        int rowLoc = 0; //row location providing proper offset for each row of pixels (bmp format specific thingie)
        int rgb;

        for (int i = bufferedImage.getHeight() - 1; 0 <= i; i--) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {

                if (rowLoc + 3 > rowLength) {
                    for (int k = 0; k < rowLength - rowLoc; k++)
                        pixelLoc++;
                    rowLoc = 0;
                } else if (rowLoc + 3 == rowLength)
                    rowLoc = 0;

                rgb = bufferedImage.getRGB(j, i); //writing in opposite manner that's how it is

                bitmap[pixelLoc++] = (byte) (rgb & 0xFF); //blue
                bitmap[pixelLoc++] = (byte) ((rgb >> 8) & 0xFF); //green
                bitmap[pixelLoc++] = (byte) ((rgb >> 16) & 0xFF); //red

                rowLoc += 3;
            }
        }

        return bitmap;
    }
}
