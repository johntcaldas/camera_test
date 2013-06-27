package com.farlo.mjpeg_test;


import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.io.IOException;

public class PreviewStreamer {

    int mPort = 0;
    WorkHandler mWorker = null;
    MemoryOutputStream mJpegOutputStream = null;
    MJpegHttpStreamer mMJpegHttpStreamer = null;
    public Camera.PreviewCallback mPreviewCallback = null;

    public int mImageWidth = 0;
    public int mImageHeight = 0;
    public int mFormat = 0;
    public int mJpegQuality = 0;
    public int mBufferSize = 0;
    public Rect mRect = null;

    private boolean initialized = false;
    private boolean waitingToStart = false;


    String TAG = "PreviewStreamer";

    public PreviewStreamer(int port, int jpegQuality) {
        mPort = port;
        mJpegQuality = jpegQuality;
        mPreviewCallback = PreviewCallback;
    }

    public void init(int bufferSize, int format, int width, int height, Rect rect) {
        mBufferSize = bufferSize;
        mFormat = format;
        mImageWidth = width;
        mImageHeight = height;
        mRect = rect;
        initialized = true;

        if(waitingToStart) {
            start();
        }
    }

    public void start() {
        Log.d(TAG, "start() ...");

        if(!initialized) {
            waitingToStart = true;
            return;
        }

        final HandlerThread worker = new HandlerThread(TAG, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        worker.setDaemon(true);
        worker.start();
        Looper looper = worker.getLooper();
        mWorker = new WorkHandler(looper);

        mJpegOutputStream = new MemoryOutputStream(mBufferSize);
        mMJpegHttpStreamer = new MJpegHttpStreamer(mPort, mBufferSize);
        mMJpegHttpStreamer.start();
    }

    public void stop() {
        Log.d(TAG, "stop() ...");

        try {
        mMJpegHttpStreamer.stop();
        }
        catch (Exception e) {
            Log.e(TAG, "Could not stop mMJpegHttpStreamer. Exception=" + e.getMessage());
        }

        try {
            mJpegOutputStream.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Could not close JpenOutputStream. Exception=" + e.getMessage());
        }
    }

    private final Camera.PreviewCallback PreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera)
        {
            final Long timestamp = SystemClock.elapsedRealtime();
            final Message message = mWorker.obtainMessage();
            message.obj = new Object[]{ data, camera, timestamp };
            message.sendToTarget();
        }
    };

    private final class WorkHandler extends Handler {

        private WorkHandler(final Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(final Message message) {
            final Object[] args = (Object[]) message.obj;
            sendPreviewFrame((byte[]) args[0], (Camera) args[1], (Long) args[2]);
        }
    }

    private void sendPreviewFrame(final byte[] data, final Camera camera, final long timestamp) {

        final YuvImage image = new YuvImage(data, mFormat, mImageWidth, mImageHeight, null);
        image.compressToJpeg(mRect, mJpegQuality, mJpegOutputStream);

        mMJpegHttpStreamer.streamJpeg(mJpegOutputStream.getBuffer(), mJpegOutputStream.getLength(),
                timestamp);

        mJpegOutputStream.seek(0);

        camera.addCallbackBuffer(data);
    }
}
