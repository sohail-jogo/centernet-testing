package com.example.centernetTest.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.example.centernetTest.InfoBlob;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Flowable;


public class SSD extends TFLiteModel {

    final int labelOffset;
    final String labelFileName;
    protected List<String> labels = new Vector<>();
    int MAXDETECTIONS = 10;
    long waitingTime = 0;
    int frameID;

    public SSD(String modelFileName, String labelFileName, int labelOffset) {
        super(0.55f, modelFileName);
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

    public void loadModel(Context context) throws IOException {
        super.loadModel(context);
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
    protected Flowable<InfoBlob> RunModel(Pair<TensorImage, InfoBlob> blob) {
        if (!running) return null;

        TensorImage inputImage = blob.first;
        frameID = blob.second.getFrameID();

        Map<Integer, Object> outputmap = buildOutputMap();
        detectionModel.runForMultipleInputsOutputs(new Object[]{inputImage.getBuffer()}, outputmap);

        float[][][] outputLocations = (float[][][]) outputmap.get(0);

        float[][] outputClasses = (float[][]) outputmap.get(1);

        float[][] outputScores = (float[][]) outputmap.get(2);

        float[] numDetections = (float[]) outputmap.get(3);

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

            Log.d("SSD DETECTION ", " x: " + rectF.centerX() + " - y: " + rectF.centerY());

        }
        waitingTime = SystemClock.elapsedRealtime();

        return Flowable.just(blob.second);
    }
}
