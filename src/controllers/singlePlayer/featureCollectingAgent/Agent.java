package controllers.singlePlayer.featureCollectingAgent;

import controllers.singlePlayer.sampleMCTS.SingleMCTSPlayer;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


/**
 *
 */
public class Agent extends AbstractPlayer {

    public int num_actions;
    public Types.ACTIONS[] actions;
    private HashMap<String, Double> features = new HashMap<>();

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
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;

        //Create the player.
        mctsPlayer = new SingleMCTSPlayer(new Random(), num_actions, actions);

        //Collect features
        Dimension dim = so.getWorldDimension();
        double size = dim.getHeight()*dim.getWidth();
        int blockSize = so.getBlockSize();
        features.put("worldSize", size);
        features.put("blockSize", blockSize+0.0);

        detectFeatures(so);
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override public Types.ACTIONS act(final StateObservation stateObs, final ElapsedCpuTimer elapsedTimer) {
        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);



        //Determine the action using MCTS...
        int action = mctsPlayer.run(elapsedTimer);

        detectFeatures(stateObs);

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
