package com.example.centernetTest.models;

import com.example.centernetTest.InfoBlob;

import java.util.List;

public interface ModelObserver {
    void parse( InfoBlob infoBlob);

    void setModel(ObservableModel model);

    ModelManager.MODELTYPE getModelType();


}
