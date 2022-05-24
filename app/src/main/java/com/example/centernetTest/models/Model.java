package com.example.centernetTest.models;

import android.util.Pair;

import com.example.centernetTest.InfoBlob;
import com.example.centernetTest.iostream.IOStream;
import com.example.centernetTest.utils.ExtendedMLImage;
import java.util.ArrayList;
import java.util.List;

public abstract class Model implements ObservableModel {
    /*
     *
     *
     * Observers
     *
     *x
     */
    protected final List<ModelObserver> observers = new ArrayList<>();
    protected float confidenceScore;
    protected boolean running;
    IOStream ioStream;

    int maxfps = -1;
    long lastFrameTime = 0;
    /*
     *
     *
     * Exercise Lead
     *
     *
     */
    private boolean exerciseLead = false;
    private int lastId = 0;

    public Model(float confscore) {
        this.confidenceScore = confscore;
    }

    public void setConfidenceScore(float confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isExerciseLead() {
        return exerciseLead;
    }

    public void setExerciseLead(boolean exerciseLead) {
        this.exerciseLead = exerciseLead;
    }

    public void setIoStream(IOStream ioStream) {
        this.ioStream = ioStream;
    }


    @Override
    public void addObserver(ModelObserver observer) {
        this.observers.add(observer);
        observer.setModel(this);
    }

    /*
     *
     *
     * General Model Functions
     *
     *
     */

    @Override
    public void addObservers(List<ModelObserver> observers) {

        observers.forEach(observer -> {
            this.observers.add(observer);
            observer.setModel(this);
        });
    }

    @Override
    public void removeObserver(ModelObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyListeners( InfoBlob infoBlob) {

        observers.forEach(observer -> observer.parse(infoBlob));
    }

    protected void distributeLocations(InfoBlob argPair) {
        if (argPair == null) return;

    }



    @Override
    public void supplyFrame(ExtendedMLImage mlImage) {
        if (!running) return;
        //if the last time since frame is to low, we skip the frame
        if (maxfps != -1 && (System.currentTimeMillis() - lastFrameTime) < 1000 / maxfps) return;
        lastFrameTime = System.currentTimeMillis();
        supplyFrameInternal(mlImage);
    }

    abstract void supplyFrameInternal(ExtendedMLImage mlImage);


    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

}
