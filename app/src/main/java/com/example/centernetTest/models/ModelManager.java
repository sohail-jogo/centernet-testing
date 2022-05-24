package com.example.centernetTest.models;

import android.content.Context;

import com.example.centernetTest.iostream.IOStream;
import com.example.centernetTest.utils.ExtendedMLImage;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelManager {

    private static ModelManager modelManager = null;

    private final IOStream ioStream;
    private final Map<MODELTYPE, Model> modelHashMap = new ConcurrentHashMap<>();
    private final Context context;

    private ModelManager(IOStream ioStream, Context context) {
        this.ioStream = ioStream;
        this.context = context;

        MODELTYPE modeltype = MODELTYPE.CENTERNET; // CHANGE TO FOOTBALLv16, if u want to run SSD model.

        Model model = modelHashMap.get(modeltype);
       // if (model == null) {
            try {
                model = instantiateModel(modeltype);
                model.loadModel(context);
                model.setIoStream(ioStream);
                model.start();
                modelHashMap.put(modeltype, model);
            } catch (IOException e) {
            }

    }

    public static ModelManager getInstance() {
        return modelManager;
    }

    public static ModelManager createInstance(IOStream ioStream, Context context) {
        synchronized (ModelManager.class) {
            if (modelManager == null)
                modelManager = new ModelManager(ioStream, context);
            return modelManager;
        }


    }

    public Model instantiateModel(MODELTYPE modelManagerType) {
        Model model;
        switch (modelManagerType) {
            case FOOTBALLv16:
                model = new Footballv16SSD();
                break;
            case CENTERNET:
                model = new FootBallv17CenterNet();
                break;
//            case POSENET:
//                model = new PoseNet();
//                break;
//            case POSENET_FASTMODE:
//                model = new PoseNetFastMode();
//                break;
//            case OLD_POSENET:
//                model = new OldPoseNet();
//                break;
//            case MOVE_NET_LIGHTNING:
//                model = new MoveNetLightning();
//                break;
//            case MOVE_NET_THUNDER:
//                model = new MoveNetThunder();
//                break;
            case SKIP:
                model = null;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + modelManagerType);
        }
        return model;
    }

    public void stop() {
        synchronized (this) {
            modelHashMap.values().forEach(Model::stop);
        }
        modelManager = null;
    }

    public Collection<Model> getModels() {
        return modelHashMap.values();
    }

    public void subscribe(ModelObserver observer) {
        //subscribe an observer to the correct model
        MODELTYPE modeltype = observer.getModelType();
        if (modeltype == MODELTYPE.SKIP) return;


        Model model = modelHashMap.get(modeltype);
        if (model == null) {
            try {
                model = instantiateModel(modeltype);
                model.loadModel(context);
                model.setIoStream(ioStream);
                model.start();
                modelHashMap.put(modeltype, model);
            } catch (IOException e) {
            }
        }
        model.addObserver(observer);
    }

    public void unsubscribe(ModelObserver observer) {
        MODELTYPE modeltype = observer.getModelType();
        if (modeltype == MODELTYPE.SKIP) return;

        Model model = modelHashMap.get(modeltype);
        if (model == null) return; // weird
        model.removeObserver(observer);
    }

    public void supplyFrame(ExtendedMLImage extendedMLImage) {

        modelHashMap.values().stream().forEach(model -> model.supplyFrame(extendedMLImage));
    }

    public enum MODELTYPE {
        FOOTBALLv16,
        POSENET,
        CENTERNET,
        SKIP,
        POSENET_FASTMODE,
        OLD_POSENET,
        MOVE_NET_LIGHTNING,
        MOVE_NET_THUNDER
    }

}
