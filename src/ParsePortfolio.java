import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by George on 2016-12-01.
 */
public class ParsePortfolio
{
    private String currFile = "";

    //total wins
    /*
    MAASTSCTS 871
    YBcriber 842
    thorbjorn 766
    NovTea 734
    adrien 626
    return42 574
    YOLOBOT -1019
    */

    public ParsePortfolio() throws IOException {
	String[] controllers = { "MaastCTS2", "YBCriber", "thorbjrn", "NovTea", "adrienctx", "Return42", "YOLOBOT" };
	ArrayList<int[]> controllersResult = new ArrayList<int[]>();

	//initialize controllersResult with arrays that can contain 80 ints.
	for (int i = 0; i < controllers.length; i++) {
	    controllersResult.add(new int[80]);
	}

	//iterate all controllers, 5 time each to get every txt file.
	for (int i = 0; i < controllers.length; i++) {
	    for (int j = 1; j < 6; j++) {
		String filename;
		if (j == 1) {
		    filename = controllers[i] + ".txt";
		} else {
		    filename = controllers[i] + j + ".txt";
		}
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String[] elements = reader.readLine().split("   ");

		//add all numbers into the controllers result list
		for (int k = 0; k < elements.length; k++) {
		    //if(i == 6 && k == 68) System.out.println(elements[k]);
		    controllersResult.get(i)[k] += Integer.parseInt(elements[k]);
		}

	    }

	}
	/*int nr = 0;
	for(int[] result : controllersResult){
	    System.out.println("controller : " + nr);
	    nr++;
	    for(int res : result){
		System.out.println(res);
	    }
	}*/

	ArrayList<Integer> finalResult = new ArrayList<>();
	int controller;
	for (int i = 0; i < controllersResult.get(0).length; i++) {
	    int max = 0;
	    controller = 0;
	    for (int j = 0; j < controllers.length; j++) {
		if (controllersResult.get(j)[i] > max){
		    max = controllersResult.get(j)[i];
		    controller = j;
		}
	    }
	    finalResult.add(controller);
	    System.out.println(finalResult.size());
	}
	// Time to write the index of the controller that has the most number of wins in one game over all 5 tries.
	Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream("trainingAsnwers.txt", false)
	));
	for(int bestController : finalResult){
	    writer.write(bestController + "\n");
	}
	writer.close();





    }
}
