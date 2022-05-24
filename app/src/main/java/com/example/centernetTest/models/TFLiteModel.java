package com.example.centernetTest.models;

import android.content.Context;
import android.util.Pair;

import com.example.centernetTest.InfoBlob;
import com.example.centernetTest.utils.ExtendedMLImage;
import com.google.android.odml.image.BitmapExtractor;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;


public abstract class TFLiteModel extends Model {

    protected static final float IMAGE_MEAN = 128.0f;
    protected static final float IMAGE_STD = 128.0f;

    final String modelFileName;
    /*
     *
     *
     * Initializing the model
     *
     *
     */
    protected int imageSizeY;
    protected int imageSizeX;
    protected DataType imageDataType;
    protected int afterPreProcessFrameId;
    protected int supplyFramePreProcessId;
    protected FlowableEmitter<Pair<ExtendedMLImage, InfoBlob>> sourceEmitter;
    protected Flowable<Pair<ExtendedMLImage, InfoBlob>> pipeline;
    Device device = Device.GPU;
    Interpreter detectionModel;
    ImageProcessor imageProcessor;



    public TFLiteModel(float confidenceScore, String modelFileName) {
        super(confidenceScore);
        this.modelFileName = modelFileName;
        buildPipeline();

    }

    @Override
    public void loadModel(Context context) throws IOException {
        MappedByteBuffer tfliteModel
                = FileUtil.loadMappedFile(context,
                modelFileName);

        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        this.device = compatList.isDelegateSupportedOnThisDevice() ? Device.GPU : Device.CPU;

        switch (device) {
            case CPU:
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
    }

    protected ImageProcessor buildImageProcessor() {

        int deviceRotation = 90;

        int imageSizeXRotated = deviceRotation%180==90 ? imageSizeX:imageSizeY;
        int imageSizeYRotated = deviceRotation%180==90 ? imageSizeY:imageSizeX;

        return new ImageProcessor.Builder()
                .add(new ResizeOp(imageSizeXRotated, imageSizeYRotated, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op((int)-deviceRotation/90))// rotates counterclockwise... ?
                .add(new NormalizeOp(IMAGE_MEAN, IMAGE_STD))
                .build();
    }

    /*
     *
     *
     * Building the Pipeline
     *
     *
     */
    protected Flowable<Pair<TensorImage, InfoBlob>> preProcessImage(Pair<ExtendedMLImage, InfoBlob> extendedMlImageInfoBlobPair) {
        TensorImage tensorImage = TensorImage.fromBitmap(BitmapExtractor.extract((extendedMlImageInfoBlobPair.first.getMlImage())));
        tensorImage = imageProcessor.process(tensorImage);
       return Flowable.just(new Pair<>(tensorImage, extendedMlImageInfoBlobPair.second));
    }

    protected abstract Flowable<InfoBlob> RunModel(Pair<TensorImage, InfoBlob> extendedMlImageInfoBlobPair);

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
    public void supplyFrameInternal(ExtendedMLImage mlImage) {
        if (!running) return;
        InfoBlob infoBlob = new InfoBlob(mlImage);
        supplyFramePreProcessId = infoBlob.getFrameID();
        sourceEmitter.onNext(new Pair<>(mlImage, infoBlob));
    }

    @Override
    public void stop() {
        super.stop();
        sourceEmitter.onComplete();
        pipeline = null;
        sourceEmitter = null;
        if (detectionModel != null) detectionModel.close();
    }


    enum Device {
        NNAPI,
        GPU,
        CPU
    }


}
