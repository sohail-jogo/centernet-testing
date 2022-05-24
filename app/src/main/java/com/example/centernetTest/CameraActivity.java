package com.example.centernetTest;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.centernetTest.utils.ExtendedBitmap;
import com.example.centernetTest.utils.ExtendedMLImage;
import com.example.centernetTest.utils.ImageUtils;
import com.karumi.dexter.Dexter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.PermissionToken;

import com.karumi.dexter.listener.PermissionRequest;
import com.example.centernetTest.camera.CameraConnectionFragment;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class CameraActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener {


    private boolean isCameraOK(CameraManager manager, String cameraId) throws CameraAccessException {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        return Objects.equals(facing, CameraCharacteristics.LENS_FACING_BACK);
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                if (isCameraOK(manager, cameraId)) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
        }
        throw new RuntimeException("should always have correct camera");
    }

    protected CameraConnectionFragment cameraFragment;

    protected void setFragment() {
        String cameraID = chooseCamera();
        cameraFragment = CameraConnectionFragment.newInstance(
                (size, rotation) -> {
                    initViews();
                },
                this,
                getLayoutId(),
                getDesiredPreviewFrameSize(),
                getRecordingVideoFile()
        );
        cameraFragment.setCamera(cameraID);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, cameraFragment).commit();

    }

    /**
     * PERMISSIONS
     **/
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            setFragment();
                        }

                        // check for permanent denial of any permission
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            requestPermission();
                        } else if (multiplePermissionsReport.getDeniedPermissionResponses().size() > 0) {
                            requestPermission();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> {
            Toast.makeText(this, "Please allow permission for this feature to work", Toast.LENGTH_SHORT).show();
            openSettings();
        }).onSameThread().check();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission() {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void managePermissionsAndCamera() {
        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }
    }


    /**
     * android
     **/

    protected abstract void initViews();


    private static final Size DESIRED_PREVIEW_SIZE = new Size(300, 300);
    //App lags on this preview size
    private static final Size HIGH_RESOLUTION_PREVIEW_SIZE = new Size(1920, 1080);

    public boolean isOrientationLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }


    public File getRecordingVideoFile() {
        return getOutputMediaFile();
    }

    public File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = getExternalFilesDir("videos");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("JOGO", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }


    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    protected Size getDesiredPreviewFrameSize() {
        return HIGH_RESOLUTION_PREVIEW_SIZE;
    }

    protected void initWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideNavigationBar();
        setContentView(R.layout.tfe_od_activity_camera);
    }


    protected void setupSettings() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to parse Exercise settings", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        initWindow();
        managePermissionsAndCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = new int[getDesiredPreviewFrameSize().getWidth() * getDesiredPreviewFrameSize().getHeight()];

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    AtomicInteger frameCounter = new AtomicInteger(0);

    private boolean processing = false;

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Image image = reader.acquireLatestImage();

        if (processing) return;
        if (image == null) return;
        processing = true;
        final Image.Plane[] planes = image.getPlanes();
        int yRowStride = planes[0].getRowStride();
        final int uvRowStride = planes[1].getRowStride();
        final int uvPixelStride = planes[1].getPixelStride();

        final int previewWidth = image.getWidth();
        final int previewHeight = image.getHeight();

        //transform bytes into bitmap
        fillBytes(planes, yuvBytes);
        ImageUtils.convertYUV420ToARGB8888(
                yuvBytes[0],
                yuvBytes[1],
                yuvBytes[2],
                previewWidth,
                previewHeight,
                yRowStride,
                uvRowStride,
                uvPixelStride,
                rgbBytes);

        Bitmap rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        //todo ensure

        int orientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 0 : 90;
        ExtendedBitmap extendedBitmap = new ExtendedBitmap(rgbFrameBitmap, frameCounter.getAndIncrement(), orientation, true);
        ExtendedMLImage extendedMLImage = new ExtendedMLImage(rgbFrameBitmap, frameCounter.getAndIncrement(), 90);
        Bitmap bitmap = preProcessImage(extendedBitmap);
        supplyImageToModelManager(extendedMLImage);

        image.close();
        processing = false;
    }

    abstract void supplyImageToModelManager(ExtendedMLImage extendedMLImage);


    public Bitmap preProcessImage(ExtendedBitmap extendedBitmap) {

        Bitmap bitmap = extendedBitmap.getBitmap();
        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
}


    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

}
