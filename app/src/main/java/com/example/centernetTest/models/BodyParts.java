package com.example.centernetTest.models;

import com.google.mlkit.vision.pose.PoseLandmark;

public interface BodyParts {


    enum BODYPART {
        NOSE(PoseLandmark.NOSE, "NOSE"),
        LEFT_EYE_INNER(PoseLandmark.LEFT_EYE_INNER, "LEFT_EYE_INNER"),
        LEFT_EYE(PoseLandmark.LEFT_EYE, "LEFT_EYE"),
        LEFT_EYE_OUTER(PoseLandmark.LEFT_EYE_OUTER, "LEFT_EYE_OUTER"),
        RIGHT_EYE_INNER(PoseLandmark.RIGHT_EYE_INNER, "RIGHT_EYE_INNER"),
        RIGHT_EYE(PoseLandmark.RIGHT_EYE, "RIGHT_EYE"),
        RIGHT_EYE_OUTER(PoseLandmark.RIGHT_EYE_OUTER, "RIGHT_EYE_OUTER"),
        LEFT_EAR(PoseLandmark.LEFT_EAR, "LEFT_EAR"),
        RIGHT_EAR(PoseLandmark.RIGHT_EAR, "RIGHT_EAR"),
        LEFT_MOUTH(PoseLandmark.LEFT_MOUTH, "LEFT_MOUTH"),
        RIGHT_MOUTH(PoseLandmark.RIGHT_MOUTH, "RIGHT_MOUTH"),
        LEFT_SHOULDER(PoseLandmark.LEFT_SHOULDER, "LEFT_SHOULDER"),
        RIGHT_SHOULDER(PoseLandmark.RIGHT_SHOULDER, "RIGHT_SHOULDER"),
        LEFT_ELBOW(PoseLandmark.LEFT_ELBOW, "LEFT_ELBOW"),
        RIGHT_ELBOW(PoseLandmark.RIGHT_ELBOW, "RIGHT_ELBOW"),
        LEFT_WRIST(PoseLandmark.LEFT_WRIST, "LEFT_WRIST"),
        RIGHT_WRIST(PoseLandmark.RIGHT_WRIST, "RIGHT_WRIST"),
        LEFT_PINKY(PoseLandmark.LEFT_PINKY, "LEFT_PINKY"),
        RIGHT_PINKY(PoseLandmark.RIGHT_PINKY, "RIGHT_PINKY"),
        LEFT_INDEX(PoseLandmark.LEFT_INDEX, "LEFT_INDEX"),
        RIGHT_INDEX(PoseLandmark.RIGHT_INDEX, "RIGHT_INDEX"),
        LEFT_THUMB(PoseLandmark.LEFT_THUMB, "LEFT_THUMB"),
        RIGHT_THUMB(PoseLandmark.RIGHT_THUMB, "RIGHT_THUMB"),
        LEFT_HIP(PoseLandmark.LEFT_HIP, "LEFT_HIP"),
        RIGHT_HIP(PoseLandmark.RIGHT_HIP, "RIGHT_HIP"),
        LEFT_KNEE(PoseLandmark.LEFT_KNEE, "LEFT_KNEE"),
        RIGHT_KNEE(PoseLandmark.RIGHT_KNEE, "RIGHT_KNEE"),
        LEFT_ANKLE(PoseLandmark.LEFT_ANKLE, "LEFT_ANKLE"),
        RIGHT_ANKLE(PoseLandmark.RIGHT_ANKLE, "RIGHT_ANKLE"),
        LEFT_HEEL(PoseLandmark.LEFT_HEEL, "LEFT_HEEL"),
        RIGHT_HEEL(PoseLandmark.RIGHT_HEEL, "RIGHT_HEEL"),
        LEFT_FOOT_INDEX(PoseLandmark.LEFT_FOOT_INDEX, "LEFT_FOOT_INDEX"),
        RIGHT_FOOT_INDEX(PoseLandmark.RIGHT_FOOT_INDEX, "RIGHT_FOOT_INDEX");

        public int part;
        public String label;

        BODYPART(int part, String label) {
            this.part = part;
            this.label = label;
        }
    }


    enum NON_MLKIT_BODYPARTS {
        NOSE("NOSE"),
        LEFT_EYE("LEFT_EYE"),
        RIGHT_EYE("RIGHT_EYE"),
        LEFT_EAR("LEFT_EAR"),
        RIGHT_EAR("RIGHT_EAR"),
        LEFT_SHOULDER("LEFT_SHOULDER"),
        RIGHT_SHOULDER("RIGHT_SHOULDER"),
        LEFT_ELBOW("LEFT_ELBOW"),
        RIGHT_ELBOW("RIGHT_ELBOW"),
        LEFT_WRIST("LEFT_WRIST"),
        RIGHT_WRIST("RIGHT_WRIST"),
        LEFT_HIP("LEFT_HIP"),
        RIGHT_HIP("RIGHT_HIP"),
        LEFT_KNEE("LEFT_KNEE"),
        RIGHT_KNEE("RIGHT_KNEE"),
        LEFT_ANKLE("LEFT_ANKLE"),
        RIGHT_ANKLE("RIGHT_ANKLE");
        public final String label;
        NON_MLKIT_BODYPARTS(String label) {
            this.label = label;
        }
    }
}
