package com.huhuanpu.codelabworkmanagerdemo.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.huhuanpu.codelabworkmanagerdemo.Constants;
import com.huhuanpu.codelabworkmanagerdemo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.huhuanpu.codelabworkmanagerdemo.Constants.CHANNEL_ID;
import static com.huhuanpu.codelabworkmanagerdemo.Constants.DELAY_TIME_MILLIS;

/**
 * Created by huhuanpu on 20-1-15
 */
final class WorkerUtils {
    private static final String TAG = WorkerUtils.class.getSimpleName();

    /**
     * Create a Notification as head-up if possible
     *
     * For this codelab, this is used to show a notification so that you know when different steps
     * of the background work chain are starting
     *
     * @param message Message shown on the notification
     * @param context Context need to create toast
     */
    static void makeStatusNotificationi(String message, Context context) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel chanel but only on api 26+, because
            // the this class is new and not in the support library
            CharSequence name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME;
            String description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Add the chanel
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(Constants.NOTIFICATION_TITLE)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[0]);
        // Show the notification
        NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build());
    }

    /**
     * Method for sleeping for a fixed about of time to emulate slower work
     */
    static void sleep() {
        try {
            Thread.sleep(DELAY_TIME_MILLIS, 0);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * Blurs the given Bitmap image
     * @param bitmap Image to blur
     * @param applicationContext Application context
     * @return Blurred bitmap image
     */
    @WorkerThread
    static Bitmap blurBitmap(@NonNull Bitmap bitmap,
                             @NonNull Context applicationContext) {
        RenderScript rsContext = null;
        try {

            // Create the output bitmap
            Bitmap output = Bitmap.createBitmap(
                    bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            // Blur the image
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG);
            Allocation inAlloc = Allocation.createFromBitmap(rsContext, bitmap);
            Allocation outAlloc = Allocation.createTyped(rsContext, inAlloc.getType());
            ScriptIntrinsicBlur theIntrinsic =
                    ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext));
            theIntrinsic.setRadius(10.f);
            theIntrinsic.setInput(inAlloc);
            theIntrinsic.forEach(outAlloc);
            outAlloc.copyTo(output);

            return output;
        } finally {
            if (rsContext != null) {
                rsContext.finish();
            }
        }
    }

    /**
     * Write bitmap to file and returns the Uri for the file
     * @param applicationContext Application context
     * @param bitmap Bitmap to write to file
     * @return Uri for the file with bitmap
     * @throws java.io.FileNotFoundException Throws if bitmap file cannot be found
     */
    static Uri writeBitmapToFile(@NonNull Context applicationContext, @NonNull Bitmap bitmap) throws FileNotFoundException {
        String name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString());
        File outputDir = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
        if(!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, name);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignore) {

                }
            }
        }
        return Uri.fromFile(outputFile);
    }

    private WorkerUtils(){
    }

}
