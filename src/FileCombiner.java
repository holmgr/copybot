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
    private String features = "features.txt";
    private String answers = "bestController.txt";

    public FileCombiner() throws IOException {
	int counter = 0;
	Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream("result.txt", true)
	));
	BufferedReader feBr = new BufferedReader(new FileReader(features));
	BufferedReader anBr = new BufferedReader(new FileReader(answers));
	while(true){
	    String feature = feBr.readLine();
	    String answer = anBr.readLine();
	    counter++;
	    if(feature == null || answer == null){
		System.out.println(feature  +  "   " + answer);;
		System.out.println(counter);
		break;
	    }
	    writer.write(feature + "\n");
	    writer.write(answer + "\n");


	}
	writer.flush();
	writer.close();
	System.out.println(counter);
    }

}
