package controllers.singlePlayer.hyperHeuristicAgent;

import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.*;
import core.game.Event;
import core.player.AbstractPlayer;
import global.svm_predict;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

    // Portfolio
    private controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer mctsPlayer;
    private int controllerClass = -1;
    private double classificationCertainty = -1.0;

    // One step look ahead members
    public static double epsilon = 1e-6;
    public static Random m_rnd;

    // filenames
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
	m_rnd = new Random();
	//Get the actions in a static array.
	List<ACTIONS> act = so.getAvailableActions();
	actions = new ACTIONS[act.size()];
	for(int i = 0; i < actions.length; ++i)
	{
	    actions[i] = act.get(i);
	}
	num_actions = actions.length;

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

	System.out.println("nanos Before featureloop: " + elapsedTimer.elapsedNanos());
	//Play for max secondsToSimulate
	final double secondsToSimulate = 0.9;
	int iters = 0;
	while (elapsedTimer.elapsedNanos() < secondsToSimulate*1000000000){
	    so.advance(getFeatureExploringAction(so, elapsedTimer));
	    detectFeatures(so);
	    iters++;
	}
	System.out.println("nanos After featureloop: " + elapsedTimer.elapsedNanos());
	System.out.println("iters done = " + iters);

	String featureString = features.toString();
	System.out.println(featureString);

	// write features to file for classification
	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		new FileOutputStream(FEATURES_FILENAME), "utf-8")))
	{
	    // write arbitrary number larger than number of classes for SVM to not take this as a training example
	    writer.write(47 + " ");
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
	try (BufferedReader reader = new BufferedReader(new FileReader(CLASSIFIED_RESULT))) {
	    String dummy = reader.readLine();
	    String line = reader.readLine();
	    String[] splitted = line.split(" ");
	    Double classDouble = Double.parseDouble(splitted[0]);
	    controllerClass = classDouble.intValue();
	    classificationCertainty = Double.parseDouble(splitted[controllerClass+1]);
	}
	catch (Exception e) {
	    System.out.println(String.format("Got Exception: %s", e));
	}
	System.out.println("Choosing controller " + controllerClass + " with certainty " + classificationCertainty*100+"%");
	switch (controllerClass) {
	    case 0:
		mctsPlayer = new controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer(new Random(), num_actions, actions);
		break;
	    case 1:
		olmctsPlayer = new controllers.singlePlayer.sampleOLMCTS.SingleMCTSPlayer(new Random(), num_actions, actions);
		break;
	    case 2:
		GAPlayer = new controllers.singlePlayer.sampleGA.Agent(so, elapsedTimer);
		break;
	    default:
		System.out.println("Class was not in range.");
		break;
	}
    }

    private ACTIONS getFeatureExploringAction(StateObservation obs, ElapsedCpuTimer timer) {
	Types.ACTIONS bestAction = null;
	double maxQ = Double.NEGATIVE_INFINITY;
	SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(obs);
	for (Types.ACTIONS action : obs.getAvailableActions()) {

	    StateObservation stCopy = obs.copy();
	    stCopy.advance(action);
	    double Q = heuristic.evaluateState(stCopy);
	    Q = Utils.noise(Q, this.epsilon, this.m_rnd.nextDouble());

	    //System.out.println("Action:" + action + " score:" + Q);
	    if (Q > maxQ) {
		maxQ = Q;
		bestAction = action;
	    }
	}

	//System.out.println("======== "  + maxQ + " " + bestAction + "============");
	return bestAction;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override public ACTIONS act(final StateObservation stateObs, final ElapsedCpuTimer elapsedTimer) {
	int action = 0;
	switch (controllerClass) {
	    case 0:
		mctsPlayer.init(stateObs);
		action = mctsPlayer.run(elapsedTimer);
		break;
	    case 1:
		olmctsPlayer.init(stateObs);
		action = olmctsPlayer.run(elapsedTimer);
		break;
	    case 2:
		return GAPlayer.act(stateObs, elapsedTimer);
	    default:
		break;
	}
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
	    double numTypesResourcesClass = FIRST_CLASS;
	    if (numTypesResources >= NUM_TYPES_RESOURCES_3RD_CLASS_LOW_LIMIT){
		numTypesResourcesClass = THIRD_CLASS;
	    }
	    else if (numTypesResources != 0) {
		numTypesResourcesClass = SECOND_CLASS;
	    }
	    features.put("numTypesResources", numTypesResourcesClass);
	    if (isResourcesAvailable > features.get("isResourcesAvailable"))
		features.put("isResourcesAvailable", isResourcesAvailable);
	}
	int numTypesResourcesAvatar = avatarResources.size();
	double avatarHasResources = numTypesResourcesAvatar == 0 ? 0.0 : 1.0;
	if (numTypesResourcesAvatar+0.0 > features.get("numTypesResourcesAvatar")){
	    features.put("numTypesResourcesAvatar", numTypesResourcesAvatar+0.0);
	}
	if (avatarHasResources > features.get("avatarHasResources")){
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
