package research_aspect;

import filter_iirj.ButterworthFilter;
import model.Sample;
import parameters.HeartRate;
import parameters.HeartRateVariability;
import parameters.PrematureVentricularContractions;
import qrs.Derivative;
import qrs.Integration;
import qrs.QrsDetection;
import qrs.Squaring;
import utils.ReadCardioPathSimple;

import java.util.ArrayList;
import java.util.List;

public class ResearchAspect {

    private static final String INTERVALS_XLS_DIRECTORY = "C:\\Users\\Damian\\Desktop\\mitdb\\mitdb-intervalsRR\\xls\\";
    private static final String INTERVALS_TXT_DIRECTORY = "C:\\Users\\Damian\\Desktop\\mitdb\\mitdb-intervalsRR\\txt\\";
    private static final String ECG_SIGNAL_CSV_DIRECTORY = "C:\\Users\\Damian\\Desktop\\mitdb\\mitdb-ecgSignal\\";


    public static boolean isFoundIdInIntervalsRRCalculatedByApplication(List<Sample> intervalsRR, int id) {
        for (int i = 0; i < intervalsRR.size(); i++) {
            if ((intervalsRR.get(i).getId() - id <= 5 && intervalsRR.get(i).getId() - id >= 0) || (id - intervalsRR.get(i).getId() <= 5 && id - intervalsRR.get(i).getId() >= 0))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {

        DataHelper dataHelper = new DataHelper();
        List<List<Sample>> intervalsRRWithPVCs = new ArrayList<>();
        List<List<Sample>> intervalsRRWithoutPVCs = new ArrayList<>();
        List<List<Sample>> intervalsRRCalculatedByApplication = new ArrayList<>();

        String[] filesNames = {"100", "101", "102", "103", "104", "105", "106", "107", "108", "109",
                "111", "112", "113", "114", "115", "116", "117", "118", "119",
                "121", "122", "123", "124", "200", "201", "202", "203", "205", "207",
                "208", "209", "210", "212", "213", "214", "215", "217", "219", "220",
                "221", "222", "223", "228", "230", "231", "232", "233", "234"};

        String[] csvFiles = new String[filesNames.length];
        for (int i = 0; i < filesNames.length; i++) {
            csvFiles[i] = filesNames[i] + ".csv";
        }

        String[] txtFiles = new String[filesNames.length];
        for (int i = 0; i < filesNames.length; i++) {
            txtFiles[i] = filesNames[i] + ".txt";
        }

        String[] xlsFiles = new String[filesNames.length];
        for (int i = 0; i < filesNames.length; i++) {
            xlsFiles[i] = filesNames[i] + ".xlsx";
        }

        for (int i = 0; i < txtFiles.length; i++) {
            dataHelper.txtToXls(INTERVALS_TXT_DIRECTORY + txtFiles[i], INTERVALS_XLS_DIRECTORY + xlsFiles[i]);
        }

        for (int i = 0; i < xlsFiles.length; i++) {
            intervalsRRWithPVCs.add(dataHelper.xlsToIntervalsRR(INTERVALS_XLS_DIRECTORY + xlsFiles[i], true));
        }

        for (int i = 0; i < xlsFiles.length; i++) {
            intervalsRRWithoutPVCs.add(PrematureVentricularContractions.detectPVCs(
                    dataHelper.xlsToIntervalsRR(INTERVALS_XLS_DIRECTORY + xlsFiles[i], false),
                    HeartRate.averageIntervalRR(dataHelper.xlsToIntervalsRR(INTERVALS_XLS_DIRECTORY + xlsFiles[i], false))));
        }

        for (int i = 0; i < csvFiles.length; i++) {
            int signalNumber = 1;
            float[] inputSignal = ReadCardioPathSimple.loadCSV(ECG_SIGNAL_CSV_DIRECTORY + csvFiles[i], signalNumber, 0, 108000);
            QrsDetection qrsDetection = new QrsDetection(inputSignal);
            List<Sample> peaks = qrsDetection.detect(Integration.integration(Squaring.squaring(Derivative.derivative(ButterworthFilter.filter(inputSignal, 360.0f), 360.0f)), 360.0f));
            intervalsRRCalculatedByApplication.add(HeartRateVariability.getIntervalsRR(peaks, 360.0f));
        }

        System.out.println("Data number: " + csvFiles.length);

        int globalAllPVCs = 0;
        int globalDetectedPVCs = 0;
        for (int i = 0; i < intervalsRRWithPVCs.size(); i++) {
            int allPVCs = 0;
            int detectedPVCs = 0;
            for (int j = 0; j < intervalsRRWithPVCs.get(i).size(); j++) {
                if (intervalsRRWithPVCs.get(i).get(j).isPVC()) {
                    if (intervalsRRWithoutPVCs.get(i).get(j).isPVC()) {
                        globalDetectedPVCs++;
                        detectedPVCs++;
                    }
                    globalAllPVCs++;
                    allPVCs++;
                }
            }
            System.out.println(filesNames[i] + " (PVCs): " + detectedPVCs + "/" + allPVCs + " = " + (detectedPVCs * 100.0f / allPVCs * 1.0f));
        }
        System.out.println("Global detected PVCs: " + globalDetectedPVCs + "/" + globalAllPVCs + " = " + (globalDetectedPVCs * 100.0f / globalAllPVCs * 1.0f));

        int TP = 0;
        int FP = 0;
        int FN = 0;
        int TN = 0;

        for (int i = 0; i < intervalsRRWithPVCs.size(); i++) {
            for (int j = 0; j < intervalsRRWithPVCs.get(i).size(); j++) {
                if (intervalsRRWithPVCs.get(i).get(j).isPVC()) {
                    if (intervalsRRWithoutPVCs.get(i).get(j).isPVC()) {
                        TP++;
                    } else {
                        FN++;
                    }
                } else {
                    if (intervalsRRWithoutPVCs.get(i).get(j).isPVC()) {
                        FP++;
                    } else {
                        TN++;
                    }
                }
            }
        }

        System.out.println("TP: " + TP);
        System.out.println("FN: " + FN);
        System.out.println("FP: " + FP);
        System.out.println("TN: " + TN);

        float TPR = (TP * 1.0f) / ((TP + FN) * 1.0f);
        System.out.println("TPR: " + TPR);

        float TNR = (TN * 1.0f) / ((FP + TN) * 1.0f);
        System.out.println("TNR: " + TNR);

        float PPV = (TP * 1.0f) / ((TP + FP) * 1.0f);
        System.out.println("PPV: " + PPV);

        float ACC = ((TP + TN) * 1.0f) / ((TP + TN + FP + FN) * 1.0f);
        System.out.println("ACC: " + ACC);


        int globalAllPeaks = 0;
        int globalDetectedPeaks = 0;

        for (int i = 0; i < intervalsRRWithPVCs.size(); i++) {
            int allPeaks = 0;
            int detectedPeaks = 0;
            for (int j = 0; j < intervalsRRWithPVCs.get(i).size(); j++) {
                if (intervalsRRWithPVCs.get(i).get(j).getId() > 108000) {
                    break;
                }
                if (isFoundIdInIntervalsRRCalculatedByApplication(intervalsRRCalculatedByApplication.get(i), intervalsRRWithPVCs.get(i).get(j).getId())) {
                    detectedPeaks++;
                    globalDetectedPeaks++;
                }
                allPeaks++;
                globalAllPeaks++;
            }
            System.out.println(filesNames[i] + " (peaks): " + detectedPeaks + "/" + allPeaks + " = " + (detectedPeaks * 100.0f / allPeaks * 1.0f));
        }
        System.out.println("Global detected peaks: " + globalDetectedPeaks + "/" + globalAllPeaks + " = " + (globalDetectedPeaks * 100.0f / globalAllPeaks * 1.0f));

    }
}
