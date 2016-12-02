import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by George on 2016-11-16.
 */
public class FileCombiner
{
    private String answers = "newControllersTraining.txt";
    private String answersVal = "newControllersValidation.txt";

    public FileCombiner() throws IOException {
    }

    public void buildFile(int numberOfFeatures, String features) throws IOException {
	boolean firstRun = true;
	String[] featureSplitted = features.split("\\.");
	Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream(featureSplitted[0] + ".train", false)
	));
	BufferedReader feBr = new BufferedReader(new FileReader(features));
	String answersToUse = features.equals("featuresTrainModified.txt") ? answers : answersVal;
	BufferedReader anBr = new BufferedReader(new FileReader(answersToUse));
	while (true) {
	    // change the loop length based on how many features we have.
	    String feature = feBr.readLine();
	    String answer = anBr.readLine();
	    if (feature == null || answer == null) {
		break;
	    }
	    if (!firstRun) writer.write("\n");
	    else firstRun = false;

	    String[] splittedOnSpace = feature.split(" ");

	    writer.write(answer + " ");

	    for (int i = 0; i < numberOfFeatures; i++) {
		String[] splittedOnEqual = splittedOnSpace[i].split("=");
		String[] splittedOnDot = splittedOnEqual[1].split("\\.");
		String result = splittedOnDot[0];
		if (i == 0) writer.write(1 + ":" + result);
		else writer.write(" " + (1+i) + ":" + result);
		writer.flush();
	    }

	}
	writer.close();
    }

}
