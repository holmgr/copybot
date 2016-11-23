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
    private String features = "featuresTraining.txt";
    private String answers = "controllersTraining.txt";

    public FileCombiner() throws IOException {
	boolean firstRun = true;
	Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream("trainingData.train", true)
	));
	BufferedReader feBr = new BufferedReader(new FileReader(features));
	BufferedReader anBr = new BufferedReader(new FileReader(answers));
	while(true){
	    // change the loop length based on how many features we have.
	    String feature = feBr.readLine();
	    String answer = anBr.readLine();
	    if(feature == null || answer == null){
		break;
	    }
	    if(!firstRun) writer.write("\n");
	    else firstRun = false;

	    String[] splittedOnSpace = feature.split(" ");

	    writer.write(answer + " ");
	    for (int i = 1; i < 17; i++) {
		String[] splittedOnEqual = splittedOnSpace[i-1].split("=");
		String[] splittedOnDot = splittedOnEqual[1].split("\\.");
		String result = splittedOnDot[0];
		if(i-1 == 0) writer.write(i+ ":" + result);
		else writer.write(" "+ i + ":" + result);
	    }

	}
	writer.close();
    }

}
