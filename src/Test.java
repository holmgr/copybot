
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

class result
{
    private int game;
    private int level;
    private int timeStamp;
    private int win;
    private double score;
    private String controller;

    public result(final int game, final int level, final int timeStamp, final int win, final double score,
                  final String controller)
    {
        this.game = game;
        this.level = level;
        this.timeStamp = timeStamp;
        this.win = win;
        this.score = score;
        this.controller = controller;
    }
}


public class Test
{
    public static ArrayList<Integer> gameToController = new ArrayList<>();
    public static void manipArray() {
        gameToController.add(2);
    }
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

        // 1. This starts a game, in a level, played by a human.
    //    ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);
        
        // 2. This plays a game in a level by the controller.
        //ArcadeMachine.runOneGame(game, level1, visuals, featureCollectingController, recordActionsFile, seed, 0);

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
        
     //   6. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
        String currController = "";
        boolean firstRun = true;
        String[] controllers = {"sampleMCTSController", "sampleOLMCTSController", "sampleGAController"};
        for (int z = 0; z <controllers.length; z++) {
            currController = controllers[z];


            int N = 2, L = 2, M = 1;
            boolean saveActions = false;
            String[] levels = new String[L];
            String[] actionFiles = new String[L * M];
            for (int i = 0; i < N; ++i) {
                int actionIdx = 0;
                game = gamesPath + games[i] + ".txt";
                for (int j = 0; j < L; ++j) {
                    levels[j] = gamesPath + games[i] + "_lvl" + j + ".txt";
                    ArcadeMachine.runOneGame(game, levels[j], false, currController, null, seed, 0);
                    System.out.println("wins: " + core.game.Game.win + " points: " + core.game.Game.score + " Timestamp: " +
                                       core.game.Game.time);

                    result curr = new result(i, j, core.game.Game.time, core.game.Game.win, core.game.Game.score, currController);


                        if (saveActions) for (int k = 0; k < M; ++k)
                            actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
                }
            }
        }
    }
}
