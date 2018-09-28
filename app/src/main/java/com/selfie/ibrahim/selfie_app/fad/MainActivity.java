package com.selfie.ibrahim.selfie_app.fad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.selfie.ibrahim.selfie_app.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar BANANA_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetectorBanana;
    boolean check = false;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeBananaSize = 0.2f;
    private int mAbsoluteBananaSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;

    public Button mSpeechButton;
    public TextView checkResults;

    double xCenter = -1;
    double yCenter = -1;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV Loaded Successfully");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetectorBanana = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetectorBanana.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetectorBanana = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.setCameraIndex(0);
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        Log.i(TAG, "Instantiated new" + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.i(TAG,"Called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_surface);
       /* mOpenCvCameraView.setCvCameraViewListener(this);
        mSpeechButton = (Button) findViewById(R.id.ttsBtn);
        checkResults = (TextView) findViewById(R.id.checkResultTB);

        Button backButton = (Button) findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent UIActivity = new Intent(MainActivity.this, UiActivity.class);
                startActivity(UIActivity);
            }
        });*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mOpenCvCameraView!=null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteBananaSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeBananaSize) > 0) {
                mAbsoluteBananaSize = Math.round(height * mRelativeBananaSize);
            }
        }

        MatOfRect bananas = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetectorBanana != null) {
                mJavaDetectorBanana.detectMultiScale(mGray, bananas, 1.1, 2, 2, //TODO: objdetect.CV_HAAR_SCALE_IMAGE)
                        new Size(mAbsoluteBananaSize, mAbsoluteBananaSize), new Size());
            }
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] bananasArray = bananas.toArray();
        for (int i = 0; i < bananasArray.length; i++) {
            Imgproc.rectangle(mRgba, bananasArray[i].tl(), bananasArray[i].br(), BANANA_RECT_COLOR, 3);
            xCenter = (bananasArray[i].x + bananasArray[i].width + bananasArray[i].x) / 2;
            yCenter = (bananasArray[i].y + bananasArray[i].height + bananasArray[i].y) / 2;
        }
        return mRgba;
    }}