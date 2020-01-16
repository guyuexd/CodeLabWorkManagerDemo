package com.huhuanpu.codelabworkmanagerdemo.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.huhuanpu.codelabworkmanagerdemo.Constants;

import java.io.FileNotFoundException;

/**
 * Created by huhuanpu on 20-1-15
 */
public class BlurWorker extends Worker {

    private static final String TAG = BlurWorker.class.getSimpleName();

    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        WorkerUtils.makeStatusNotificationi("bluring image", applicationContext);
        WorkerUtils.sleep();

        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = applicationContext.getContentResolver();

            // Create a bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));

            // Blur the bitmap
            Bitmap output = WorkerUtils.blurBitmap(bitmap, applicationContext);

            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);

            // Return the output for the temp file
            Data outputData = new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, outputUri.toString()).build();

            // If there were no errors, return SUCCESS
            return Result.success(outputData);
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(TAG, "Failed to decode input stream", fileNotFoundException);
            throw new RuntimeException("Failed to decode input stream", fileNotFoundException);

        } catch (Throwable throwable) {

            // If there were errors, return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
