package com.selfie.ibrahim.selfie_app.test1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.selfie.ibrahim.selfie_app.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Main extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    public  static String TAG="Main";

    private JavaCameraView mJavaCameraView;
    private Mat mRrgba;
    BaseLoaderCallback mBaseLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    mJavaCameraView.enableView();

                    break;
                }
                default:
                    super.onManagerConnected(status);

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test1);

        mJavaCameraView=findViewById(R.id.java_camera_view);
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mJavaCameraView != null) {
            mJavaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mJavaCameraView != null) {
            mJavaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG,"Opencv111 Loaded Success");
            mBaseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }else {
            Log.d(TAG,"Opencv111 Loaded Failed");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mBaseLoaderCallback);

        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRrgba=new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

        mRrgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRrgba=inputFrame.rgba();
        return mRrgba;
    }
}
