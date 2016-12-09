package controllers.singlePlayer.featureCollectingAgent;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.*;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 *
 */
public class Agent extends AbstractPlayer {
    public int num_actions;
    public ACTIONS[] actions;
    // Features of a game, used for training set
    private HashMap<String, Double> features = new HashMap<>();
    private final static String FILENAME = "features.txt";

    // Use MCTS for feature collection
    private SingleMCTSPlayer mctsPlayer;

    private AbstractPlayer featureCollector = null;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
	//Get the actions in a static array.
	List<ACTIONS> act = so.getAvailableActions();
	actions = new ACTIONS[act.size()];

	for(int i = 0; i < actions.length; ++i)
	{
	    actions[i] = act.get(i);
	}
	num_actions = actions.length;

	//Create the player for simulation for feature collection
	//mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);
	featureCollector = new controllers.singlePlayer.sampleonesteplookahead.Agent(so, elapsedTimer);

	//Init some features
	initFeatures();

	//Collect features
	int blockSize = so.getBlockSize();
	int canMoveVertically = 0;
	int isUseAvailable = 0;

	//Are different actions available
	if(act.contains(ACTIONS.ACTION_DOWN) && act.contains(ACTIONS.ACTION_UP)){
	    canMoveVertically = 1;
	}
	if(act.contains(ACTIONS.ACTION_USE)) {
	    isUseAvailable = 1;
	}
	features.put("isUseAvailable", isUseAvailable+0.0);
	features.put("canMoveVertically", canMoveVertically +0.0);
	features.put("blockSize", blockSize+0.0);

	//mctsPlayer.init(so);
	while (elapsedTimer.elapsedMillis() < 6000){
	    so.advance(featureCollector.act(so, elapsedTimer));
	    detectFeatures(so);
	}
	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream(FILENAME, true), "utf-8")))
	{
	    writer.write(features.toString() + "\n");
	}
	catch (Exception e){
	    System.out.println(String.format("Got Exception: %s", e));
	}
	// Create a fresh player to use to play the game for further feature collection
	// mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */

    @Override public ACTIONS act(final StateObservation stateObs, final ElapsedCpuTimer elapsedTimer) {
	//Set the state observation object as the new root of the tree.
	mctsPlayer.init(stateObs);

	//Determine the action using MCTS...
	int action = mctsPlayer.run(elapsedTimer);

	//... and return it.
	return actions[action];
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

    public HashMap<String, Double> getFeatures() {
	return features;
    }

    private void initFeatures(){
	features.put("numNPCTypes", 0.0);
	features.put("numPortals", 0.0);
	features.put("numTypesResources", 0.0);
    }

    private void detectFeatures(StateObservation stateObs){
	ArrayList<Observation>[] portalTypes = stateObs.getPortalsPositions();
	detectPortalFeatures(portalTypes);
	ArrayList<Observation>[] NPCTypes = stateObs.getNPCPositions();
	detectNPCFeatures(NPCTypes);

	ArrayList<Observation>[] resources = stateObs.getResourcesPositions();
	detectResourceFeatures(resources);
    }

    /**
     * Get the number of Portal types and the total number of portals.
     *
     */
    private void detectPortalFeatures(List<Observation>[] portalTypes) {
	double numPortals = 0.0;
	if (portalTypes != null) {
	    for (List<Observation> portalType : portalTypes) numPortals += portalType.size();
	}
	if(numPortals > features.get("numPortals")){
	    features.put("numPortals", numPortals);
	}
    }

    /**
     * Get the number of NPC types and the total number of NPC in the game.
     */
    private void detectNPCFeatures(List<Observation>[] NPCTypes) {
	double numNPCTypes = 0.0;

	if (NPCTypes != null) {
	    numNPCTypes = NPCTypes.length;
	}

	if(numNPCTypes > features.get("numNPCTypes")){
	    features.put("numNPCTypes", numNPCTypes);
	}
    }

    private void detectResourceFeatures(List<Observation>[] resources) {
	if (resources != null){
	    int numTypesResources = resources.length;
	    if (numTypesResources > features.get("numTypesResources")){
		features.put("numTypesResources", numTypesResources+0.0);
	    }
	}
    }
}