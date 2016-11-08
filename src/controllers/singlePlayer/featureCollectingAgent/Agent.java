package controllers.singlePlayer.featureCollectingAgent;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.*;
import core.game.Event;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;

import java.util.*;
import java.util.List;

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
    public ACTIONS[] actions;
    // Features of a game, used for training set
    private HashMap<String, Double> features = new HashMap<>();
    private Event lastEvent = null;
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
	//Get the actions in a static array.
	List<ACTIONS> act = so.getAvailableActions();
	actions = new ACTIONS[act.size()];

	for(int i = 0; i < actions.length; ++i)
	{
	    actions[i] = act.get(i);
	}
	num_actions = actions.length;

	//Create the player for simulation for feature collection
	mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);

	//Init some features
	initFeatures();

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
	while (elapsedTimer.elapsedMillis() < 6000){
	    so.advance(actions[mctsPlayer.run(elapsedTimer)]);
	    detectFeatures(so);

	}
	System.out.println(features.toString());
	// Create a fresh player to use to play the game for further feature collection
	mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);
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

    private void initFeatures(){
	features.put("numNPCTypes", 0.0);
	features.put("numNPC", 0.0);
	features.put("numImmovableSprites", 0.0);
	features.put("numMovableSprites", 0.0);
	features.put("numPlayerSprites", 0.0);
	features.put("numPortals", 0.0);
	features.put("numPortalTypes", 0.0);
    }

    private void detectFeatures(StateObservation stateObs){
	ArrayList<Observation>[] portalTypes = stateObs.getPortalsPositions();
	detectPortalFeatures(portalTypes);
	ArrayList<Observation>[] NPCTypes = stateObs.getNPCPositions();
	detectNPCFeatures(NPCTypes);
	ArrayList<Observation>[] immovableTypes = stateObs.getImmovablePositions();
	ArrayList<Observation>[] movableTypes = stateObs.getMovablePositions();
	ArrayList<Observation>[] spritesTypesByPlayer = stateObs.getFromAvatarSpritesPositions();
	detectSpriteFeatures(immovableTypes, movableTypes, spritesTypesByPlayer);

	Map<Integer, Integer> avatarResources = stateObs.getAvatarResources();
	ArrayList<Observation>[] resources = stateObs.getResourcesPositions();
	detectResourceFeatures(resources, avatarResources);

	Set<Event> events = stateObs.getEventsHistory();
	if (canShoot == 0) {
	    tryDetectShootFeature(events);
	}
    }

    /**
     * Get the number of Portal types and the total number of portals.
     *
     */
    private void detectPortalFeatures(List<Observation>[] portalTypes) {
	double numPortals = 0.0;
	double numPortalTypes = 0.0;

	if (portalTypes != null) {
	    numPortalTypes = portalTypes.length;
	    for (List<Observation> portalType : portalTypes) numPortals += portalType.size();
	}
	if(numPortals > features.get("numPortals")){
	    features.put("numPortals", numPortals);
	}
	if(numPortalTypes > features.get("numPortalTypes")){
	    features.put("numPortalTypes", numPortalTypes);
	}
    }

    /**
     * Get the number of NPC types and the total number of NPC in the game.
     */
    private void detectNPCFeatures(List<Observation>[] NPCTypes) {
	double numNPC = 0.0;
	double numNPCTypes = 0.0;

	if (NPCTypes != null) {
	    numNPCTypes = NPCTypes.length;
	    for (List<Observation> NPCType : NPCTypes) numNPC += NPCType.size();
	}
	if(numNPC > features.get("numNPC")){
	    features.put("numNPC", numNPC);
	}
	if(numNPCTypes > features.get("numNPCTypes")){
	    features.put("numNPCTypes", numNPCTypes);
	}
    }

    private void detectSpriteFeatures(List<Observation>[] immovableTypes, List<Observation>[] movableTypes,
				      List<Observation>[] spritesTypesByPlayer) {
	double numImmovableSprites = 0.0;
	double numMovableSprites = 0.0;
	double numPlayerSprites = 0.0;

	// Get the number if types of immovable sprites.
	if (immovableTypes != null) numImmovableSprites = immovableTypes.length;
	if(numImmovableSprites > features.get("numImmovableSprites")) {
	    features.put("numImmovableSprites", numImmovableSprites);
	}

	// Get the number of types of movable sprites (NOT NPC).
	if (movableTypes != null) numMovableSprites = movableTypes.length;
	if(numMovableSprites > features.get("numMovableSprites")){
	    features.put("numMovableSprites", numMovableSprites);
	}

	// Get the number of types of sprites that are created by the player.
	if (spritesTypesByPlayer != null) numPlayerSprites = spritesTypesByPlayer.length;
	if(numPlayerSprites > features.get("numPlayerSprites")){
	    features.put("numPlayerSprites", numPlayerSprites);
	}
    }

    private void detectResourceFeatures(List<Observation>[] resources, Map<Integer, Integer> avatarResources ) {
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

    private void tryDetectShootFeature(Set<Event> events) {
	if (firstRun && !events.isEmpty()) {
	    checkEventsForCollision(events);
	    firstRun = false;
	} else if (!events.isEmpty()) {
	    SortedSet<Event> newEvents = ((TreeSet)events).tailSet(lastEvent);
	    checkEventsForCollision(newEvents);
	}
	features.put("canShoot", canShoot+0.0);
    }

    private void checkEventsForCollision(Set<Event> events){
	for (Event event : events) {
	    lastEvent = event;
	    if (event.fromAvatar) {
		canShoot = 1;
		break;
	    }
	}
    }
}
