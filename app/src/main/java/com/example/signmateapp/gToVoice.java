package com.example.signmateapp;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class gToVoice extends CameraActivity {
    Button b1 ;
    Button b2 ;
    private Mat mRgba;
    private Mat mGray;
    CameraBridgeViewBase cameraBridgeViewBase;
    com.example.signmateapp.objectDetectorClass objectDetectorClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.g_to_voice);

        cameraBridgeViewBase= findViewById(R.id.javaCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);



        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStarted(int width, int height) {
                mRgba=new Mat(height,width, CvType.CV_8UC4);
                mGray =new Mat(height,width,CvType.CV_8UC1);
            }

            @Override
            public void onCameraViewStopped() {
                mRgba.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                mRgba=inputFrame.rgba();
                mGray=inputFrame.gray();
                // Before watching this video please watch previous video of loading tensorflow lite model

                // now call that function
                new Mat();
                Mat out;
                out=objectDetectorClass.recognizeImage(mRgba);

                return out;
            }
        });

        try{

            objectDetectorClass=new objectDetectorClass(getAssets(),"model.tflite","label.txt",300);
            Log.d("gTOVoice","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("gToVoice","Getting some error");
            e.printStackTrace();
        }

        b1 = findViewById(R.id.gtv_b1);
        b1.setOnClickListener(
                v -> {
                    if(OpenCVLoader.initDebug()){
                        cameraBridgeViewBase.enableView();
                    }
                }
        );

        b2 = findViewById(R.id.gtv_b2);
        b2.setOnClickListener(
                v -> {
                    Intent i = new Intent(gToVoice.this, vToGesture.class);
                    startActivity(i);
                }
        );
        getPermission();

        if (OpenCVLoader.initDebug()) Log.d("OpenCV APP","Loaded......");
    }
    void getPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions((new String[] {Manifest.permission.CAMERA}), 101);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        b1.setOnClickListener(
                v -> {
                    if(OpenCVLoader.initDebug()){
                        Log.d(TAG,"Opencv initialization is done");

                        cameraBridgeViewBase.enableView();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase !=null) {
        cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase !=null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase) ;
    }
}