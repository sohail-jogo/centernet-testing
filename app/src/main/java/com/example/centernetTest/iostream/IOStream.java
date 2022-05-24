package com.example.centernetTest.iostream;

//This is an interface that allows for opening/installing streams

import com.example.centernetTest.InfoBlob;

public interface IOStream {

    void onImageProcessed(InfoBlob infoBlob);

}
