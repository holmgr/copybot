package controllers.singlePlayer.featureCollectingAgent;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.*;
import core.game.Event;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.awt.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;



/**
 * Agent 007
 */
public class Agent extends AbstractPlayer {
    public int num_actions;
    public ACTIONS[] actions;
    // Features of a game, used for training set
    private static HashMap<String, Double> features = new HashMap<>();
    private Event lastEvent = null;
    private boolean firstRun = true;
    private int canShoot = 0;

    // Use MCTS for feature collection
    private SingleMCTSPlayer mctsPlayer;
    // Name of file to write features to
    private final static String FILENAME = "featuresTraining.txt";
    private final static double FIRST_CLASS = 0.0;
    private final static double SECOND_CLASS = 1.0;
    private final static double THIRD_CLASS = 2.0;

    private static final int NUM_NPC_3RD_CLASS_LOW_LIMIT = 11;
    private static final int NUM_NPCTYPES_3RD_CLASS_LOW_LIMIT = 4;
    private static final int NUM_TYPES_RESOURCES_3RD_CLASS_LOW_LIMIT = 4;
    private static final int BLOCKSIZE_2ND_CLASS_LOWER_LIMIT = 40;
    private static final int WORLDSIZE_2ND_CLASS_LOWER_LIMIT = 30000;
    private static final int NUM_PORTALS_3RD_CLASS_LOW_LIMIT = 2;
    private static final int NUM_PORTALTYPES_3RD_CLASS_LOW_LIMIT = 2;
    private static final int NUM_IMMOV_SPRITES_2ND_CLASS_LOW_LIMIT = 3;
    private static final int NUM_MOV_SPRITES_2ND_CLASS_LOW_LIMIT = 3;
    private static final int NUM_PLAYER_SPRITES_2ND_CLASS_LOW_LIMIT = 1;

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
	double worldSize = dim.getHeight()*dim.getWidth();
	double worldSizeClass = FIRST_CLASS;
	if (worldSize >= WORLDSIZE_2ND_CLASS_LOWER_LIMIT) {
	    worldSizeClass = SECOND_CLASS;
	}
	features.put("worldSize", worldSizeClass);

	int blockSize = so.getBlockSize();
	double blockSizeClass = FIRST_CLASS;
	if (blockSize >= BLOCKSIZE_2ND_CLASS_LOWER_LIMIT) {
	    blockSizeClass = SECOND_CLASS;
	}
	features.put("blockSize", blockSizeClass);

	//Are different actions available
	int canMoveVertically = 0;
	if(act.contains(ACTIONS.ACTION_DOWN) && act.contains(ACTIONS.ACTION_UP)){
	    canMoveVertically = 1;
	}
	features.put("canMoveVertically", canMoveVertically+0.0);
	int isUseAvailable = 0;
	if(act.contains(ACTIONS.ACTION_USE)) {
	    isUseAvailable = 1;
	}
	features.put("isUseAvailable", isUseAvailable+0.0);

	//Init the controller used for feature collection
	mctsPlayer.init(so);
	//Play for max secondsToSimulate
	final int secondsToSimulate = 6;
	while (elapsedTimer.elapsedMillis() < secondsToSimulate*1000){
	    so.advance(actions[mctsPlayer.run(elapsedTimer)]);
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
	// (possibly unneccessary)
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

    /**
     * Init features that we can assume as 0 initially
     */
    private void initFeatures(){
	features.put("numNPCTypes", 0.0);
	features.put("numNPC", 0.0);
	features.put("numImmovableSprites", 0.0);
	features.put("numMovableSprites", 0.0);
	features.put("numPlayerSprites", 0.0);
	features.put("numPortals", 0.0);
	features.put("numPortalTypes", 0.0);
	features.put("numTypesResources", 0.0);
	features.put("isResourcesAvailable", 0.0);
	features.put("numTypesResourcesAvatar", 0.0);
	features.put("avatarHasResources", 0.0);
    }

    /**
     * Detect features in the current state observation
     * @param stateObs state observation object we use to analyze features
     */
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
     * Analyze the number of Portal types and the total number of portals.
     */
    private void detectPortalFeatures(List<Observation>[] portalTypes) {
	int numPortals = 0;
	int numPortalTypes = 0;

	if (portalTypes != null) {
	    numPortalTypes = portalTypes.length;
	    for (List<Observation> portalType : portalTypes) numPortals += portalType.size();
	}

	double numPortalsClass = FIRST_CLASS;
	if(numPortals > NUM_PORTALS_3RD_CLASS_LOW_LIMIT){
	    numPortalsClass = THIRD_CLASS;
	}
	else if (numPortals != 0){
	    numPortalsClass = SECOND_CLASS;
	}
	if (numPortalsClass > features.get("numPortals")){
	    features.put("numPortals", numPortalsClass);
	}

	double numPortalTypesClass = FIRST_CLASS;
	if(numPortalTypes > NUM_PORTALTYPES_3RD_CLASS_LOW_LIMIT){
	    numPortalTypesClass = THIRD_CLASS;
	}
	else if (numPortalTypes != 0){
	    numPortalTypesClass = SECOND_CLASS;
	}
	if (numPortalTypesClass > features.get("numPortalTypes")){
	    features.put("numPortalTypes", numPortalTypesClass);
	}
    }

    /**
     * Analyze the number of NPC types and the total number of NPC in the game.
     */
    private void detectNPCFeatures(List<Observation>[] NPCTypes) {
	int numNPC = 0;
	int numNPCTypes = 0;

	if (NPCTypes != null) {
	    numNPCTypes = NPCTypes.length;
	    for (List<Observation> NPCType : NPCTypes) numNPC += NPCType.size();
	}

	double numNpcClass = FIRST_CLASS;
	if (numNPC >= NUM_NPC_3RD_CLASS_LOW_LIMIT) {
	    numNpcClass = THIRD_CLASS;
	}
	else if (numNPC != 0) {
	    numNpcClass = SECOND_CLASS;
	}
	if(numNpcClass > features.get("numNPC")){
	    features.put("numNPC", numNpcClass);
	}

	double numNpcTypesClass = FIRST_CLASS;
	if (numNPCTypes >= NUM_NPCTYPES_3RD_CLASS_LOW_LIMIT) {
	    numNpcTypesClass = THIRD_CLASS;
	}
	else if (numNPCTypes != 0) {
	    numNpcTypesClass = SECOND_CLASS;
	}
	if(numNpcTypesClass > features.get("numNPCTypes")){
	    features.put("numNPCTypes", numNpcTypesClass);
	}
    }

    /**
     * Analyze the number of different sprite
     * @param immovableTypes array of ArrayLists where each ArrayList contains observations
     *                       of a certain type of immovable sprites
     * @param movableTypes array of ArrayLists where each ArrayList contains observations
     *                     of a certain type of movable spirtes
     * @param spritesTypesByPlayer array of ArrayLists where each ArrayList contains observations
     *                             of a certain type of sprites created by the player (avatar)
     */
    private void detectSpriteFeatures(List<Observation>[] immovableTypes, List<Observation>[] movableTypes,
				      List<Observation>[] spritesTypesByPlayer) {
	int numImmovableSprites = 0;
	int numMovableSprites = 0;
	int numPlayerSprites = 0;

	// Get the number of types of immovable sprites.
	if (immovableTypes != null) numImmovableSprites = immovableTypes.length;
	double numImmovableSpritesClass = FIRST_CLASS;
	if (numImmovableSprites > NUM_IMMOV_SPRITES_2ND_CLASS_LOW_LIMIT) {
	    numImmovableSpritesClass = SECOND_CLASS;
	}
	if(numImmovableSpritesClass > features.get("numImmovableSprites")) {
	    features.put("numImmovableSprites", numImmovableSpritesClass);
	}

	// Get the number of types of movable sprites (NOT NPC).
	if (movableTypes != null) numMovableSprites = movableTypes.length;
	double numMovableSpritesClass = FIRST_CLASS;
	if (numMovableSprites > NUM_MOV_SPRITES_2ND_CLASS_LOW_LIMIT) {
	    numMovableSpritesClass = SECOND_CLASS;
	}
	if(numMovableSpritesClass > features.get("numMovableSprites")) {
	    features.put("numMovableSprites", numMovableSpritesClass);
	}

	// Get the number of types of sprites that are created by the player.
	if (spritesTypesByPlayer != null) numPlayerSprites = spritesTypesByPlayer.length;
	double numPlayerSpritesClass = FIRST_CLASS;
	if (numPlayerSprites > NUM_PLAYER_SPRITES_2ND_CLASS_LOW_LIMIT){
	    numPlayerSpritesClass = SECOND_CLASS;
	}
	if(numPlayerSpritesClass > features.get("numPlayerSprites")){
	    features.put("numPlayerSprites", numPlayerSpritesClass);
	}
    }

    /**
     * Analyze resources in the game
     * @param resources Array of ArrayLists where each ArrayList contains all observation of certain type of resource
     * @param avatarResources A map where keyset is type of resource and value is how many of that type the avatar has
     */
    private void detectResourceFeatures(List<Observation>[] resources, Map<Integer, Integer> avatarResources ) {
	if (resources != null){
	    int numTypesResources = resources.length;
	    double isResourcesAvailable = numTypesResources == 0 ?
					  0.0 : 1.0;
	    if (isResourcesAvailable > features.get("isResourcesAvailable")) {
		features.put("isResourcesAvailable", isResourcesAvailable);
	    }
	    if (numTypesResources > features.get("numTypesResources")){
		features.put("numTypesResources", numTypesResources+0.0);
	    }
	}
	int numTypesResourcesAvatar = avatarResources.size();
	double avatarHasResources = numTypesResourcesAvatar == 0 ?
				    0.0 : 1.0;
	if (numTypesResourcesAvatar > features.get("numTypesResourcesAvatar")) {
	    features.put("numTypesResourcesAvatar", numTypesResourcesAvatar+0.0);
	}
	if (avatarHasResources > features.get("avatarHasResources")) {
	    features.put("avatarHasResources", avatarHasResources);
	}
    }

    /**
     * Analyze whether avatar can shoot with the USE action
     * @param events All events that have occured (collisions)
     */
    private void tryDetectShootFeature(Collection<Event> events) {
	if (firstRun && !events.isEmpty()) {
	    checkEventsForCollision(events);
	    firstRun = false;
	} else if (!events.isEmpty()) {
	    // all events that occured after lastEvent
	    SortedSet<Event> newEvents = ((TreeSet)events).tailSet(lastEvent);
	    checkEventsForCollision(newEvents);
	}
	features.put("canShoot", canShoot+0.0);
    }

    /**
     * Find out if any coliision event has occured with a sprite created by avatar
     * @param events A set of events we will check
     */
    private void checkEventsForCollision(Iterable<Event> events){
	for (Event event : events) {
	    lastEvent = event;
	    if (event.fromAvatar) {
		canShoot = 1;
		break;
	    }
	}
    }
}
