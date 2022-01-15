package com.example.my_first_app.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.my_first_app.runnables.ClientRunnable;

import java.io.DataInputStream;
import java.io.IOException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static int getNavigationBarHeight(Activity activity) {
        Rect rectangle = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels - (rectangle.top + rectangle.height());
    }

    public static void getDeviceScreenHeightAndWidthAndStoreItInStatusWatchman(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        StatusWatchman.DEVICE_WIDTH = dm.widthPixels;
        StatusWatchman.DEVICE_HEIGHT = dm.heightPixels;
        StatusWatchman.DEVICE_NAVBAR_HEIGHT = Utils.getNavigationBarHeight(activity);
    }

    public static boolean validateEnteredPortAndIPAndStoreThemInStatusWatchmanIfValid(String ip, String portAsString) {

        Log.d("debug", "inputted [ip:" + ip + "], [port:" + portAsString + "]");


        Log.i("info", "Validating ip");
        if(ipIsInvalid(ip)){
            Log.w("warning", "Invalid IP");
            return false;
        }
        //IP is valid (Well at least it's in right format :p)
        Log.i("info", "IP validated");


        Log.i("info", "Validating port");
        if(portIsInvalid(portAsString)){
            Log.w("warning", "Invalid Port");
            return false;
        }
        //Port is in valid format
        Log.i("info", "Port validated");


        //storing ip and port in status watchman
        StatusWatchman.IP = ip;
        StatusWatchman.PORT = Integer.parseInt(portAsString);;

        Log.i("info", "Validation successful");
        return true;
    }



    public static void createNewThreadLaunchItAndNotifyStatusWatchman() {
        StatusWatchman.RECEIVER_THREAD = new Thread(new ClientRunnable());
        StatusWatchman.RECEIVER_THREAD.start();
        StatusWatchman.reportReceiverThreadStart();
    }



    public static void mainLoop(DataInputStream incomingStream) throws IOException {
        //needed variables
        int compressedLength;
        int decompressedLength;

        byte[] image;
        byte[] compressedArray = new byte[0];
        Bitmap tmpBitmap;

        //since device is in landscape mode height of the device will be width of the image and vice-versa
        final int imageWidth = StatusWatchman.DEVICE_HEIGHT + StatusWatchman.DEVICE_NAVBAR_HEIGHT;
        final int imageHeight = StatusWatchman.DEVICE_WIDTH;

        Log.d("debug", "Starting main loop");

        try {
            while (!StatusWatchman.shouldConnectionBeTerminated()) {
                //**getting lengths
                decompressedLength = incomingStream.readInt();
                compressedLength = incomingStream.readInt();

                //**initialise new array if needed (here should go assignNewArrayIfNeeded
                // but it seems inefficient :p
                if (compressedArray.length < compressedLength) {
                    compressedArray = new byte[compressedLength];
                }

                //**reading compressed array
                incomingStream.readFully(compressedArray, 0, compressedLength);

                //**decompressing array
                image = Decompressor.fastDecompress(compressedArray, decompressedLength);

                //**decoding array
                tmpBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

                //**scaling image to fit screen
                tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap, imageWidth, imageHeight, false);

                //**notifying status watchman
                StatusWatchman.reportMirroredImageUpdate(tmpBitmap);
            }
        } catch(IOException e){
            //it's possible that when user terminates mirroring right before reading stream
            if(!StatusWatchman.shouldConnectionBeTerminated()) {
                throw e;
            }
        }

        Log.d("debug", "Main loop ended");
    }

    /**
     * only creates new array when needed (eg if newly received data wont fit in it)
     * data length shouldn't differ too much so carrying extra bytes won't affect performance
     * or at least I hope so :p
     * @return
     */
    private static byte[] assignNewArrayIfNeeded(byte[] array, int newLength) {
        if(array.length < newLength) {
            return new byte[newLength];
        }
        return array;
    }



    //**private utility**\\
    private static boolean ipIsInvalid(String ip) {
        String[] ipComponents = ip.split("\\.");
        //all of the dot separated values in ipv4
        //should be parsable 8bit integers
        // xxx.xxx.xxx.xxx also there should be 4 such values
        if (ipComponents.length != 4) return true;
        try {
            for (String s : ipComponents)
                if (Integer.parseInt(s) > 255 || Integer.parseInt(s) < 0)
                    return true;
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private static boolean portIsInvalid(String portAsString) {
        int port;

        //port should be number in [0; 65535]
        if (portAsString.isEmpty()) return true;
        try {
            port = Integer.parseInt(portAsString);
        } catch (Exception e) {
            return true;
        }
        if (port < 0 || port > 65535) return true;

        return false;
    }
}
