package com.example.centernetTest.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.google.android.odml.image.BitmapExtractor;
import com.google.android.odml.image.BitmapMlImageBuilder;
import com.google.android.odml.image.MlImage;

public class ExtendedMLImage {

    public final int screenOrientation;
    private final int frameID;
    private final MlImage mlImage;

    public ExtendedMLImage(Bitmap bitmap, int frameID, int screenOrientation) {
        this.frameID = frameID;
        this.screenOrientation = screenOrientation;
        mlImage = new BitmapMlImageBuilder(bitmap).setRotation(screenOrientation).build();
    }

    public ExtendedMLImage(Bitmap bitmap, int frameID) {
        this(bitmap, frameID, 0);
    }

    public int getFrameID() {
        return frameID;
    }

    public MlImage getMlImage() {
        return mlImage;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public Bitmap getCropped(int croppedWidth, int croppedHeight) {
        return cropBitmap(false, croppedWidth, croppedHeight);
    }

    private Bitmap cropBitmap(boolean maintainAspectRatio, int croppedWidth, int croppedHeigth) {
        int sensorOrientation = screenOrientation; //orientation 1

        //todo this can be optimized
        //MATRIX for cropping
        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(
                BitmapExtractor.extract(this.mlImage).getWidth(), BitmapExtractor.extract(this.mlImage).getHeight(),
                croppedWidth, croppedHeigth,
                sensorOrientation, maintainAspectRatio, false, false);


        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        // the actual cropping
        Bitmap croppedBitmap = Bitmap.createBitmap(croppedWidth, croppedHeigth, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(BitmapExtractor.extract(this.mlImage), frameToCropTransform, null);
//        ImageUtils.saveBitmap(croppedBitmap,"image.png");

        return croppedBitmap;
    }
}

