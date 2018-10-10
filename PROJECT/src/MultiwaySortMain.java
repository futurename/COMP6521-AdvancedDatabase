/*
import java.io.File;
import java.util.Scanner;*/


public class MultiwaySortMain {
	private final static double DEFAULT_MEM_RATE = 0.4;
	private final static String DEFAULT_TXT_PATH = "./TestData/input_1.txt";	
	final static String DEFAULT_TEMP_DATA_DIR = "./TestData/temp/";	
	static int totalNumberValue;
	
	
	public static void main(String[] args) throws Exception {
	/*	Scanner in = new Scanner(System.in);
		System.out.println("Please input the relative path of the data file(\"./(folder)(/filename)\": ");
		String dataPath = in.nextLine();

		if (dataPath.equals("\\s") || !new File(dataPath).isFile()) {
			dataPath = DEFAULT_TXT_PATH;
		}*/
		
		Long phaseoneStartTime = System.currentTimeMillis();
		
		String dataPath = DEFAULT_TXT_PATH;
		int memSize = getFreeMemSize();
		int bfMemSize = memSize % 2 == 0 ? memSize / 2 : memSize / 2 - 1;		

		PhaseOne.readAndSortNumbers(dataPath, bfMemSize);
		Long phaseoneEndTime = System.currentTimeMillis();
		long phaseoneTimeUsed = phaseoneEndTime - phaseoneStartTime;
		System.out.println("Phase one, time used: " + phaseoneTimeUsed + " ms\n");
		
		long phasetwoStartTime = System.currentTimeMillis();		
		PhaseTwo.phaseTwoMain();
		long phasetwoEndTime = System.currentTimeMillis();
		long phasetwoTimeUsed = phasetwoEndTime - phasetwoStartTime;
		System.out.println("Phase two, time used: " + phasetwoTimeUsed + " ms\n");
		System.out.println("total time cost: " + (phaseoneTimeUsed + phasetwoTimeUsed) + " ms\n" );
	}
	
	public static int getFreeMemSize() {
		System.gc();
		long totalFreeMem = Runtime.getRuntime().freeMemory();		
		int defFreeMem = (int) (totalFreeMem * DEFAULT_MEM_RATE);
		int result = defFreeMem;
		return result;
	}

	
}
