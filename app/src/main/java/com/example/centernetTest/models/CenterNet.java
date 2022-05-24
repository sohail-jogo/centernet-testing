package com.example.centernetTest.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.example.centernetTest.InfoBlob;
import com.example.centernetTest.utils.ExtendedMLImage;
import com.google.android.odml.image.BitmapExtractor;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class CenterNet extends TFLiteModel {

    final int labelOffset;
    final String labelFileName;
    final private int NUM_THREADS = 4;
    final private boolean USE_XNN_PACK = true;
    protected List<String> labels = new Vector<>();
    long waitingTime = 0;
    int frameID;
    int MAXDETECTIONS = 10;


    public CenterNet(String modelFileName, String labelFileName, int labelOffset) {
        super(0.5f, modelFileName);
        this.labelOffset = labelOffset;
        this.labelFileName = labelFileName;
    }


    public void loadLabels(AssetManager assetManager) throws IOException {
        InputStream labelsInput = assetManager.open(labelFileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        labelsInput.close();
        br.close();
    }


    @Override
    public void loadModel(Context context) throws IOException {
        MappedByteBuffer tfliteModel
                = FileUtil.loadMappedFile(context,
                modelFileName);

        Interpreter.Options options = (new Interpreter.Options());
        switch (device) {
            case CPU:
                options.setNumThreads(NUM_THREADS);
                options.setUseXNNPACK(USE_XNN_PACK);
                break;
            case GPU:
                GpuDelegate delegate = new GpuDelegate();
                options.addDelegate(delegate);
                break;
            case NNAPI:
                NnApiDelegate nnApiDelegate = new NnApiDelegate();
                options.addDelegate(nnApiDelegate);
                break;
            default:
                throw new IllegalStateException("Unexpected Device Value " + device);
        }

        detectionModel = new Interpreter(tfliteModel, options);

        int imageTensorIndex = 0;
        int[] imageShape = detectionModel.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}

        imageDataType = detectionModel.getInputTensor(imageTensorIndex).dataType();
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];

        imageProcessor = buildImageProcessor();

        loadLabels(context.getAssets());
    }


    protected Map<Integer, Object> buildOutputMap() {

        int[] shape = detectionModel.getOutputTensor(0).shape();

        float[][][] outputLocations = new float[shape[0]][shape[1]][shape[2]];

        shape = detectionModel.getOutputTensor(1).shape();

        float[][] outputClasses = new float[shape[0]][shape[1]];

        shape = detectionModel.getOutputTensor(2).shape();
        float[][] outputScores = new float[shape[0]][shape[1]];

        shape = detectionModel.getOutputTensor(3).shape();
        float[] numDetections = new float[shape[0]];

        Map<Integer, Object> outputMap = new ConcurrentHashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        return outputMap;
    }

    @Override
    protected ImageProcessor buildImageProcessor() {

        int deviceRotation = 90;

        int imageSizeXRotated = deviceRotation%180==90 ? imageSizeX:imageSizeY;
        int imageSizeYRotated = deviceRotation%180==90 ? imageSizeY:imageSizeX;

        return new ImageProcessor.Builder()
                .add(new ResizeOp(imageSizeXRotated, imageSizeYRotated, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op((int)-deviceRotation/90))// rotates counterclockwise... ?
                .build();
    }

    @Override
    protected Flowable<Pair<TensorImage, InfoBlob>> preProcessImage(Pair<ExtendedMLImage, InfoBlob> extendedMlImageInfoBlobPair) {

        TensorImage tensorImage = TensorImage.fromBitmap(BitmapExtractor.extract((extendedMlImageInfoBlobPair.first.getMlImage())));
        tensorImage = imageProcessor.process(tensorImage);
        TensorImage ntensorImage = TensorImage.createFrom(tensorImage, DataType.FLOAT32);

        return Flowable.just(new Pair<>(ntensorImage, extendedMlImageInfoBlobPair.second));
    }

    @Override
    protected void buildPipeline() {
        pipeline = Flowable.create(emitter -> sourceEmitter = emitter, BackpressureStrategy.LATEST);
        pipeline.observeOn(Schedulers.computation())
                .filter((blob) -> supplyFramePreProcessId == blob.second.getFrameID())
                .concatMapEager(this::preProcessImage)
                .doOnNext((blob) -> afterPreProcessFrameId = blob.second.getFrameID())
                .observeOn(Schedulers.computation())
                .filter((blob) -> afterPreProcessFrameId == blob.second.getFrameID())
                .concatMap(this::RunModel)
                 .subscribe(this::distributeLocations);

    }



    @Override
    public Flowable<InfoBlob> RunModel(Pair<TensorImage, InfoBlob> blob){
        if (!running) return null;

        Map<Integer, Object> outputMap = buildOutputMap();
       // detectionModel.run(blob.first.getBuffer(), outputMap);

        frameID = blob.second.getFrameID();

        // Run the inference call.
        detectionModel.runForMultipleInputsOutputs(new Object[]{blob.first.getBuffer()}, outputMap);

        float[][][] outputLocations = (float[][][]) outputMap.get(0);
        float[][] outputClasses = (float[][]) outputMap.get(1);
        float[][] outputScores = (float[][]) outputMap.get(2);
        float[] numDetections = (float[]) outputMap.get(3);

        int numDetectionsOutput = Math.min(MAXDETECTIONS, (int) numDetections[0]); // cast from float to integer, use min for safety


        for (int i = 0; i < numDetectionsOutput; ++i) {
            if (outputScores[0][i] < confidenceScore) continue;

            //TODO if an object is square on camera, it will be projected as rectangular in cropped dimensions.
            float left = outputLocations[0][i][1];
            float top = outputLocations[0][i][0];
            float right = outputLocations[0][i][3];
            float bottom = outputLocations[0][i][2];

            RectF rectF = new RectF(
                    left,
                    top,
                    right,
                    bottom);

//            DetectionLocation detection = new RectLocation(
//                    rectF,
//                    labels.get((int) outputClasses[0][i] + labelOffset),
//                    frameID,
//                    outputScores[0][i]
//            );
            Log.d("CENTERNET DETECTION ", " x: " + rectF.centerX() + " - y: " + rectF.centerY());

        }

        waitingTime = SystemClock.elapsedRealtime();

        return Flowable.just(blob.second);
    }

}
