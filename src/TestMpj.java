/**
 * Created by holmgr on 11/18/16.
 */
import mpi.MPI;

public class mpj {

    public static void main(String[] args) {
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello world from <"+me+"> from <"+size);
        MPI.Finalize();
    }
}