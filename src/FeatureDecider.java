import global.svm_predict;
import global.svm_train;

import java.io.*;
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
	for (int i = 0; i < boolArrays.size(); i++) {
	    boolean[] bools = boolArrays.get(i);
	    buildFeatureFile(bools, "featuresTraining.txt", "featuresTrainModified.txt");
	    buildFeatureFile(bools, "featuresValidation.txt", "featuresValidationModified.txt");
	    fc.buildFile(featuresAvailableTraining.size() - 1, "featuresTrainModified.txt");
	    fc.buildFile(featuresAvailableTraining.size() - 1, "featuresValidationModified.txt");
	    svm_train.main(trainingFiles);
	    svm_predict.main(predictFiles);
	    updateBest(i);
	    System.out.println("finished iter " + iters);
	    iters++;
	    System.out.println("curr best is " + globalBestAcc);
	}

	boolean[] best = boolArrays.get(bestIndex);
	for (int i = 0; i < best.length; i++) {
	    if (!best[i]) {
		featuresAvailableTraining.remove(i);
	    }
	}

//	while (featuresAvailableTraining.size() > 0) {
//	    for (int i = 0; i < featuresAvailableTraining.size(); i++) {
//		buildFeatureFile(i, "featuresTraining.txt", "featuresTrainModified.txt");
//		buildFeatureFile(i, "featuresValidation.txt", "featuresValidationModified.txt");
//		fc.buildFile(featuresAvailableTraining.size() - 1, "featuresTrainModified.txt");
//		fc.buildFile(featuresAvailableTraining.size() - 1, "featuresValidationModified.txt");
//		svm_train.main(trainingFiles);
//		svm_predict.main(predictFiles);
//		if(updateBest()){
//		    featureToRemove = i;
//		}
//	    }
//	    if(currBestAcc > globalBestAcc) {
//		globalBestAcc = currBestAcc;
//		featuresAvailableTraining.remove(featureToRemove);
//		iters++;
//	    }
//	    else if (iters > 5) break;
//	    else iters++;
//	}
//
	featuresAvailableTraining.forEach(System.out::println);
	System.out.println(featuresAvailableTraining.size());
	System.out.println(globalBestAcc);
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
	    if(acc > globalBestAcc){
		globalBestAcc = acc;
		bestIndex = currIndex;
	    }
	}
    }




    private void buildFeatureFile(boolean[] indexes, String filename, String output) throws IOException {
	Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream(output, false)

	));
	BufferedReader feBr = new BufferedReader(new FileReader(filename));
	while(true){
	    String feature = feBr.readLine();
	    if(feature == null){
		break;
	    }
	    String subStr = "";
	    for (int i = 0; i < indexes.length; i++){
		if (!indexes[i]) continue;

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
		    subStr = m.replaceAll("");
		}
	    }
	    writer.write(subStr + "\n");
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

