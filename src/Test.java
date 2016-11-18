import core.ArcadeMachine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    private static final int TRAINING_SET_SIZE = 1;
    private static final int NUMBER_OF_LEVELS = 5;
    private static final int NUMBER_OF_TRIES = 1;

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
                "controllers.singlePlayer.adrienctx.Agent",
                "controllers.singlePlayer.NovTea.Agent",
                "controllers.singlePlayer.MaastCTS2.Agent",
                "controllers.singlePlayer.Return42.Agent",
                "controllers.singlePlayer.thorbjrn.Agent",
                "controllers.singlePlayer.YBCriber.Agent",
                "controllers.singlePlayer.YOLOBOT.Agent"
        );

        // Get the actual names for the controllers
        List<String> names = controllers
                .stream()
                .map(Test::extractControllerName)
                .collect(Collectors.toList());

        // Calculate the length of the longest controller name
        int maxNameLength = names
                .stream()
                .mapToInt(String::length)
                .reduce(0, Math::max);

        // List of strings to be written to file
        List<String> lines = new ArrayList<>();

        // Print header for table
        int numberOfSpaces = TRAINING_SET_SIZE * 4;
        lines.add(String.format("%" + maxNameLength + "s:\t" + "%" + numberOfSpaces + "s\t%s:" ,
                "Controllers",
                "",
                "Total"));

        long testStartTime = System.nanoTime();

        // Do a run of all games for each controller and print the results
        for (String controller : controllers) {

            // Take out the name for this controller
            String name = extractControllerName(controller);

            long startTime = System.nanoTime();
            int[] wins = testController(controller, trainingSet, TRAINING_SET_SIZE, NUMBER_OF_LEVELS, NUMBER_OF_TRIES);

            // Format the result as a space separated string (2 chars per result)
            String results = Arrays.stream(wins)
                    .mapToObj(res -> String.format("%4d", res))
                    .reduce("", String::concat);

            int totalWins = Arrays.stream(wins).sum();

            long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            System.out.printf("Finished testing controller: %s took %d seconds\n", name, duration);

            lines.add(String.format("%" + maxNameLength + "s:\t%s\t%s", name, results, totalWins));
        }

        // Write results to file
        Path file = Paths.get("controller-results.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long testDuration = TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - testStartTime);
        System.out.printf("Entire test took: %d minutes", testDuration);
    }

    private static String extractControllerName(String s) {
        return s.split("\\.")[2];
    }

    public static int[] testController(String controller, List<String> gameSet, int trainingSetSize, int numberOfLevels, int numberOfTries) {
        int seed = 4; // Arbitrary random number :)

        String gamesPath = "examples/gridphysics/";
        int[] wins = new int[trainingSetSize];

        for (int j = 0; j < trainingSetSize; j++) {

            String game = gamesPath + gameSet.get(j) + ".txt";

            for (int k = 0; k < numberOfLevels; ++k) {

                for (int l = 0; l < numberOfTries; l++) {
                    String level = gamesPath + gameSet.get(j) + "_lvl" + k + ".txt";
                    try {
                        double[] results = ArcadeMachine.runOneGame(game, level, false, controller, null, seed, 0);
                        wins[j] += (int) results[0];
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        return wins;
    }
}
