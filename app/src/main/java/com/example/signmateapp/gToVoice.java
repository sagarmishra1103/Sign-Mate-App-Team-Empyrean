package com.example.signmateapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class gToVoice extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="gToVoice";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private objectDetectorClass objectDetectorClass;

    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");

                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public gToVoice(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=101;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(gToVoice.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(gToVoice.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }



        setContentView(R.layout.g_to_voice);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        try{

            objectDetectorClass=new objectDetectorClass(getAssets(),"model.tflite","labels.txt",300);
            Log.d("gToVoice","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("gToVoice","Getting some error");
            e.printStackTrace();
        }

        Button b1 = findViewById(R.id.gtv_b1);
        b1.setOnClickListener(
                v -> {

                    if (OpenCVLoader.initDebug()) {

                        mOpenCvCameraView.enableView();
                    }
                });
        Button b2 = findViewById(R.id.gtv_b2);
        b2.setOnClickListener(v -> {
            Intent i = new Intent(gToVoice.this, vToGesture.class);
            startActivity(i);
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initDebug();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallback);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();
        // Before watching this video please watch previous video of loading tensorflow lite model

        // now call that function
        Mat out=new Mat();
        out=objectDetectorClass.recognizeImage(mRgba);

        return out;
    }

    @Override
    public List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }
}
