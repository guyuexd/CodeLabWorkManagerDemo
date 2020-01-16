package com.huhuanpu.codelabworkmanagerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by huhuanpu on 20-1-15
 */
public class BlurActivity  extends AppCompatActivity {

    private BlurViewModel mViewModel;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Button mGoButton, mCancelButton, mOutputButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blur);

        // Get the ViewModel
        mViewModel = new ViewModelProvider(this).get(BlurViewModel.class);

        // Get all of the Views
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mGoButton = findViewById(R.id.go_button);
        mOutputButton = findViewById(R.id.see_file_button);
        mCancelButton = findViewById(R.id.cancel_button);

        // ImageUri should store in ViewModel
        Intent intent = getIntent();
        String imageUri = intent.getStringExtra(Constants.KEY_IMAGE_URI);
        mViewModel.setImageUri(imageUri);
        if(mViewModel.getImageUri() != null) {
            Glide.with(this).load(mViewModel.getImageUri()).into(mImageView);
        }

        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.applyBlur(getBlurLevel());
            }
        });

        mOutputButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri currentUri = mViewModel.getOutputUri();
                if (currentUri != null) {
                    Intent actionView = new Intent(Intent.ACTION_VIEW, currentUri);
                    if(actionView.resolveActivity(getPackageManager()) != null) {
                        startActivity(actionView);
                    }
                }
            }
        });

        // Hookup the Cancel button
        mCancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mViewModel.cancelWork();
            }
        });

        // Show work status
        mViewModel.getOutputWorkInfo().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos == null || workInfos.isEmpty()) {
                    return;
                }
                WorkInfo workInfo = workInfos.get(0);
                boolean isFinished = workInfo.getState().isFinished();
                if (!isFinished) {
                    showWorkInProgress();
                } else {
                    showWorkFinished();

                    Data outputData = workInfo.getOutputData();
                    String outputImageUri = outputData.getString(Constants.KEY_IMAGE_URI);
                    if (!TextUtils.isEmpty(outputImageUri)) {
                        mViewModel.setOutputUri(outputImageUri);
                        mOutputButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private void showWorkInProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
        mGoButton.setVisibility(View.GONE);
        mOutputButton.setVisibility(View.GONE);
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private void showWorkFinished() {
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mGoButton.setVisibility(View.VISIBLE);
    }

    /**
     * Get the blur level from the radio button as an integer
     * @return Integer representing the amount of times to blur the image
     */
    private int getBlurLevel() {
        RadioGroup radioGroup = findViewById(R.id.radio_blur_group);

        switch(radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_blur_lv_1:
                return 1;
            case R.id.radio_blur_lv_2:
                return 2;
            case R.id.radio_blur_lv_3:
                return 3;
        }

        return 1;
    }
}
