package qrs;


import model.Sample;
import utils.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QrsDetection {

    private float[] ecgSignal;

    public QrsDetection(float[] ecgSignal) {
        this.ecgSignal = ecgSignal;
    }

    private static boolean isQRS(float[] input) {
        for (int i = 0; i < 5; i++) {
            if (input[i] > Math.THRESHOLD_VALUE)
                return false;
        }
        for (int i = 5; i < 10; i++) {
            if (input[i] < Math.THRESHOLD_VALUE)
                return false;
        }
        return true;
    }

    private Sample searchPeak(int sampleId) {
        if (ecgSignal[sampleId - 1] < ecgSignal[sampleId + 1] && ecgSignal[sampleId] < ecgSignal[sampleId + 1])
            return new Sample(sampleId + 1, ecgSignal[sampleId + 1]);

        else if (ecgSignal[sampleId - 1] > ecgSignal[sampleId] && ecgSignal[sampleId - 1] > ecgSignal[sampleId + 1])
            return new Sample(sampleId + 1, ecgSignal[sampleId + 1]);

        else return new Sample(sampleId, ecgSignal[sampleId]);

    }

    private static Sample getPeak(List<Sample> samples) {
        float max = samples.get(0).getValue();
        int id = samples.get(0).getId();

        for (int i = 1; i < samples.size(); i++) {
            if (samples.get(i).getValue() > max) {
                max = samples.get(i).getValue();
                id = samples.get(i).getId();
            }
        }

        return new Sample(id, max);
    }

    public List<Sample> detect(float[] input) {
        List<Sample> temp = new ArrayList<>();
        List<Sample> peaks = new ArrayList<>();
        int peakId;

        input = Normalization.normalize(input);

        for (int i = 5; i < input.length; i++) {

            if (input[i] / Math.max(input) > Math.THRESHOLD_VALUE) {

                if (isQRS(Arrays.copyOfRange(input, i - 5, i + 5))) {
                    while (input[i] / Math.max(input) > Math.THRESHOLD_VALUE && i < input.length - 1) {
                        temp.add(new Sample(i, input[i] / Math.max(input)));
                        i++;
                    }
                    peakId = getPeak(temp).getId();
                    peaks.add(searchPeak(peakId));
                    temp.clear();
                }
            }
        }

        return peaks;
    }

}