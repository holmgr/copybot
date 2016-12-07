package controllers.singlePlayer.hyperHeuristicAgent;

import core.game.*;
import core.player.AbstractPlayer;
import global.svm_predict;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Map;


/**
 * Agent 007
 */
public class Agent extends AbstractPlayer {

    // Selected agent from portfolio
    private AbstractPlayer chosenAgent = null;

    // Constants
    private final static int ARBITRARY_NUMBER = 47;

    // Filenames
    private final static String FEATURES_FILENAME = "featur.es";
    private final static String SVM_MODEL_FILENAME = "classify.model";
    private final static String CLASSIFIED_RESULT = "controller.ans";

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) throws IOException
    {
	final controllers.singlePlayer.featureCollectingAgent.Agent featureCollectingAgent =
		new controllers.singlePlayer.featureCollectingAgent.Agent(so, elapsedTimer);
	Map<String, Double> features = featureCollectingAgent.getFeatures();
	String featureString = features.toString();
	System.out.println(featureString);

	// write features to file for classification
	// TODO check that the ordering of features is correct
	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream(FEATURES_FILENAME), "utf-8")))
	{
	    // write arbitrary number larger than number of classes for SVM to not take this as a training example
	    writer.write(ARBITRARY_NUMBER + " ");
	    String[] splittedOnSpace = featureString.split(" ");
	    for (int i = 0; i < features.size(); i++) {
		String[] splittedOnEqual = splittedOnSpace[i].split("=");
		String[] splittedOnDot = splittedOnEqual[1].split("\\.");
		String result = splittedOnDot[0];
		if(i == 0) writer.write((i+1)+ ":" + result);
		else writer.write(" "+ (i+1) + ":" + result);
	    }
	}
	catch (Exception e){
	    System.out.println(String.format("Got Exception: %s", e));
	}
	svm_predict.main(new String[]{ "-b", "1", FEATURES_FILENAME, SVM_MODEL_FILENAME, CLASSIFIED_RESULT });
	int controllerClass = -1;
	double classificationCertainty = -1.0;
	try (BufferedReader reader = new BufferedReader(new FileReader(CLASSIFIED_RESULT))) {
	    // ignore first line
	    String dummyString = reader.readLine();
	    String line = reader.readLine();
	    String[] splitted = line.split(" ");
	    Double classDouble = Double.parseDouble(splitted[0]);
	    controllerClass = classDouble.intValue();
	    classificationCertainty = Double.parseDouble(splitted[controllerClass +1]);
	}
	catch (Exception e) {
	    System.out.println(String.format("Got Exception: %s", e));
	}
	System.out.println("Choosing controller " + controllerClass + " with certainty " + classificationCertainty *100+"%");
	switch (controllerClass) {
	    case 0:
		chosenAgent = new controllers.singlePlayer.YOLOBOT.Agent(so, elapsedTimer);
		break;
	    case 1:
		chosenAgent = new controllers.singlePlayer.YBCriber.Agent(so, elapsedTimer);
		break;
	    case 2:
		chosenAgent = new controllers.singlePlayer.thorbjrn.Agent(so, elapsedTimer);
		break;
	    case 3:
		chosenAgent = new controllers.singlePlayer.NovTea.Agent(so, elapsedTimer);
		break;
	    default:
		System.out.println("Class was not in range.");
		break;
	}
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override public ACTIONS act(final StateObservation stateObs, final ElapsedCpuTimer elapsedTimer) {
	return chosenAgent.act(stateObs, elapsedTimer);
    }

    /**
     * Function called when the game is over. This method must finish before CompetitionParameters.TEAR_DOWN_TIME,
     *  or the agent will be DISQUALIFIED
     * @param stateObservation the game state at the end of the game
     * @param elapsedCpuTimer timer when this method is meant to finish.
     */
    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
//        System.out.println("MCTS avg iters: " + SingleMCTSPlayer.iters / SingleMCTSPlayer.num);
	//Include your code here to know how it all ended.
	//System.out.println("Game over? " + stateObservation.isGameOver());
    }
}
