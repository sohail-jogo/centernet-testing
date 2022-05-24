package com.example.centernetTest.models;

import android.content.Context;

import com.example.centernetTest.InfoBlob;
import com.example.centernetTest.utils.ExtendedMLImage;

import java.io.IOException;
import java.util.List;

public interface ObservableModel {

    void addObservers(List<ModelObserver> observers);

    void addObserver(ModelObserver observer);

    void removeObserver(ModelObserver observer);

    void notifyListeners(InfoBlob infoBlob);

    void loadModel(Context context) throws IOException;

    void supplyFrame(ExtendedMLImage mlImage);

    void start();

    void stop();

}
