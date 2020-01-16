package com.huhuanpu.codelabworkmanagerdemo.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.huhuanpu.codelabworkmanagerdemo.Constants;

import java.io.File;

/**
 * Created by huhuanpu on 20-1-15
 */
public class CleanupWorker extends Worker {
    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static final String TAG = CleanupWorker.class.getSimpleName();


    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        WorkerUtils.makeStatusNotificationi("Cleaning cache", applicationContext);
        WorkerUtils.sleep();

        try {
            File outputDirectory = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
            if (outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();
                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.e(TAG,String.format("Delete %s - %s", name, deleted));
                        }
                    }
                }
            }
            return Result.success();
        } catch (Exception exception) {
            Log.e(TAG,"Error cleaning up ", exception);
            return Result.failure();
        }
    }
}
