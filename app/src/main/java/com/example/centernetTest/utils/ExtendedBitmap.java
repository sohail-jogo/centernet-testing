package com.example.centernetTest.utils;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class ExtendedBitmap {

    private int frameID;
    private Bitmap bitmap;

    public int screenOrientation;
    private boolean mirrorY = false;
    private boolean mirrorX = false;


    public ExtendedBitmap(Bitmap bitmap, int frameID, int screenOrientation, boolean isBackCamera) {

        this.bitmap = bitmap;
        this.frameID = frameID;
        this.screenOrientation = screenOrientation;

        //todo this is extremely hacky

        if (isBackCamera) {
            this.screenOrientation = 90; // todo make sure this works correctly...
        } else {
            this.screenOrientation = -90; // todo make sure this works correctly...
            if ((screenOrientation % 90) == 0) mirrorX = true; // mirror x because we also do rotation I think...

        }

    }


    public ExtendedBitmap(Bitmap bitmap, int frameID) {
        this(bitmap, frameID, 0, true);
    }

    public Bitmap getCropped(int croppedWidth, int croppedHeight) {
        return cropBitmap(false, croppedWidth, croppedHeight);
    }

    public int getFrameID() {
        return frameID;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


    private Bitmap cropBitmap(boolean maintainAspectRatio, int croppedWidth, int croppedHeigth) {
        int sensorOrientation = screenOrientation; //orientation 1

        //todo this can be optimized
        //MATRIX for cropping
        Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(
                bitmap.getWidth(), bitmap.getHeight(),
                croppedWidth, croppedHeigth,
                sensorOrientation, maintainAspectRatio, mirrorY, mirrorX);


        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        // the actual cropping
        Bitmap croppedBitmap = Bitmap.createBitmap(croppedWidth, croppedHeigth, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);
//        ImageUtils.saveBitmap(croppedBitmap,"image.png");

        return croppedBitmap;
    }

    public Bitmap getFlippedBitmap( boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }
}
