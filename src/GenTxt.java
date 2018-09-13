import java.io.*;
import java.util.Random;

public class GenTxt {

    final static int numberToBeGen = 999999;

    public static void main(String[] args) throws IOException {
        String pathname = "C:\\Users\\WW\\Google Drive\\!!Fall 2018\\COMP 6521 Advanced " +
                "database\\Project1\\TestData\\data.txt";
        File filename = new File(pathname);
        filename.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        genRanNum(out, numberToBeGen);
        System.out.println("finished!");


    }

    static void genRanNum(BufferedWriter out, int numToGen) throws IOException {
        if(numToGen <= 0){
            return;
        }
        else{
            Random rand = new Random();
            int length = (int) Math.log10(numToGen);

            for(int i = 0 ; i < numToGen; i++){
                int ranNumber = (int) (rand.nextInt((int) (numToGen - Math.pow(10, length)))
                        + Math.pow(10,length));
                out.write(ranNumber + " ");
                out.flush();
            }
            out.close();
        }
    }
}
