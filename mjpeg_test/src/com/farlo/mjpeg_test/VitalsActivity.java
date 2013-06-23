package com.farlo.mjpeg_test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;


public class VitalsActivity extends Activity {

    private static final String TAG = "MJPEG_TEST_VITALS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        //win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 

        setContentView(R.layout.vitals);

    }
     @Override
    public void onDestroy(){
        super.onDestroy();
    }   

    @Override
    public void onStart(){
        super.onStart();
    }   

    @Override
    public void onResume(){
        super.onResume();
    }   
    

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
     public boolean onKeyDown(int keyCode, KeyEvent event){
        Log.d(TAG, "onKeyDown keyCode=" + keyCode + " event = " + event);
        if( keyCode == 61  || keyCode == 82) {
            // 61 = tab button, or horizontal swipe on glass.
            // 82 = menu key
            Log.d("","Got a horizontal swipe");
        Intent myIntent = new Intent(VitalsActivity.this, EKGActivity.class);
        myIntent.putExtra("key", ""); //Optional parameters
        VitalsActivity.this.startActivity(myIntent);
    }
    else if( keyCode == 4 ) {
        // 4 = back button on device, or vertical swipe on glass.
        Log.d(TAG,"Got a vertical swipe");
        onPause();
    }
        return( false );
     }
    @Override
    public void onPause(){
        super.onPause();
        //System.exit(0);
        finish();
    }

  }

