package qrs;

import utils.Math;

public class Derivative {

    public static float[] derivative(float[] input) {
        float[] output = new float[input.length];
        output[0] = input[0];
        for (int i = 1; i < input.length; i++) {
            output[i] = Math.SAMPLING_FREQUENCY * (input[i] - input[i - 1]);
        }
        return output;
    }

}
