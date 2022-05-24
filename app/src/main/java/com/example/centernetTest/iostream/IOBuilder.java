package com.example.centernetTest.iostream;

import android.content.Context;

public class IOBuilder {

    public static IOStream getIOStream(Context context, int type) {
        switch (type) {
            case 0:
                return IOBuilder.camera();
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private static IOStream camera() {
        return null;
    }
}
