package com.farlo.mjpeg_test;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;


public class MainActivity extends Activity {

    private static final String TAG = "MJPEG_TEST_MAIN";

    /* Configuration */
    public static final int STREAM_PORT = 8080;
    public static final int JPEG_QUALITY = 40;

    private Camera mCamera;
    private CameraPreview mPreview;
    private PreviewStreamer mPreviewStreamer;

    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate ...");

        super.onCreate(savedInstanceState);

        // Get rid of the title, and keep the screen on all the time.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        // Allow network on main thread. Remove me when done debugging.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Context context = (Context) this;
        if(!checkCameraHardware(context)) {
            Log.d(TAG, "No Camera Found!");
        }

        Log.d(TAG, "Getting camera instance ...");
        mCamera = getCameraInstance();

        if(mCamera == null) {
            Log.d(TAG, "Camera is null!");
        }

        // Get the camera parameters.
        //Camera.Parameters mCameraParams = mCamera.getParameters();

        // Set up streamer.
        mPreviewStreamer = new PreviewStreamer(STREAM_PORT, JPEG_QUALITY);


        // Create our Preview view and set it as the content of our activity.
        Log.d(TAG, "Initializing preview ...");
        mPreview = new CameraPreview(this, mCamera, mPreviewStreamer);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume ...");

        super.onResume();

        // Start Streaming
        mPreviewStreamer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop Streaming

        // Release Camera
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera is not available! msg=" +  e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }
}