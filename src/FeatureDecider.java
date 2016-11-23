import jdk.nashorn.internal.ir.WhileNode;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fredrik on 23/11/2016.
 */
public class FeatureDecider {

    private String[] featuresAvailableTraining = {"numPlayerSprites", "numNPC", "numPortalTypes", "numTypesResources", "avatarHasResources",
            "isResourcesAvailable", "canShoot", "numNPCTypes", "numTypesResourcesAvatar", "blockSize", "numPortals", "numMovableSprites",
            "isUseAvailable", "canMoveVertically", "numImmovableSprites", "worldSize"};

    public FeatureDecider() throws IOException {
        FileCombiner fc = new FileCombiner();
        while (featuresAvailableTraining.length > 0){
            for (int i = 0; i < featuresAvailableTraining.length; i++) {
                buildFeatureFile(i, "featuresTraining.txt", "featuresTrainModified.txt");
                buildFeatureFile(i, "featuresValidation.txt", "featuresValidationModified.txt");
                fc.buildFile(featuresAvailableTraining.length - 1, "featuresTrainModified.txt");
                fc.buildFile(featuresAvailableTraining.length -1, "featuresValidationModified.txt");

            }
            break;
        }
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
            if (i == featuresAvailableTraining.length - 1) {
                regex = ".." + featuresAvailableTraining[i] + "=.+?(?=})";
            }
            else {
                regex = featuresAvailableTraining[i] + "=.+?(?=[a-z])";
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

