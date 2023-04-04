package com.example.signmateapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class  objectDetectorClass {
    // should start from small letter

    // this is used to load model and predict
    private Interpreter interpreter;

    private Interpreter interpreter2;
    // store all label in array
    private List<String> labelList;
    private int INPUT_SIZE;
    private int PIXEL_SIZE=3; // for RGB
    private int IMAGE_MEAN=0;
    private  float IMAGE_STD=255.0f;
    // use to initialize gpu in app
    private GpuDelegate gpuDelegate;
    private int height=0;
    private  int width=0;
    private  int classification_input_size=0;


    objectDetectorClass(AssetManager assetManager,String modelPath, String labelPath,int inputSize) throws IOException{
        INPUT_SIZE=inputSize;

        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if(compatList.isDelegateSupportedOnThisDevice()){
            // if the device has a supported GPU, add the GPU delegate
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
        } else {
            // if the GPU is not supported, run on 4 threads
            options.setNumThreads(4);
        }
        // load labelmap
        labelList=loadLabelList(assetManager,labelPath);
        Interpreter.Options options2=new Interpreter.Options();
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // to store label
        List<String> labelList=new ArrayList<>();
        // create a new reader
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        // loop through each line and store it to labelList
        while ((line=reader.readLine())!=null){
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // use to get description of file
        AssetFileDescriptor fileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset =fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    // create new Mat function
    public Mat recognizeImage(Mat mat_image){
        // Rotate original image by 90 degree get get portrait frame

        // This change was done in video: Does Your App Keep Crashing? | Watch This Video For Solution.
        // This will fix crashing problem of the app

        Mat rotated_mat_image=new Mat();

        Mat a=mat_image.t();
        Core.flip(a,rotated_mat_image,1);
        // Release mat
        a.release();

        // if you do not do this process you will get improper prediction, less no. of object
        // now convert it to bitmap
        Bitmap bitmap=null;
        bitmap=Bitmap.createBitmap(rotated_mat_image.cols(),rotated_mat_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotated_mat_image,bitmap);
        // define height and width
        height=bitmap.getHeight();
        width=bitmap.getWidth();

        // scale the bitmap to input size of model
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);

        // convert bitmap to bytebuffer as model input should be in it
        ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);

        // defining output
        // 10: top 10 object detected
        // 4: there coordinate in image
        //  float[][][]result=new float[1][10][4];
        Object[] input=new Object[1];
        input[0]=byteBuffer;

        Map<Integer,Object> output_map=new TreeMap<>();
        // we are not going to use this method of output
        // instead we create treemap of three array (boxes,score,classes)

        float[][][]boxes =new float[1][10][4];
        // 10: top 10 object detected
        // 4: there coordinate in image
        float[][] scores=new float[1][10];
        // stores scores of 10 object
        float[][] classes=new float[1][10];
        // stores class of object

        // add it to object_map;
        output_map.put(0,boxes);
        output_map.put(1,classes);
        output_map.put(2,scores);

        // now predict
        interpreter.runForMultipleInputsOutputs(input,output_map);
        // Before watching this video please watch my previous 2 video of
        //      1. Loading tensorflow lite model
        //      2. Predicting object
        // In this video we will draw boxes and label it with it's name

        Object value=output_map.get(0);
        Object Object_class=output_map.get(1);
        Object score=output_map.get(2);

        // loop through each object
        // as output has only 10 boxes
        for (int i=0;i<10;i++){
            //here we are looping through each hand detected
            float class_value=(float) Array.get(Array.get(Object_class,0),i);
            float score_value=(float) Array.get(Array.get(score,0),i);
            // define threshold for score

            // Here you can change threshold according to your model
            // Now we will do some change to improve app
            if(score_value>0.5){
                Object box1=Array.get(Array.get(value,0),i);
                // we are multiplying it with Original height and width of frame

                float x1=(float) Array.get(box1,0)*height;
                float y1=(float) Array.get(box1,1)*width;
                float x2=(float) Array.get(box1,2)*height;
                float y2=(float) Array.get(box1,3)*width;

                //set boundary limit
                if (y1<0){
                    y1=0;
                }
                if (x1<0){
                    x1=0;
                }
                if (y1>width){
                    y1=width;
                }
                if (y1>height){
                    y1=height;
                }
                //now set height and width of the box
                float w1= x2-x1;
                float h1 =y2-y1;

                //(x1,y1) starting point if hand
                //(x2,y2) ending point if frame

                //crop hand image from original frame
                Rect cropped_roi= new Rect((int)x1, (int)y1, (int)w1, (int)h1 );
                Mat cropped= new Mat(rotated_mat_image, cropped_roi.clone());

                //now convert this cropped mat to bitmap
                Bitmap bitmap1=null;
                bitmap1= Bitmap.createBitmap(cropped.cols(),cropped.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped,bitmap1);

                //resize bitmap1 to classification input size=96
                Bitmap scaledBitmap1= Bitmap.createScaledBitmap(bitmap1,classification_input_size,classification_input_size,false);
                //Convert scaledBitmap1 to byte buffer
                ByteBuffer bytebuffer1= convertBitmapToByteBuffer1(scaledBitmap1);
                //create an array of interpreter2
                float[][] output_class_value=new float[1][1];

                //predict output of Bytebuffer1
                interpreter2.run(bytebuffer1, output_class_value);
                //if you want to output class value
                Log.d("objectDetectionClass","output_Class_value:"+output_class_value[0][0]);

                //convert output values to alphabets
                //now create alphabets function
                String sign_val=get_alphabets(output_class_value[0][0]);
                //use puttext to add class name in image
                //          input/output                  text          starting point              font size                       text color ( black )
                Imgproc.putText(rotated_mat_image,""+sign_val,labelList.get((int) class_value),new Point(x1+10,y1+40),2,1.5,new Scalar(0, 0, 0, 255),2);
                Imgproc.rectangle(rotated_mat_image,new Point(x1,y1),new Point(x2,y2),new Scalar(0, 255, 0, 255),2);

                //now will change layout

            }

        }
        // select device and run

        // before returning rotate back by -90 degree

        // Do same here
        Mat b=rotated_mat_image.t();
        Core.flip(b,mat_image,0);
        b.release();
        // Now for second change go to CameraBridgeViewBase
        return mat_image;
    }

    private String get_alphabets(float sig_v) {
        String val="";
        if(sig_v>=-0.5 & sig_v<0.5){
            val="A";
        }
        else if(sig_v>=0.5 & sig_v<1.5){
            val="B";
        }
        //do this process from A-Y
        else if(sig_v>=1.5 & sig_v<2.5){
            val="C";
        }
        else if(sig_v>=2.5 & sig_v<3.5){
            val="D";
        }else if(sig_v>=3.5 & sig_v<4.5){
            val="E";
        }
        else if(sig_v>=4.5 & sig_v<5.5){
            val="F";
        }else if(sig_v>=5.5 & sig_v<6.5){
            val="G";
        }
        else if(sig_v>=6.5 & sig_v<7.5){
            val="H";
        }else if(sig_v>=7.5 & sig_v<8.5){
            val="I";
        }
        else if(sig_v>=8.5 & sig_v<9.5){
            val="J";
        }else if(sig_v>=9.5 & sig_v<10.5){
            val="K";
        }
        else if(sig_v>=10.5 & sig_v<11.5){
            val="L";
        }else if(sig_v>=11.5 & sig_v<12.5){
            val="M";
        }
        else if(sig_v>=12.5 & sig_v<13.5){
            val="N";
        }else if(sig_v>=13.5 & sig_v<14.5){
            val="O";
        }
        else if(sig_v>=14.5 & sig_v<15.5){
            val="P";
        }else if(sig_v>=15.5 & sig_v<16.5){
            val="Q";
        }
        else if(sig_v>=16.5 & sig_v<17.5){
            val="R";
        }else if(sig_v>=17.5 & sig_v<18.5){
            val="S";
        }
        else if(sig_v>=18.5 & sig_v<19.5){
            val="T";
        }else if(sig_v>=19.5 & sig_v<20.5){
            val="U";
        }
        else if(sig_v>=20.5 & sig_v<21.5){
            val="V";
        }else if(sig_v>=21.5 & sig_v<22.5){
            val="W";
        }
        else if(sig_v>=22.5 & sig_v<23.5){
            val="X";

        }else {
            val="Y";
        }
        return val;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        // some model input should be quant=0  for some quant=1
        // for this quant=0
        // Change quant=1
        // As we are scaling image from 0-255 to 0-1
        int quant = 1;
        int size_images = INPUT_SIZE;
        if (quant == 0) {
            byteBuffer = ByteBuffer.allocateDirect(size_images * size_images * 3);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * size_images * size_images * 3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[size_images * size_images];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        // some error
        //now run
        for (int i = 0; i < size_images; ++i) {
            for (int j = 0; j < size_images; ++j) {
                final int val = intValues[pixel++];
                if (quant == 0) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    // paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val) & 0xFF)) / 255.0f);
                }
            }
        }
        return byteBuffer;
    }


    private ByteBuffer convertBitmapToByteBuffer1(Bitmap bitmap) {
        ByteBuffer byteBuffer;

        int quant=1;

        //change input size
        int size_images= classification_input_size;
        if(quant==0){
            byteBuffer=ByteBuffer.allocateDirect(size_images * size_images * 3);
        }
        else {
            byteBuffer=ByteBuffer.allocateDirect(4 * size_images * size_images * 3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[size_images*size_images];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel=0;
        //remove 255.0f as we did not scaled image
        for (int i=0;i<size_images;++i){
            for (int j=0;j<size_images;++j){
                final  int val=intValues[pixel++];
                if(quant==0){
                    byteBuffer.put((byte) ((val>>16)&0xFF));
                    byteBuffer.put((byte) ((val>>8)&0xFF));
                    byteBuffer.put((byte) (val&0xFF));
                }
                else {

                    byteBuffer.putFloat((((val >> 16) & 0xFF)));
                    byteBuffer.putFloat((((val >> 8) & 0xFF)));
                    byteBuffer.putFloat((((val) & 0xFF)));
                }
            }
        }
        return byteBuffer;

    }
}