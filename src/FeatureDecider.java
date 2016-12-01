import global.svm_predict;
import global.svm_train;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fredrik on 23/11/2016.
 */
public class FeatureDecider {

    private ArrayList<String> featuresAvailableTraining;
    private double globalBestAcc = 0;
    private int bestIndex = 0;

    public FeatureDecider() throws IOException {
        final ArrayList<boolean[]> boolArrays = buildBoolArrays(16);

        FileCombiner fc = new FileCombiner();
        initializeArray();
        int iters = 1;
        final String[] predictFiles = { "-b", "1", "-q", "featuresValidationModified.train", "featuresTrainModified.train.model",
                "probabilityForController.out" };
        final String[] trainingFiles = { "-b", "1", "-q", "featuresTrainModified.train" };
        //for (int i = boolArrays.size()-1; i>= 0; i--) {
        for (int i = 0; i < boolArrays.size(); i++) {
            boolean[] bools = boolArrays.get(i);
            int numTrue = 0;
            for (boolean shouldUseFeature : bools) {
                if (shouldUseFeature) numTrue++;
            }
            System.out.println("num true is  " + numTrue);
            if (numTrue == 0) {
                continue;
            }
            int filesDeleted = 0;
            try {
                File f = new File("featuresTrainModified.txt");
                if (f.delete()){
                    filesDeleted++;
                }
                f = new File("featuresValidationModified.txt");
                if (f.delete()){
                    filesDeleted++;
                }
            } catch(RuntimeException e) {
                System.out.println(e.toString());
            }
            buildFeatureFile(bools, "featuresTrainingBinless.txt", "featuresTrainModified.txt");
            buildFeatureFile(bools, "featuresValidationBinless.txt", "featuresValidationModified.txt");

            BufferedReader br = new BufferedReader(new FileReader("featuresValidationModified.txt"));
            String lineOne = br.readLine();
            String[] splitted = lineOne.split(",");
            System.out.println("features: " + splitted.length);

            try {
                File f = new File("featuresTrainModified.train");
                if (f.delete()){
                    filesDeleted++;
                }
                f = new File("featuresValidationModified.train");

                if (f.delete()){
                    filesDeleted++;
                }
            } catch(RuntimeException e) {
                System.out.println(e.toString());
            }

            if(filesDeleted!=4){
                System.out.println("DIDNT DELETE ALL FILES");
            }

            fc.buildFile(numTrue, "featuresTrainModified.txt");
            fc.buildFile(numTrue, "featuresValidationModified.txt");
            svm_train.main(trainingFiles);
            svm_predict.main(predictFiles);
            updateBest(i);
            System.out.println("finished iter " + iters);
            iters++;
        }
        ArrayList<String> bestFeatures = new ArrayList<>();
        boolean[] best = boolArrays.get(bestIndex);
        for (int i = 0; i < best.length; i++) {
            if (best[i]) {
                bestFeatures.add(featuresAvailableTraining.get(i));
            }
        }

        System.out.println("Optimally use " + bestFeatures.size() + " features");
        bestFeatures.forEach(System.out::println);
        System.out.println("best acc was " + globalBestAcc);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("bestFeaturesToUse.txt", true)))) {
            writer.write("Best acc was " + globalBestAcc + "\n");
            for (String feature : bestFeatures) {
                writer.write(feature + "\n");
            }
        }

    }

    private void initializeArray() {
        featuresAvailableTraining = new ArrayList<>();
        featuresAvailableTraining.addAll(Arrays.asList("numPlayerSprites", "numNPC", "numPortalTypes", "numTypesResources", "avatarHasResources",
                "isResourcesAvailable", "canShoot", "numNPCTypes", "numTypesResourcesAvatar", "blockSize", "numPortals", "numMovableSprites",
                "isUseAvailable", "canMoveVertically", "numImmovableSprites", "worldSize"));
    }

    private void updateBest(int currIndex) throws IOException, FileNotFoundException {
        try (BufferedReader feBr = new BufferedReader(new FileReader("result.out"))) {
            String accuracy = feBr.readLine();
            double acc = Double.parseDouble(accuracy);
            if (acc > globalBestAcc) {
                globalBestAcc = acc;
                bestIndex = currIndex;

                File source = new File("featuresTrainModified.train");
                Path sourcep = source.toPath();
                File dest = new File("BESTfeaturesTrainModified.train");
                Path destp = dest.toPath();
                Files.copy(sourcep, destp, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("curr acc was " + acc + " curr best is " + globalBestAcc);
        }
    }


    private void buildFeatureFile(boolean[] indexes, String filenameIn, String filenameOut) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filenameOut, false)
        ));
        BufferedReader feBr = new BufferedReader(new FileReader(filenameIn));
        while(true){
            String feature = feBr.readLine();
            if(feature == null){
                break;
            }
            String subStr = feature;
            for (int i = 0; i < indexes.length; i++){
                if (indexes[i]) continue;

                String regex;
                if (i == featuresAvailableTraining.size() - 1) {
                    regex = ".." + featuresAvailableTraining.get(i) + "=.+?(?=})";
                }
                else {
                    regex = featuresAvailableTraining.get(i) + "=.+?(?=[a-z])";
                }
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(subStr);
                while(m.find()) {
                    subStr = m.replaceAll("");
                }
            }
            writer.write(subStr + "\n");
            writer.flush();
        }
        writer.close();
    }

    private ArrayList<boolean[]> buildBoolArrays(final int n){
        ArrayList<boolean[]> res = new ArrayList<>();
        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n)
                bin = "0" + bin;
            char[] chars = bin.toCharArray();
            boolean[] boolArray = new boolean[n];
            for (int j = 0; j < chars.length; j++) {
                boolArray[j] = chars[j] == '0' ? true : false;
            }
            res.add(boolArray);
        }
        return res;
    }
}

