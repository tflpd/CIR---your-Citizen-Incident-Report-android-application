package com.inducesmile.citizenreportingtool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class IntroActivity extends AppCompatActivity {
    private ImageView mLogoImageView;
    private TextView mCaptureTextView;
    private TextView mDescribeTextView;
    private TextView mReportTextView;
    private boolean isUserLogedIn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setContentView(R.layout.intro_activity);

        mLogoImageView = findViewById(R.id.landmarkIntroLogo);

        if (isUserLogedIn){
            final Intent toHomeActivity = new Intent(getApplicationContext(), MainActivity.class);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Actions to do after 10 seconds
                    startActivity(toHomeActivity);
                    finish();
                }
            }, 2500);
        }else{
            final Intent toLoginActivity = new Intent(getApplicationContext(), LogInActivity.class);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Actions to do after 10 seconds
                    startActivity(toLoginActivity);
                    finish();
                }
            }, 2500);
        }
    }
}
