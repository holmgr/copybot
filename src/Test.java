import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Random;
import core.ArcadeMachine;
import tools.StatSummary;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{

    // Uncomment below to compare what games fit to which controller
    /*
    private static Stats[][] gameToController = new Stats[83][6];
    private final static String FILENAME = "bestController";
    */

    public static void main(String[] args)
    {
	//Available controllers:
	String sampleRandomController = "controllers.singlePlayer.sampleRandom.Agent";
	String doNothingController = "controllers.singlePlayer.doNothing.Agent";
	String sampleOneStepController = "controllers.singlePlayer.sampleonesteplookahead.Agent";
	String sampleMCTSController = "controllers.singlePlayer.sampleMCTS.Agent";
	String sampleFlatMCTSController = "controllers.singlePlayer.sampleFlatMCTS.Agent";
	String sampleOLMCTSController = "controllers.singlePlayer.sampleOLMCTS.Agent";
	String sampleGAController = "controllers.singlePlayer.sampleGA.Agent";
	String sampleOLETSController = "controllers.singlePlayer.olets.Agent";
	String repeatOLETS = "controllers.singlePlayer.repeatOLETS.Agent";

        // Our agent that is used to collect features and save them
        String featureCollectingController = "controllers.singlePlayer.featureCollectingAgent.Agent";

	//Available Generators
	String randomLevelGenerator = "levelGenerators.randomLevelGenerator.LevelGenerator";
	String geneticGenerator = "levelGenerators.geneticLevelGenerator.LevelGenerator";
	String constructiveLevelGenerator = "levelGenerators.constructiveLevelGenerator.LevelGenerator";

	//Available games:
	String gamesPath = "examples/gridphysics/";
	String games[] = new String[]{};
	String generateLevelPath = "examples/gridphysics/";

	//All public games
	games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", //0-4
		"beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman",         //5-9
		"boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky",         //10-14
		"camelRace", "catapults", "chainreaction", "chase", "chipschallenge",         //15-19
		"clusters", "colourescape", "chopper", "cookmepasta", "cops",                 //20-24
		"crossfire", "defem",  "defender", "digdug", "dungeon",                       //25-29
		"eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager",   //30-34
		"firecaster",  "fireman", "firestorms", "freeway", "frogs",                   //35-39
		"garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga",       //40-44
		"infection", "intersection", "islands", "jaws", "killbillVol1",               //45-49
		"labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings",                //50-54
		"missilecommand", "modality", "overload", "pacman", "painter",                //55-59
		"pokemon", "plants", "plaqueattack", "portals", "racebet",                    //60-64
		"raceBet2", "realportals", "realsokoban", "rivers", "roadfighter",            //65-69
		"roguelike", "run", "seaquest", "sheriff", "shipwreck",                       //70-74
		"sokoban", "solarfox" ,"superman", "surround", "survivezombies",              //75-79
		"tercio", "thecitadel", "thesnowman",  "waitforbreakfast", "watergame",       //80-84
		"waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban",      //85-89
		"zelda", "zenpuzzle" };                                                       //90, 91


	//Other settings
	boolean visuals = true;
	int seed = new Random().nextInt();

	//Game and level to play

	int gameIdx = 0;
	int levelIdx = 4; //level names from 0 to 4 (game_lvlN.txt).

	String game = gamesPath + games[gameIdx] + ".txt";
	String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

	String recordLevelFile = generateLevelPath + games[gameIdx] + "_glvl.txt";
	String recordActionsFile = null;//"actions_" + games[gameIdx] + "_lvl" + levelIdx + "_" + seed + ".txt"; //where to record the actions executed. null if not to save.

	// Uncomment to collect features
	/*
	for (int i = 0; i < 80; i++){
	    String gameToPlay = gamesPath + games[i] + ".txt";

	    String levelToPlay = gamesPath + games[i] + "_lvl" + 0 + ".txt";
	    ArcadeMachine.runOneGame(gameToPlay, levelToPlay, visuals, featureCollectingController, recordActionsFile, seed,
				     0);

	}
        */


	// 1. This starts a game, in a level, played by a human.
	//    ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

	// 2. This plays a game in a level by the controller.
	//    ArcadeMachine.runOneGame(game, level1, visuals, featureCollectingController, recordActionsFile, seed, 0);

	// 3. This replays a game from an action file previously recorded
	//String readActionsFile = recordActionsFile;
	//ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

	// 4. This plays a single game, in N levels, M times :
//        String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";
//        int M = 10;
//        for(int i=0; i<games.length; i++){
//        	game = gamesPath + games[i] + ".txt";
//        	level1 = gamesPath + games[i] + "_lvl" + levelIdx +".txt";
//        	ArcadeMachine.runGames(game, new String[]{level1}, M, sampleMCTSController, null);
//        }

        //5. This starts a game, in a generated level created by a specific level generator

        //if(ArcadeMachine.generateOneLevel(game, randomLevelGenerator, recordLevelFile)){
        //	ArcadeMachine.playOneGeneratedLevel(game, recordActionsFile, recordLevelFile, seed);
        //}



        //6. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).

//        int N = 20, L = 5, M = 1;
//        boolean saveActions = false;
//        String[] levels = new String[L];
//        String[] actionFiles = new String[L*M];
//        for(int i = 0; i < N; ++i)
//        {
//            int actionIdx = 0;
//            game = gamesPath + games[i] + ".txt";
//            for(int j = 0; j < L; ++j){
//                levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
//                if(saveActions) for(int k = 0; k < M; ++k)
//                    actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//            }
//            ArcadeMachine.runGames(game, levels, M, featureCollectingController, saveActions? actionFiles:null);
//        }


        //      This code is for playing games and writing which controller was best suitable for the games played to a File.
        //      You can choose to play a game several times with several levels
       /*
        boolean firstRun = true;
        boolean update = false;
        int N = 79, L = 5;
        String[] controllers = {sampleMCTSController, sampleOLMCTSController, sampleGAController};
        for (int z = 0; z < controllers.length; z++) {
            String[] levels = new String[L];
            for (int i = 0; i < N; ++i) {
                game = gamesPath + games[i] + ".txt";
                for (int j = 0; j < L; ++j) {
                    System.out.println("Spel nummer: " + " " + i + " " + "Level: " + " " + j);
                    levels[j] = gamesPath + games[i] + "_lvl" + j + ".txt";
                    double[] result = ArcadeMachine.runOneGame(game, levels[j], false, controllers[z], null, seed, 0);
                    double win = result[0];
                    double score = result[1];
                    double timeSteps = result[2];

                    if (firstRun) {
                        Stats currStats = new Stats(timeSteps, win, score, z);
                        gameToController[i][j] = currStats;
                    } else {
                        Stats bestStats = gameToController[i][j];
                        if (bestStats.win < win) {
                           update = true;
                        } else if (bestStats.win == win){
                            if(bestStats.score < score){
                                update = true;
                            }else if(bestStats.score == score){
                                if(bestStats.timeStamp > timeSteps){
                                    update = true;
                                }
                            }
                        }
                        if (update) {
                            Stats currStats = new Stats(timeSteps, win, score, z);
                            gameToController[i][j] = currStats;
                            update = false;
                        }
                    }
                }
            }
            firstRun = false;
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FILENAME, true), "utf-8")))
        {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < L; j++) {
                    writer.write(gameToController[i][j].controller + "\n");
                }
            }
        }
        catch (Exception e){
            System.out.println(String.format("Got Exception: %s", e));
        }
    }
*/


// This class is used when we want to write to file what controller is best with what game.
        /*
    static class Stats
    {
        private double timeStamp;
        private double win;
        private double score;
        private int controller;

        public Stats(final double timeStamp, final double win, final double score,
                     final int controller)
        {
            this.timeStamp = timeStamp;
            this.win = win;
            this.score = score;
            this.controller = controller;
        }
*/
    }
}
