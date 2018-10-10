import java.io.*;
import java.util.Random;

public class GenTxt {

    final static int numberToBeGen = 1000000;
    final static String pathname = "./TestData/data1.txt";

    public static void main(String[] args) throws IOException {
                
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
        	String firstLine = numToGen + "\t5m";
        	out.write(firstLine);
        	out.newLine();
        	out.newLine();
            Random rand = new Random();
            //int length = (int) Math.log10(numToGen);

            for(int i = 0 ; i < numToGen; i++){
                int ranNumber = (int) rand.nextInt((int) Math.pow(10, 10)) + 1;               
                out.write(ranNumber + " ");
                out.flush();
            }
            out.close();
        }
    }
}
