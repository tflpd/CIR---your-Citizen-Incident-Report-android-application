package com.inducesmile.citizenreportingtool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

public class MyReportsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    //String[] items = {"item 0","item 1","item 2","item 3","item 4","item 5","item 6","item 7","item 8","item 9",};
    //IncidentReport[] incidentReports;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.my_reports_activity);

        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("incidentReportsPrefs", MODE_PRIVATE);

        // Get saved string data in it.
        String incidentReportsJsonString = sharedPreferences.getString("incidentReportsObjs", "");

        // Create Gson object and translate the json string to related java object array.
        Gson gson = new Gson();
        IncidentReport incidentReportsArray[] = gson.fromJson(incidentReportsJsonString, IncidentReport[].class);

        if (incidentReportsArray != null){
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new ReportsAdaptor(this, incidentReportsArray));
        }else{
            Toast.makeText(getApplicationContext(), "No reports yet made!", Toast.LENGTH_LONG).show();
        }
    }

    public void returnToMenu(View view) { finish();
    }

    // Make the app's reports page fullscreen
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
}
