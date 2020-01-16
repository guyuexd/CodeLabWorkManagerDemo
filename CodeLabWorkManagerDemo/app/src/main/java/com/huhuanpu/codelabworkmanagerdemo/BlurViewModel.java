package com.huhuanpu.codelabworkmanagerdemo;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.huhuanpu.codelabworkmanagerdemo.workers.BlurWorker;
import com.huhuanpu.codelabworkmanagerdemo.workers.CleanupWorker;
import com.huhuanpu.codelabworkmanagerdemo.workers.SaveImageToFileWorker;

import java.util.List;

import static com.huhuanpu.codelabworkmanagerdemo.Constants.TAG_OUTPUT;

/**
 * Created by huhuanpu on 20-1-15
 */
public class BlurViewModel extends AndroidViewModel {

    private WorkManager mWorkManager;
    private Uri mImageUri, mOutputUri;
    private LiveData<List<WorkInfo>> mSavedWorkInfo;

    public BlurViewModel(@NonNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);
        mSavedWorkInfo = mWorkManager.getWorkInfosByTagLiveData(TAG_OUTPUT);
    }

    void applyBlur(int blurLevel) {
        // Add workrequest to CleanUp temporary images
        WorkContinuation workContinuation = mWorkManager.beginUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker.class));

        // Add WorkRequests to blur the image the number of times requested
        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if ( i == 0 ) {
                blurBuilder.setInputData(createInputDataForUri());
            }

            workContinuation = workContinuation.then(blurBuilder.build());
        }

        // Create charging constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();

        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .setConstraints(constraints)
                .addTag(TAG_OUTPUT)
                .build();

        workContinuation = workContinuation.then(save);

        workContinuation.enqueue();

    }


    /**
     * Cancel work using the work's unique name
     */
    void cancelWork() {
        mWorkManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME);
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if(mImageUri != null) {
            builder.putString(Constants.KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    void setOutputUri(String outputImageUri) {
        mOutputUri = uriOrNull(outputImageUri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    Uri getOutputUri() { return mOutputUri; }

    LiveData<List<WorkInfo>> getOutputWorkInfo() {
        return mSavedWorkInfo;
    }
}
