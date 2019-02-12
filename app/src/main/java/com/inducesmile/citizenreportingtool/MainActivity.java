 package com.inducesmile.citizenreportingtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

 public class MainActivity extends AppCompatActivity {

     public static final String CAMERA_FRONT = "1";
     public static final String CAMERA_BACK = "0";

     private String cameraId = CAMERA_BACK;
     private boolean isFlashSupported;
     private boolean isTorchOn;
     ImageButton flashButton;


     private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
     private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
     private static final int STATE_PREVIEW = 0;
     private static final int STATE_WAIT_LOCK = 1;
     private int mCaptureState = STATE_PREVIEW;
     // Texture View to display our camera's preview
     private TextureView mTextureView;
     // A listener so we can no when the texture view is available since inflation can take some time
     private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
         @Override
         public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
             //Toast.makeText(getApplicationContext(), "Texture View is available", Toast.LENGTH_SHORT).show();
             setUpCamera(width, height);
             connectCamera();
         }

         @Override
         public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

         }

         @Override
         public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
             return false;
         }

         @Override
         public void onSurfaceTextureUpdated(SurfaceTexture surface) {

         }
     };


     private CameraDevice mCameraDevice;
     // Returning a camera device so we can set it up
     private CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
         @Override
         public void onOpened(CameraDevice camera) {

             mCameraDevice = camera;
             //Toast.makeText(getApplicationContext(), "Camera connection made", Toast.LENGTH_SHORT).show();
             startPreview();
         }

         // Clean up the camera resources
         @Override
         public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
         }

         @Override
         public void onError(CameraDevice camera, int error) {
             camera.close();
             mCameraDevice = null;
         }
     };

     // A thread so we can perform all the time consuming tasks in the background without having fps drops in the front end
     private HandlerThread mBackgroundHandlerThread;
     private Handler mBackgroundHandler;

    // The id of the camera that we ll use, front or rear
     private String mCameraId;

     private Size mPreviewSize;

     private Size mImageSize;
     private ImageReader mImageReader;
     // A listener to notify us when the image is capture so we can proceed
     private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
         @Override
         public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
         }
     };

     public void toMenu(View view) {
         Intent goToMenuIntent = new Intent(getApplicationContext(), MenuActivity.class);
         startActivity(goToMenuIntent);
     }

     /*private class ImageObjectForIntent implements Runnable{
         private ImageInfo mImageInfo;

         public ImageObjectForIntent(ImageInfo mImageInfo) {
             this.mImageInfo = mImageInfo;
         }

         @Override
         public void run() {
             //ISWS EDW NA TO STELNW STO ALLO ACTIVITY
             Intent sendImageForPreviewIntent = new Intent(getApplicationContext(), PhotoPreviewActivity.class);
             //final int result = 1;
             sendImageForPreviewIntent.putExtra("imageToBeSent", mImageInfo);
             startActivity(sendImageForPreviewIntent);

         }
     }

     private class ImageInfo implements Serializable {
         private Image mImage;
         private String myImageFileName;

         public ImageInfo(Image image) {
             mImage = image;
             myImageFileName = mImageFileName;
         }

         public Image getmImage() {
             return mImage;
         }

         public String getMyImageFileName() {
             return myImageFileName;
         }
     }*/

     //A runnable class that will implements the saving image part
     private class ImageSaver implements Runnable{

         private final Image mImage;

         public ImageSaver(Image image) {
             mImage = image;
         }

         @Override
         public void run() {
             // Setting up a byte buffer and populating it with the image
             ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
             byte[] bytes = new byte[byteBuffer.remaining()];
             byteBuffer.get(bytes);


             FileOutputStream fileOutputStream = null;
             try {
                 fileOutputStream = new FileOutputStream(mImageFileName);
                 fileOutputStream.write(bytes);
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 mImage.close();
                 if (fileOutputStream != null){
                     try {
                         fileOutputStream.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }

             //ISWS EDW NA TO STELNW STO ALLO ACTIVITY
             goToPhotoPreviewActivity();

             closeCamera();

             /*stopBackgroundThread();*/
         }
     }

     private void goToPhotoPreviewActivity(){
         Intent sendImageForPreviewIntent = new Intent(getApplicationContext(), PhotoPreviewActivity.class);
         //final int result = 1;
         sendImageForPreviewIntent.putExtra("pathToImage", mImageFileName);
         startActivity(sendImageForPreviewIntent);
     }

     private int mTotalRotation;

     private CameraCaptureSession mPreviewCaptureSession;
     private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {

         private void process(CaptureResult captureResult){
             switch (mCaptureState){
                 case STATE_PREVIEW:
                     // Do nothing
                     break;
                 case STATE_WAIT_LOCK:
                     mCaptureState = STATE_PREVIEW;
                     Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                     if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                        || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
                         Toast.makeText(getApplicationContext(), "Autofocus Locked", Toast.LENGTH_SHORT).show();
                         startStillCaptureRequest();
                     }else {
                         Toast.makeText(getApplicationContext(), "Auto Focus Not Locked", Toast.LENGTH_SHORT).show();
                     }
                     break;
             }
         }

         @Override
         public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
             super.onCaptureCompleted(session, request, result);

             process(result);
         }
     };

     private CaptureRequest.Builder mCaptureRequestBuilder;

     //private ImageButton mRecordImageButton;
     private ImageButton mStillImageButton;

     private File mImageFolder;
     private String mImageFileName;

     // An array to translate device's orientation's to real world's
     private static SparseIntArray ORIENTATIONS = new SparseIntArray();
     static {
         ORIENTATIONS.append(Surface.ROTATION_0, 0);
         ORIENTATIONS.append(Surface.ROTATION_90, 90);
         ORIENTATIONS.append(Surface.ROTATION_180, 180);
         ORIENTATIONS.append(Surface.ROTATION_270, 270);
     }

     private static class CompareSizeByArea implements Comparator<Size>{

         // Method to compare the size of the recording and the preview in order to find their ratio
         @Override
         public int compare(Size o1, Size o2) {
             return Long.signum((long) o1.getWidth() * o1.getHeight() /
                     (long) o2.getWidth() * o2.getHeight());
         }
     }


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

        setContentView(R.layout.activity_main);

        createImageFolder();

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mStillImageButton = (ImageButton) findViewById(R.id.cameraImageButton2);
        mStillImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWriteStoragePermission();
                lockFocus();
                //Toast.makeText(getApplicationContext(), "OLAPOPA", Toast.LENGTH_SHORT).show();
            }
        });
        flashButton = (ImageButton) findViewById(R.id.flashBtn);

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlash();
            }
        });
        //mRecordImageButton = (ImageButton) findViewById(R.id.cameraImageButton2);
        /*mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWriteStoragePermission();
            }
        });*/
    }

    @Override
    protected void onResume(){
         super.onResume();

         startBackgroundThread();

         // Waiting for the TextureView to inflate if not inflated already
         if (mTextureView.isAvailable()){
             setUpCamera(mTextureView.getWidth(), mTextureView.getHeight());
             connectCamera();
         }else{
             mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
         }
    }

     @Override
     public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         // If it is camera permission requested
         if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
             // If request was rejected
             if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                 Toast.makeText(getApplicationContext(), "Application will not run without camera services", Toast.LENGTH_SHORT).show();
             }else{
                 Toast.makeText(getApplicationContext(), "Application granted camera services permission", Toast.LENGTH_SHORT).show();
             }
         }
         // If it is the write permission requested
         if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT){
             if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 /*try {
                     createImageFileName();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }*/
                 /*checkWriteStoragePermission();*/
                 Toast.makeText(this, "Permission write granted", Toast.LENGTH_SHORT).show();
             }else{
                 Toast.makeText(this, "Application needs to save to photos in order to run properly", Toast.LENGTH_LONG).show();
             }
         }
         return;
     }

     @Override
    protected void onPause(){
         closeCamera();

         stopBackgroundThread();
         super.onPause();
    }

    // Make the app's home page fullscreen
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

    // Method to set up configurations for this camera
    private void setUpCamera(int width, int height){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }

                // Creating a map with all the available preview resolutions
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                // Checking the orientation of the device in order to project our preview straight in case we are on portrait mode
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                // If we actually are in portrait swap height for width
                if (swapRotation){
                    rotatedHeight = width;
                    rotatedWidth = height;
                }
                // Selecting the optimal preview size between the available ones
                // POSSIBLE BUG, NOT FINDING ALL THE PREVIEWS
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera(){
         CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
         // If we are on a Marshmellow and above device we need to set up permissions check
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                     PackageManager.PERMISSION_GRANTED){
                 try {
                     CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                     Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                     isFlashSupported = available == null ? false : available;

                     setupFlashButton();

                     cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
                 } catch (CameraAccessException e) {
                     e.printStackTrace();
                 }
             }else{
                 // Else if permission was not granted
                 // If permission has been denied before we need to insist on asking for it
                 if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                     Toast.makeText(this, "Citizen Reporting Tool requires access to camera", Toast.LENGTH_SHORT).show();
                 }
                 requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
             }
         }else{
             try {
                 cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
             } catch (CameraAccessException e) {
                 e.printStackTrace();
             }
         }
    }

    private void startPreview (){

         SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
         surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
         Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            //Now we are ready to set up the session
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewCaptureSession = session;
                    // To constantly update the preview
                    // 2nd argument is null because we don't want to process that data we just want ot keep on projecting it
                    try {
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Unable to set up camera preview", Toast.LENGTH_SHORT).show();

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest(){
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            // Creating a callback so we will create a the file name only if we have the focus to avoid creating empty image files
            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    try {
                        createImageFileName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /*checkWriteStoragePermission();*/
                }
            };
            // Called to be executed by null thread because it is already being executed by the background handler
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Method to be called on exiting the camera to make sure the resources are freed
    private void closeCamera(){
         if (mCameraDevice != null) {
             mCameraDevice.close();
             mCameraDevice = null;
         }
    }

    // Methods to start and stop the thread
    private void startBackgroundThread(){
        mBackgroundHandlerThread = new HandlerThread("MainThread");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
         int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
         deviceOrientation = ORIENTATIONS.get(deviceOrientation);
         return (sensorOrientation + deviceOrientation +360) % 360;
         //return sensorOrientation;
    }

    // Checking which of the available aspect options fits the best to our preview
    private static Size chooseOptimalSize(Size[] choices, int width, int height){
         List<Size> bigEnough = new ArrayList<Size>();
         for (Size option : choices){
             // Aspect ratio check
             if ((option.getHeight() == ((option.getWidth() * height) / width)) &&
                     (option.getWidth() >= width) &&
                     (option.getHeight() >= height)){
                 bigEnough.add(option);
             }
         }
         if (bigEnough.size() > 0){
             return Collections.min(bigEnough,new CompareSizeByArea());
         }else{
             return choices[0];
         }
    }

    // Creating the folder on which the photos of the reports will be saved
    private void createImageFolder(){
         File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
         mImageFolder = new File(imageFile, "Citizen Reporting Tool");
         if (!mImageFolder.exists()){
             mImageFolder.mkdirs();
         }

    }

    // Method to create unique name for each photo report based on timestamp
    private File createImageFileName() throws IOException {
         String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         String prepend = "IMAGE_" + timestamp + "_";
         File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
         mImageFileName = imageFile.getAbsolutePath();
         return imageFile;
    }

    private void checkWriteStoragePermission(){
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
                 //Toast.makeText(this, "Application has permission to save photos", Toast.LENGTH_SHORT).show();
                 /*try {
                     createImageFileName();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }*/
             }else{
                 // If not running application for first time and permission is not granted
                 if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                     Toast.makeText(this, "Application needs to be able to save photos", Toast.LENGTH_SHORT).show();
                 }
                 // Request the permission
                 requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
             }
         }else{
             Toast.makeText(this, "Running on ol device yo?", Toast.LENGTH_SHORT).show();
             /*try {
                 createImageFileName();
             } catch (IOException e) {
                 e.printStackTrace();
             }*/
         }
        //Toast.makeText(this, "Eftasa", Toast.LENGTH_SHORT).show();
    }

    // Method to lock the focus on the focused object after tapping the take photo button
    private void lockFocus(){
         mCaptureState = STATE_WAIT_LOCK;
        //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
         mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
            //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            //Toast.makeText(this, "OOPS", Toast.LENGTH_SHORT).show();
        }
    }

    // Triggered everytime the flash button is pushed and opens/closes the flash accordingly
     public void switchFlash() {
         try {
             if (cameraId.equals(CAMERA_BACK)) {
                 if (isFlashSupported) {
                     if (isTorchOn) {
                         mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                         mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                         flashButton.setImageResource(R.drawable.ic_round_flash_off_24px);
                         /*flashButton.setBackgroundColor(Color.TRANSPARENT);*/
                         isTorchOn = false;
                     } else {
                         mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                         mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                         flashButton.setImageResource(R.drawable.ic_round_flash_on_24px);
                         /*flashButton.setBackgroundColor(Color.TRANSPARENT);*/
                         isTorchOn = true;
                     }
                 }
             }
         } catch (CameraAccessException e) {
             e.printStackTrace();
         }
     }

     // Setting up the starting status of the flash button
     public void setupFlashButton() {
         if (cameraId.equals(CAMERA_BACK) && isFlashSupported) {
             flashButton.setVisibility(View.VISIBLE);

             if (isTorchOn) {
                 flashButton.setImageResource(R.drawable.ic_round_flash_on_24px);
             } else {
                 flashButton.setImageResource(R.drawable.ic_round_flash_off_24px);
             }

         } else {
             flashButton.setVisibility(View.GONE);
         }
     }
}
