package com.inducesmile.citizenreportingtool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class PhotoPreviewActivity extends AppCompatActivity {
    // I isws Activity sketo
    //private TextureView mTextureView;
    private ImageView mImageView;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setContentView(R.layout.photo_preview_activity);
        Intent activityThatCalled = getIntent();

        mImagePath = activityThatCalled.getExtras().getString("pathToImage");

        //TextView mTextView = (TextView) findViewById(R.id.textView);
        mImageView = (ImageView) findViewById(R.id.imageView);

        //mTextView.append(" " + previousActivity);

        File imageFile = new File(mImagePath);
        Picasso.get().load(imageFile).into(mImageView);

    }

    // Make the app's image preview page fullscreen
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        View decorView = getWindow().getDecorView();
        if (hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    public void ReturnToHomeView(View view) {
        finish();
    }

    public void ForwardToReportView(View view) {
        Intent sendImageForReportIntent = new Intent(getApplicationContext(), CreateReportActivity.class);
        sendImageForReportIntent.putExtra("pathToImage", mImagePath);
        startActivity(sendImageForReportIntent);
    }
}
