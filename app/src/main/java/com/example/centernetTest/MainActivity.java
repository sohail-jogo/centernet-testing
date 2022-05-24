package com.example.centernetTest;

import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;

import com.example.centernetTest.iostream.IOStream;
import com.example.centernetTest.models.ModelManager;
import com.example.centernetTest.utils.ExtendedMLImage;

import java.util.List;

public class MainActivity extends CameraActivity implements IOStream {

    public ModelManager modelManager;
    private ImageView backButton;


    /**
     * A note to Mr.George Soloupis.
     *  YOU will  model detections logs.
     *  If you want to change the model (from centernet to SSD), Go to ModelManager -> Line 26.
     *
     */

    @Override
    protected void initViews() {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (modelManager == null)
            this.modelManager = ModelManager.createInstance(this, getApplicationContext());

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> back());

    }


    private void back() {
        finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onStart() {
        super.onStart();
    }


    protected void supplyImageToModelManager(ExtendedMLImage extendedMLImage) {
        modelManager.supplyFrame(extendedMLImage);
    }


    @Override
    public void onImageProcessed(InfoBlob infoBlob) {

    }
}