
package com.selfie.ibrahim.selfie_app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

/** Main {@code Activity} class for the Camera app. */
public class CameraActivity extends AppCompatActivity {

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    if (null == savedInstanceState) {
      getSupportFragmentManager().beginTransaction()
              .replace(R.id.container, Camera2BasicFragment.newInstance())
              .commit();
    }
  }
}
