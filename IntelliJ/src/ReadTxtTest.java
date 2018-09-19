import java.io.BufferedReader;
import java.io.FileReader;

public class ReadTxtTest {
	static int[] bufferNumbers = new int[1024];
	
	public static void main(String[] args) throws Exception {
		String filename = "C:\\Users\\WW\\Google Drive\\!!Fall 2018\\COMP 6521 Advanced " +
                "database\\Project1\\TestData\\data.txt";
		//String filename = "d:\\data.txt";
		readNumbers(filename);
		
		for(int i = 0; i < 1024; i++) {
			System.out.println(i + ": " +  bufferNumbers[i]);
		}		
		
	}
	
	static void readNumbers(String filename) throws Exception {
		
		FileReader fr = new FileReader(filename);		
		BufferedReader br = new BufferedReader(fr);		
		
		int curNum = 0;
		char curCh;
		String curNumber = "";
		int bufferCount = 0;
		
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);

		while((curNum = br.read()) != -1) {
			curCh = (char)curNum;
			if(curNum != 32) {
				curNumber += curCh;
				//System.out.println(curNumber);
			}
			else {
				if(bufferCount < 1024) {
                    bufferNumbers[bufferCount++] = Integer.parseInt(curNumber);
                    System.out.println(curNumber);
                    curNumber = "";
                }
                else{
                	curNumber = "";
					continue;
				}

			}
		}
		
		long end = System.currentTimeMillis();
	
		System.out.println("total time: " + (end - start) + " ms");
		
		fr.close();
		br.close();
		
	}

}
