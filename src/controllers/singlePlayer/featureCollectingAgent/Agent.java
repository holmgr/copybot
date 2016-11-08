package controllers.singlePlayer.featureCollectingAgent;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.*;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;

import java.util.*;
import java.util.List;

import java.awt.Event;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;



/**
 *
 */
public class Agent extends AbstractPlayer {

    public int num_actions;
    public Types.ACTIONS[] actions;
    // Features of a game, used for training set
    private HashMap<String, Double> features = new HashMap<>();
    private Random random;
    private core.game.Event lastEvent = new core.game.Event(-1,false,0,0,0,0,new Vector2d());
    private boolean firstRun = true;
    private int canShoot = 0;

    // Use MCTS for feature collection
    private SingleMCTSPlayer mctsPlayer;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
	random = new Random();
	//Get the actions in a static array.

	List<Types.ACTIONS> act = so.getAvailableActions();
	actions = new Types.ACTIONS[act.size()];

	for(int i = 0; i < actions.length; ++i)
	{
	    actions[i] = act.get(i);
	}
	num_actions = actions.length;

	//Create the player for simulation for feature collection
	mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);

	//Collect features
	Dimension dim = so.getWorldDimension();
	double size = dim.getHeight()*dim.getWidth();
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
	features.put("worldSize", size);
	features.put("blockSize", blockSize+0.0);

	mctsPlayer.init(so);
	while (elapsedTimer.elapsedMillis() < 500){
	    so.advance(actions[mctsPlayer.run(elapsedTimer)]);
	    if (elapsedTimer.elapsedMillis() % 50 == 0){
		Map<Integer, Integer> avatarResources = so.getAvatarResources();
		ArrayList<Observation>[] resources = so.getResourcesPositions();
		if (resources != null){
		    int numTypesResources = resources.length;
		    double isResourcesAvailable = numTypesResources == 0 ?
						  0.0 : 1.0;
		    features.put("numTypesResources", numTypesResources+0.0);
		    features.put("isResourcesAvailable", isResourcesAvailable);
		}
		int numTypesResourcesAvatar = avatarResources.size();
		double avatarHasResources = numTypesResourcesAvatar == 0 ?
					    0.0 : 1.0;
		features.put("numTypesResourcesAvatar", numTypesResourcesAvatar+0.0);
		features.put("avatarHasResources", avatarHasResources);
	    }
	}
	System.out.println(features.toString());
	// Create a fresh player to use to play the game for further feature collection
	mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);
        detectFeatures(so);
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

	if (canShoot == 0) {
	    isShootAvailable(stateObs);
	}


        detectFeatures(stateObs);

        //... and return it.
        return actions[action];
    }

    private void isShootAvailable(final StateObservation stateObs) {

	    TreeSet<core.game.Event> events = stateObs.getEventsHistory();

	    if (firstRun && events.size() > 0) {
		for (core.game.Event event : events) {
		    lastEvent = event;
		    if (event.fromAvatar) {
			canShoot = 1;
			break;
		    }
		}
		firstRun = false;
	    } else if (events.size() > 0) {
		SortedSet<core.game.Event> newEvents = events.tailSet(lastEvent);
		for (core.game.Event event : newEvents) {
		    lastEvent = event;
		    if (event.fromAvatar) {
			canShoot = 1;
			break;
		    }

	    }
	    features.put("canShoot", canShoot+0.0);
	}
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

    private void detectFeatures(StateObservation stateObs){
        double numPortals = 0.0;
        double numPortalTypes = 0.0;
        double numNPC = 0.0;
        double numNPCTypes = 0.0;
        double numImmovableSprites = 0.0;
        double numMovableSprites = 0.0;
        double numPlayerSprites = 0.0;

        // Get the number of Portal types and the total number of portals.
        ArrayList<Observation>[] portalTypes = stateObs.getPortalsPositions();
        if (portalTypes != null) {
            numPortalTypes = portalTypes.length;
            for (ArrayList<Observation> portalType : portalTypes) numPortals += portalType.size();
        }
        features.put("numPortals", numPortals);
        features.put("numPortalTypes", numPortalTypes);

        // Get the number of NPC types and the total number of NPC in the game.
        ArrayList<Observation>[] NPCTypes = stateObs.getNPCPositions();
        if (NPCTypes != null) {
            numNPCTypes = NPCTypes.length;
            for (ArrayList<Observation> NPCType : NPCTypes) numNPC += NPCType.size();
        }
        features.put("numNPC", numNPC);
        features.put("numNPCTypes", numNPCTypes);

        // Get the number if types of immovable sprites.
        ArrayList<Observation>[] immovableTypes = stateObs.getImmovablePositions();
        if (immovableTypes != null) numImmovableSprites = immovableTypes.length;
        features.put("numImmovableSprites", numImmovableSprites);

        // Get the number of types of movable sprites (NOT NPC).
        ArrayList<Observation>[] movableTypes = stateObs.getMovablePositions();
        if (movableTypes != null) numMovableSprites = movableTypes.length;
        features.put("numMovableSprites", numMovableSprites);

        // Get the number of types of sprites that are created by the player.
        ArrayList<Observation>[] spritesTypesByPlayer = stateObs.getFromAvatarSpritesPositions();
        if (spritesTypesByPlayer != null) numPlayerSprites = spritesTypesByPlayer.length;
        features.put("numPlayerSprites", numPlayerSprites);
    }
}
