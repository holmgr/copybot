import global.svm_predict;
import global.svm_train;
import jdk.nashorn.internal.ir.WhileNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fredrik on 23/11/2016.
 */
public class FeatureDecider {

    private ArrayList<String> featuresAvailableTraining;

    private String[] trainingFiles = {"-b", "1", "-q", "featuresTrainModified.train"};
    private String[] predictFiles = {"-b", "1", "-q", "featuresValidationModified.train", "featuresTrainModified.train.model", "probabilityForController.out"};
    private double bestAcc = 0;
    private double accOnCurrLevel = 0;
    private boolean firstrun = true;

    public FeatureDecider() throws IOException {
        FileCombiner fc = new FileCombiner();
        initializeArray();
        while (!featuresAvailableTraining.isEmpty()) {
            int featureToRemove = -1;
            boolean changeFeature = false;
            accOnCurrLevel = 0;
            for (int i = 0; i < featuresAvailableTraining.size(); i++) {
                buildFeatureFile(i, "featuresTraining.txt", "featuresTrainModified.txt");
                buildFeatureFile(i, "featuresValidation.txt", "featuresValidationModified.txt");
                fc.buildFile(featuresAvailableTraining.size() - 1, "featuresTrainModified.txt");
                fc.buildFile(featuresAvailableTraining.size() - 1, "featuresValidationModified.txt");
                svm_train.main(trainingFiles);
                svm_predict.main(predictFiles);
                changeFeature = changeFeature();
                if(changeFeature){
                    featureToRemove = i;
                }
            }
            for(String str : featuresAvailableTraining){
                System.out.println(str);

            }
            System.out.println("\n");
            if(accOnCurrLevel >= bestAcc) featuresAvailableTraining.remove(featureToRemove);
          //  else break;
            System.out.println(accOnCurrLevel);
        }




    }

    private void initializeArray() {
        featuresAvailableTraining = new ArrayList<>();
        featuresAvailableTraining.addAll(Arrays.asList("numPlayerSprites", "numNPC", "numPortalTypes", "numTypesResources", "avatarHasResources",
                "isResourcesAvailable", "canShoot", "numNPCTypes", "numTypesResourcesAvatar", "blockSize", "numPortals", "numMovableSprites",
                "isUseAvailable", "canMoveVertically", "numImmovableSprites", "worldSize"));
    }

    private boolean changeFeature() throws IOException {
        BufferedReader feBr = new BufferedReader(new FileReader("result.out"));
        String accuracy = feBr.readLine();
     /*   if (firstrun){
            bestAcc = Double.parseDouble(accuracy);
            firstrun = false;
        } */

    /*    if(Double.parseDouble(accuracy) > bestAcc){
            bestAcc = Double.parseDouble(accuracy);
        }
*/
        if(Double.parseDouble(accuracy) > accOnCurrLevel){
            accOnCurrLevel = Double.parseDouble(accuracy);
            return true;
        }else
            return false;
    }




    private void buildFeatureFile(int i, String filename, String output) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output, false)

        ));
        BufferedReader feBr = new BufferedReader(new FileReader(filename));
        while(true){
            String feature = feBr.readLine();
            if(feature == null){
                break;
            }
            String regex;
            if (i == featuresAvailableTraining.size() - 1) {
                regex = ".." + featuresAvailableTraining.get(i) + "=.+?(?=})";
            }
            else {
                regex = featuresAvailableTraining.get(i) + "=.+?(?=[a-z])";
            }
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(feature);
            while(m.find()) {
                String subStr = m.replaceAll("");
                writer.write(subStr + "\n");
            }
        }
        writer.close();
    }
}

