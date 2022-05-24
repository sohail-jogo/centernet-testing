package com.example.centernetTest.models;

public class FootBallv17CenterNet extends CenterNet {

    public FootBallv17CenterNet() {
        super("centernet_v20.tflite",
                "football_labels.txt",
                0);
    }
}
