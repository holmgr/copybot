///**
// * Created by holmgr on 11/18/16.
// */
//import mpi.MPI;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//public class TestMpj {
//
//    private static final int TRAINING_SET_SIZE = 80;
//    private static final int NUMBER_OF_LEVELS = 5;
//    private static final int NUMBER_OF_TRIES = 1;
//
//    public static void main(String[] args) {
//
//        // All games in the training set
//        List<String> trainingSet = Arrays.asList(
//                "aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait",                //0-4
//                "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman",           //5-9
//                "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky",           //10-14
//                "camelRace", "catapults", "chainreaction", "chase", "chipschallenge",           //15-19
//                "clusters", "colourescape", "chopper", "cookmepasta", "cops",                   //20-24
//                "crossfire", "defem", "defender", "digdug", "dungeon",                          //25-29
//                "eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager",     //30-34
//                "firecaster", "fireman", "firestorms", "freeway", "frogs",                      //35-39
//                "garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga",         //40-44
//                "infection", "intersection", "islands", "jaws", "killBillVol1",                 //45-49
//                "labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings",                  //50-54
//                "missilecommand", "modality", "overload", "pacman", "painter",                  //55-59
//                "pokemon", "plants", "plaqueattack", "portals", "raceBet",                      //60-64
//                "racebet2", "realportals", "realsokoban", "rivers", "roadfighter",              //65-69
//                "roguelike", "run", "seaquest", "sheriff", "shipwreck",                         //70-74
//                "sokoban", "solarfox", "superman", "surround", "survivezombies"                 //75-79
//        );
//
//        // All controllers to test
//        List<String> controllers = Arrays.asList(
//                "controllers.singlePlayer.adrienctx.Agent",
//                "controllers.singlePlayer.NovTea.Agent",
//                "controllers.singlePlayer.MaastCTS2.Agent",
//                "controllers.singlePlayer.Return42.Agent",
//                "controllers.singlePlayer.thorbjrn.Agent",
//                "controllers.singlePlayer.YBCriber.Agent",
//                "controllers.singlePlayer.YOLOBOT.Agent"
//        );
//
//        // Get the actual names for the controllers
//        List<String> names = controllers
//                .stream()
//                .map(TestMpj::extractControllerName)
//                .collect(Collectors.toList());
//
//        // Calculate the length of the longest controller name
//        int maxNameLength = names
//                .stream()
//                .mapToInt(String::length)
//                .reduce(0, Math::max);
//
//
//        MPI.Init(args);
//        int me = MPI.COMM_WORLD.Rank();
//
//
//        // Take out the name for this controller
//        String name = extractControllerName(controllers.get(me));
//
//        long startTime = System.nanoTime();
//        int[] wins = Test.testController(controllers.get(me), trainingSet, TRAINING_SET_SIZE, NUMBER_OF_LEVELS, NUMBER_OF_TRIES);
//
//        // Format the result as a space separated string (2 chars per result)
//        String results = Arrays.stream(wins)
//                .mapToObj(res -> String.format("%4d", res))
//                .reduce("", String::concat);
//
//        int totalWins = Arrays.stream(wins).sum();
//
//        // Write results to file
//        Path file = Paths.get(String.format("%s.txt", names.get(me)));
//        try {
//            Files.write(file, Collections.singletonList(String.format("%" + maxNameLength + "s:\t%s\t%s\n", name, results, totalWins)), Charset.forName("UTF-8"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        long duration = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
//        System.out.printf("Finished testing controller: %s took %d seconds\n", name, duration);
//        MPI.Finalize();
//    }
//
//
//    private static String extractControllerName(String s) {
//        return s.split("\\.")[2];
//    }
//
//
//}