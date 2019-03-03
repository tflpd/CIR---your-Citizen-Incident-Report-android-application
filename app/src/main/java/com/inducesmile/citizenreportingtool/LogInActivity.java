package com.inducesmile.citizenreportingtool;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class LogInActivity extends AppCompatActivity {

    boolean userPasswordCorrect = true;

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

        setContentView(R.layout.log_in_activity);
    }

    public void toHomeActivity(View view) {
        if (userPasswordCorrect){
            final Intent toHomeActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(toHomeActivity);
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "Email - password combination incorrect, please try again", Toast.LENGTH_LONG).show();
        }
    }

    public void toSignUpView(View view) {
        final Intent toSignUpActivity = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(toSignUpActivity);
    }
}
