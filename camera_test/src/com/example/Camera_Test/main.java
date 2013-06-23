package com.example.Camera_Test;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class main extends Activity
{
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private boolean isRecording = false;

    public final static String TAG = "camera_test_main";

    public static final int WIDTH = 720;
    public static final int HEIGHT = 480;

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private TextView mTextView;
    private CameraPreview mPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get rid of the title, and keep the screen on all the time.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mTextView = (TextView) findViewById(R.id.videoMessageTextView);

        // Allow network on main thread. Remove me when done debugging.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Begin camera hacking code.
        Context context = (Context) this;
        if(!checkCameraHardware(context)) {
            mTextView.setText("No Camera Found.");
        }

        Log.d(TAG, "Getting camera instance ...");
        mCamera = getCameraInstance(mTextView);

        if(mCamera == null) {
            mTextView.setText("Camera is null.");
        }

        Camera.Parameters mCameraParams = mCamera.getParameters();
        mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        mCameraParams.set("cam_mode", 1);
        mCameraParams.setPreviewSize(WIDTH, HEIGHT);
        mCamera.setParameters(mCameraParams);

        // Create our Preview view and set it as the content of our activity.
        Log.d(TAG, "Initializing preview ...");
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        /*
        Log.d(TAG, "Preparing video recorder ...");
        if(!prepareVideoRecorder()) {
            Log.d(TAG, "Failed to prepare video recorder.");
        }*/
    }

    public void myClickHandler(View view) {

        if(isRecording) {
            mTextView.setText("Stopping Video ...");
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            isRecording = false;
        } else {
            mTextView.setText("Grabbing Video ...");
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            //if(mMediaRecorder != null) {
                Log.d(TAG, "Preparing video recorder ...");
                if(!prepareVideoRecorder()) {
                    Log.d(TAG, "Failed to prepare video recorder.");
                }
                else {
                    mMediaRecorder.start();
                    isRecording = true;
                }
            //}
            //else {
            //    Log.d(TAG, "MediaRecorder was null on video capture attempt.");
            //}
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
    public static Camera getCameraInstance(TextView myTextView){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            myTextView.setText("Could not grab camera.");
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        camcorderProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        camcorderProfile.videoCodec = MediaRecorder.VideoEncoder.H263;
        camcorderProfile.videoFrameWidth = WIDTH;
        camcorderProfile.videoFrameHeight = HEIGHT;
        mMediaRecorder.setProfile(camcorderProfile);

        // Step 4: Set output file
        // TO FILE:
        //mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // UDP:
        /*
        DatagramSocket socket;
        try {
            InetAddress destination = InetAddress.getByName("192.168.1.148");
            socket = new DatagramSocket();
            socket.connect(destination, 50007);
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromDatagramSocket(socket);
            mMediaRecorder.setOutputFile(pfd.getFileDescriptor());
        } catch (UnknownHostException e) {
            Log.d(TAG, "Caught UnknownHostException: " + e.getMessage());
        } catch (IOException eio) {
            Log.d(TAG, "Caught IOException: " + eio.getMessage());
        }*/

        // TCP:

        Socket socket;
        try {
            socket = new Socket("192.168.1.124",50007);
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
            mMediaRecorder.setOutputFile(pfd.getFileDescriptor());
        } catch (UnknownHostException e) {
            Log.d(TAG, "Caught UnknownHostException: " + e.getMessage());
        } catch (IOException eio) {
            Log.d(TAG, "Caught IOException: " + eio.getMessage());
        }


        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "h263.mp4");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}




