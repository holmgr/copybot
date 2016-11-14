import core.ArcadeMachine;

import java.util.concurrent.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) {

        // All games in the training set
        List<String> trainingSet = Arrays.asList(
                "aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait",                //0-4
                "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman",           //5-9
                "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky",           //10-14
                "camelRace", "catapults", "chainreaction", "chase", "chipschallenge",           //15-19
                "clusters", "colourescape", "chopper", "cookmepasta", "cops",                   //20-24
                "crossfire", "defem", "defender", "digdug", "dungeon",                          //25-29
                "eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager",     //30-34
                "firecaster", "fireman", "firestorms", "freeway", "frogs",                      //35-39
                "garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga",         //40-44
                "infection", "intersection", "islands", "jaws", "killbillVol1",                 //45-49
                "labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings",                  //50-54
                "missilecommand", "modality", "overload", "pacman", "painter",                  //55-59
                "pokemon", "plants", "plaqueattack", "portals", "racebet",                      //60-64
                "raceBet2", "realportals", "realsokoban", "rivers", "roadfighter",              //65-69
                "roguelike", "run", "seaquest", "sheriff", "shipwreck",                         //70-74
                "sokoban", "solarfox", "superman", "surround", "survivezombies"                 //75-79
        );

        // All controllers to test
        List<String> controllers = Arrays.asList(
                "controllers.singlePlayer.sampleMCTS.Agent",
                "controllers.singlePlayer.sampleFlatMCTS.Agent",
                "controllers.singlePlayer.sampleOLMCTS.Agent",
                "controllers.singlePlayer.sampleGA.Agent",
                "controllers.singlePlayer.olets.Agent",
                "controllers.singlePlayer.repeatOLETS.Agent"
        );

        List<String> gameSet = trainingSet.subList(0, 2);

        // Get the actual names for the controllers
        List<String> names = controllers
                .stream()
                .map(s -> s.split("\\.")[2])
                .collect(Collectors.toList());

        // Calculate the length of the longest controller name
        int maxNameLength = names
                .stream()
                .mapToInt(String::length)
                .reduce(0, Math::max);

        // Do a run of all games for each controller and print the results
        String format = "%" + maxNameLength + "s:\t";
        for (String controller : controllers) {
            String name = controller.split("\\.")[2];
            System.out.printf(format, name);
            System.out.println(Arrays.toString(testController(controller, gameSet)));
        }
    }

    private static int[] testController(String controller, List<String> gameSet) {
        int seed = 4; // Arbitrary random number :)

        String gamesPath = "examples/gridphysics/";
        int numLevels = 1, tries = 1;
        int[] wins = new int[gameSet.size()];

        for (int j = 0; j < gameSet.size(); j++) {

            String game = gamesPath + gameSet.get(j) + ".txt";

            for (int k = 0; k < numLevels; ++k) {

                for (int l = 0; l < tries; l++) {
                    String level = gamesPath + gameSet.get(j) + "_lvl" + j + ".txt";
                    double[] results = ArcadeMachine.runOneGame(game, level, false, controller, null, seed, 0);
                    wins[j] += (int) results[0];
                }
            }
        }
        return wins;
    }
}
