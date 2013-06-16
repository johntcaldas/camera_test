package com.farlo.mjpeg_test;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewStreamer mPreviewStreamer;

    public int imageWidth = 0;
    public int imageHeight = 0;
    public int format = 0;
    public int bufferSize = 0;
    public Rect rect = null;

    String TAG = "CameraPreview";

    public CameraPreview(Context context, Camera camera, PreviewStreamer previewStreamer) {
        super(context);
        mCamera = camera;
        mPreviewStreamer = previewStreamer;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Camera.Parameters cameraParameters = mCamera.getParameters();

            // Set FPS Range. TODO: check to see if this is really necessary.
            final int[] range = cameraParameters.getSupportedPreviewFpsRange().get(0);
            cameraParameters.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                    range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
            mCamera.setParameters(cameraParameters);

            // Set up preview callback
            format = cameraParameters.getPreviewFormat();
            final Camera.Size previewSize = cameraParameters.getPreviewSize();
            imageWidth = previewSize.width;
            imageHeight = previewSize.height;
            final int bitsPerByte = 8;
            final int bytesPerPixel = ImageFormat.getBitsPerPixel(format) / bitsPerByte;

            // Empirically determined that buffer size is width * height * bytesPerPixel * magical constant (1.5)
            bufferSize = imageWidth * imageHeight * bytesPerPixel * 3 / 2 + 1;
            mCamera.addCallbackBuffer(new byte[bufferSize]);
            rect = new Rect(0, 0, imageWidth, imageHeight);
            mCamera.setPreviewCallbackWithBuffer(mPreviewStreamer.mPreviewCallback);

            // Start up the preview streamer
            mPreviewStreamer.init(bufferSize, format, imageWidth, imageHeight, rect);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            //mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            //mCamera.setPreviewDisplay(mHolder);
            //mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
