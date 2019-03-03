package com.inducesmile.citizenreportingtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class CreateReportActivity extends AppCompatActivity {

    private ImageView mImageView;
    private String mImagePath;
    private EditText mReportText;
    private SeekBar mEmergencyLevel;
    private FusedLocationProviderClient client;


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

        setContentView(R.layout.create_report_activity);

        Intent activityThatCalled = getIntent();

        mImagePath = activityThatCalled.getExtras().getString("pathToImage");

        //TextView mTextView = (TextView) findViewById(R.id.emergencyLevelText);
        mImageView = (ImageView) findViewById(R.id.imageThumbnail);

        mReportText = (EditText) findViewById(R.id.reportText);
        mReportText.setScroller(new Scroller(this));
        mReportText.setMaxLines(7);
        mReportText.setVerticalScrollBarEnabled(true);
        mReportText.setMovementMethod(new ScrollingMovementMethod());

        //mTextView.append(" " + previousActivity);

        File imageFile = new File(mImagePath);
        Picasso.get().load(imageFile).into(mImageView);

        mEmergencyLevel = findViewById(R.id.emergencyLevelBar);

        requestPermission();

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED){
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                    Toast.makeText(this, "Citizen Reporting Tool requires access to gps", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }*/
        client.getLastLocation().addOnSuccessListener(CreateReportActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    Toast.makeText(getApplicationContext(), "Coordinates Loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Toast.makeText(getApplicationContext(), "Coordinates Loaded", Toast.LENGTH_SHORT).show();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });
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

    public void ReturnToPreviewView(View view) {
        finish();
    }


    public void sendIncident(View view) {

        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("incidentReportsPrefs", MODE_PRIVATE);

        // Get saved string data in it.
        String incidentReportsJsonString = sharedPreferences.getString("incidentReportsObjs", "");

        // Create Gson object and translate the json string to related java object array.
        Gson gson = new Gson();
        IncidentReport incidentReportsArray[] = gson.fromJson(incidentReportsJsonString, IncidentReport[].class);

        List<IncidentReport> initialIncidentReportList;

        // Loop the UserInfoDTO array and print each UserInfoDTO data in android monitor as debug log.
        if (incidentReportsArray == null){
            initialIncidentReportList = initIncidentReportList(mReportText.getText().toString(), mImagePath, mEmergencyLevel.getProgress());
        }else{
            initialIncidentReportList = addToIncidentReportList(mReportText.getText().toString(), mImagePath, mEmergencyLevel.getProgress(),new ArrayList<IncidentReport>(Arrays.asList(incidentReportsArray)));
        }

        // Get java object list json format string.
        String initialIncidentReportListJsonString = gson.toJson(initialIncidentReportList);

        // Create SharedPreferences object.
            /*Context ctx = getApplicationContext();
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREFERENCES_FILE_USER_INFO_LIST, MODE_PRIVATE);*/

        // Put the json format string to SharedPreferences object.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("incidentReportsObjs", initialIncidentReportListJsonString);
        editor.commit();

        /*Integer length = initialIncidentReportList.size();
        String sLength = length.toString();

        String toToast = "Incident Reported Successfully" + sLength;*/

        Toast.makeText(getApplicationContext(), "Incident Reported Successfully", Toast.LENGTH_SHORT).show();
        ToHomeView();

        /*String descriptionString = mReportText.getText().toString();
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        File imageFile = new File(mImagePath);

        Uri imageUri = Uri.fromFile(imageFile);

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(imageUri)),
                        imageFile
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);*/

        /*Integer user = 1;
        RequestBody user_id =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, user);

        Integer emergency = mEmergencyLevel.getProgress();
        RequestBody emergencyLevel =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, emergency);*/

        /*Call<ResponseBody> call = service.upload(description, body);*/

        /*call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });*/



/*      ////////////////////Deprecated Code//////////////////////
        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().upload();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String successfulResponse = response.body().string();
                    Toast.makeText(getApplicationContext(), successfulResponse, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });*/
        /*Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("196.168.1.7:8000/myServer/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        Api client = retrofit.create(Api.class);

        Call<List<GitHubRepo>> call = client.reposForUser();

        call.enqueue(new Callback<List<GitHubRepo>>() {
            @Override
            public void onResponse(Call<List<GitHubRepo>> call, Response<List<GitHubRepo>> response) {
                Toast.makeText(getApplicationContext(), "YAS", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<List<GitHubRepo>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Damn", Toast.LENGTH_LONG).show();

            }
        });*/


    }

    public void ToHomeView() {
        final Intent backToHomeViewIntent = new Intent(getApplicationContext(), MainActivity.class);
        /*Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // As I am using LENGTH_LONG in Toast
                    startActivity(backToHomeViewIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();*/
        startActivity(backToHomeViewIntent);
        finish();
    }

    // Used to initialize a list of IncidentReport objects.
    private List<IncidentReport> initIncidentReportList(String description, String imagePath, Integer emergencyLevel)
    {
        List<IncidentReport> incidentReportList = new ArrayList<IncidentReport>();

        IncidentReport firstIncident = new IncidentReport(description, imagePath, emergencyLevel);

        incidentReportList.add(firstIncident);

        return incidentReportList;
    }

    // Used to add an incident to a list of IncidentReport objects.
    private List<IncidentReport> addToIncidentReportList(String description, String imagePath, Integer emergencyLevel, List<IncidentReport> incidentReportList)
    {
        IncidentReport newIncident = new IncidentReport(description, imagePath, emergencyLevel);

        incidentReportList.add(newIncident);

        return incidentReportList;
    }

}
