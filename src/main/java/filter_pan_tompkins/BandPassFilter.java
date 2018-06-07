package filter_pan_tompkins;

public class BandPassFilter {

    /**
     * https://ac.els-cdn.com/S221201731200415X/1-s2.0-S221201731200415X-main.pdf?_tid=a343adcf-dbce-467f-bb8e-2c8a30e590d5&acdnat=1527509300_5bb74e091b0adb39e98a22c1486e6cf9
     * p (nT) = x(nT – 16T) – 0.03125 [y(nT – T) + x(nT) – x(nT – 32T)]
     *
     * @param input
     * @return
     */

    public static float[] filter(float[] input) {

        float[] output = new float[input.length];

        for (int i = 0; i < input.length; i++) {
            if (i < 32) {
                output[i] = input[i];
            } else {
                output[i] = input[i - 16] - 0.03125f * (output[i - 1] + output[i] - input[i - 32]);
            }
        }

        return output;
    }

    public static float[] cascade(float[] input) {
        return HighPassFilter.filter(LowPassFilter.filter(input));
    }
}
